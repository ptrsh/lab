package src.lab.schemas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLinkRequest {
    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL is too long (max 2048 characters)")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    private Integer clickLimit;
}
