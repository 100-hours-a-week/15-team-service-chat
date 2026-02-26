package com.sipomeokjo.commitme.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.loadtest")
public class LoadtestProperties {

    private boolean enabled = false;
}
