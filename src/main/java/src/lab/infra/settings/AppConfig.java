package src.lab.infra.settings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    private LinkConfig link = new LinkConfig();
    private CleanupConfig cleanup = new CleanupConfig();

    @Data
    public static class LinkConfig {
        private int ttlHours;
        private int defaultClickLimit;
        private int shortCodeLength;
    }

    @Data
    public static class CleanupConfig {
        private int rateMinutes;
    }
}
