package com.sipomeokjo.commitme.config.observability;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.chat.observability")
public class ChatObservabilityProperties {

    private final DatasourceProxy datasourceProxy = new DatasourceProxy();

    @Getter
    @Setter
    public static class DatasourceProxy {

        private boolean enabled = true;
        private Duration slowQueryThreshold = Duration.ofMillis(300);
        private String proxyName = "chat-data-source-proxy";
        private String datasourceTag = "chat-mysql";
        private boolean slowQueryLogEnabled = true;
    }
}
