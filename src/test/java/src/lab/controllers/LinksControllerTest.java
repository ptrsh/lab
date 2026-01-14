package src.lab.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import src.lab.db.models.Link;
import src.lab.db.models.User;
import src.lab.db.repositories.LinksRepository;
import src.lab.db.repositories.UsersRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class LinksControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LinksRepository linkRepository;

    @Autowired
    private UsersRepository usersRepository;

    private String userId1;
    private String userId2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        linkRepository.deleteAll();
        usersRepository.deleteAll();

        userId1 = "550e8400-e29b-41d4-a716-446655440000";
        userId2 = "550e8400-e29b-41d4-a716-446655440001";
    }

    @Test
    void createLink_validRequest_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/links")
                        .header("Authorization", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\",\"clickLimit\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.clickLimit").value(10))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createLink_withoutClickLimit_usesDefault() throws Exception {
        mockMvc.perform(post("/api/links")
                        .header("Authorization", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clickLimit").value(100));
    }

    @Test
    void createLink_invalidUrl_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/links")
                        .header("Authorization", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"not-a-url\",\"clickLimit\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("URL must start with")));
    }

    @Test
    void createLink_missingAuthHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createLink_invalidUuid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/links")
                        .header("Authorization", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLink_sameUrlDifferentUsers_generatesUniqueShortCodes() throws Exception {
        String url = "https://example.com";

        String shortCode1 = mockMvc.perform(post("/api/links")
                        .header("Authorization", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String shortCode2 = mockMvc.perform(post("/api/links")
                        .header("Authorization", userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assert !shortCode1.equals(shortCode2);
    }

    @Test
    void getUserLinks_returnsAllUserLinks() throws Exception {
        createTestLink(userId1, "code1", "https://example.com/1");
        createTestLink(userId1, "code2", "https://example.com/2");
        createTestLink(userId2, "code3", "https://example.com/3");

        mockMvc.perform(get("/api/links")
                        .header("Authorization", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].shortCode", containsInAnyOrder("code1", "code2")));
    }

    @Test
    void getLink_validShortCode_returnsLink() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(get("/api/links/abc123")
                        .header("Authorization", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"));
    }

    @Test
    void getLink_notFound_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/links/nonexistent")
                        .header("Authorization", userId1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Link not found")));
    }

    @Test
    void updateLink_validOwner_updatesLimit() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(patch("/api/links/abc123")
                        .header("Authorization", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clickLimit\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickLimit").value(20));
    }

    @Test
    void updateLink_differentUser_returnsForbidden() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(patch("/api/links/abc123")
                        .header("Authorization", userId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clickLimit\":20}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to modify this link"));
    }

    @Test
    void deleteLink_validOwner_deletesLink() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(delete("/api/links/abc123")
                        .header("Authorization", userId1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/links/abc123")
                        .header("Authorization", userId1))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLink_differentUser_returnsForbidden() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(delete("/api/links/abc123")
                        .header("Authorization", userId2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Not authorized to delete this link"));
    }

    @Test
    void redirect_validLink_returnsUrl() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(get("/api/links/abc123/redirect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com"));
    }

    @Test
    void redirect_incrementsClickCount() throws Exception {
        createTestLink(userId1, "abc123", "https://example.com");

        mockMvc.perform(get("/api/links/abc123/redirect"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/links/abc123")
                        .header("Authorization", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1));
    }

    @Test
    void redirect_clickLimitReached_returnsGone() throws Exception {
        Link link = createTestLink(userId1, "abc123", "https://example.com");
        link.setClickLimit(2);
        link.setClickCount(2);
        linkRepository.save(link);

        mockMvc.perform(get("/api/links/abc123/redirect"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error", containsString("Click limit reached")));
    }

    @Test
    void redirect_expiredLink_returnsGone() throws Exception {
        Link link = createTestLink(userId1, "abc123", "https://example.com");
        link.setExpiresAt(LocalDateTime.now().minusHours(1));
        linkRepository.save(link);

        mockMvc.perform(get("/api/links/abc123/redirect"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error", containsString("Link has expired")));
    }

    @Test
    void redirect_notFound_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/links/nonexistent/redirect"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Link not found")));
    }

    private Link createTestLink(String userId, String shortCode, String url) {
        User user = usersRepository.findById(userId).orElseGet(() -> {
            User u = new User();
            u.setId(userId);
            return usersRepository.save(u);
        });

        Link link = new Link();
        link.setShortCode(shortCode);
        link.setOriginalUrl(url);
        link.setUser(user);
        link.setClickLimit(10);
        link.setExpiresAt(LocalDateTime.now().plusHours(24));
        return linkRepository.save(link);
    }
}
