package src.lab.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.lab.schemas.CreateLinkRequest;
import src.lab.schemas.LinkResponse;
import src.lab.schemas.RedirectResponse;
import src.lab.schemas.UpdateLinkRequest;
import src.lab.services.LinksService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/links")
public class LinksController {

    private final LinksService linksService;

    public LinksController(LinksService linksService) {
        this.linksService = linksService;
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@RequestAttribute String userId, @Valid @RequestBody CreateLinkRequest request) {
        var link = linksService.createShortLink(userId, request.getUrl(), request.getClickLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(LinkResponse.from(link));
    }

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getUserLinks(@RequestAttribute String userId) {
        List<LinkResponse> links = linksService.getUserLinks(userId).stream().map(LinkResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(links);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<LinkResponse> getLink(@PathVariable String shortCode) {
        var link = linksService.getLink(shortCode);
        return ResponseEntity.ok(LinkResponse.from(link));
    }

    @PatchMapping("/{shortCode}")
    public ResponseEntity<LinkResponse> updateLink(@PathVariable String shortCode, @RequestAttribute String userId, @Valid @RequestBody UpdateLinkRequest request) {
        var link = linksService.updateLink(shortCode, userId, request.getClickLimit());
        return ResponseEntity.ok(LinkResponse.from(link));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode, @RequestAttribute String userId) {
        linksService.deleteLink(shortCode, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/redirect")
    public ResponseEntity<RedirectResponse> redirect(@PathVariable String shortCode) {
        String url = linksService.redirect(shortCode);
        return ResponseEntity.ok(RedirectResponse.of(url));
    }
}
