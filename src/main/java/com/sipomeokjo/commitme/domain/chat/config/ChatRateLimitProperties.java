package com.sipomeokjo.commitme.domain.chat.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.chat.rate-limit")
public class ChatRateLimitProperties {

    private boolean enabled = true;
    private int maxRequests = 20;
    private Duration window = Duration.ofSeconds(10);
    private String keyPrefix = ":chat:local:rate-limit:send";
}
