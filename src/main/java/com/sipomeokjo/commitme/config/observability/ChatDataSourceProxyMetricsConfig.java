package com.sipomeokjo.commitme.config.observability;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(ChatObservabilityProperties.class)
public class ChatDataSourceProxyMetricsConfig {

    private static final Duration DEFAULT_SLOW_QUERY_THRESHOLD = Duration.ofMillis(300);
    private static final String DEFAULT_PROXY_NAME = "chat-data-source-proxy";

    @Bean(name = "observabilityProxyDataSource")
    @Primary
    @ConditionalOnBean(name = "dataSource")
    @ConditionalOnProperty(
            prefix = "app.chat.observability.datasource-proxy",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public DataSource observabilityProxyDataSource(
            @Qualifier("dataSource") DataSource dataSource,
            MeterRegistry meterRegistry,
            ChatObservabilityProperties observabilityProperties) {
        if (dataSource instanceof ProxyDataSource) {
            return dataSource;
        }

        ChatObservabilityProperties.DatasourceProxy properties =
                observabilityProperties.getDatasourceProxy();
        Duration slowQueryThreshold = resolveSlowQueryThreshold(properties.getSlowQueryThreshold());

        ProxyDataSourceBuilder builder =
                ProxyDataSourceBuilder.create(dataSource)
                        .name(resolveProxyName(properties.getProxyName()))
                        .listener(
                                new ChatJdbcMetricsQueryExecutionListener(
                                        meterRegistry,
                                        slowQueryThreshold,
                                        properties.getDatasourceTag()));

        if (properties.isSlowQueryLogEnabled()) {
            builder.logSlowQueryBySlf4j(
                    Math.max(1L, slowQueryThreshold.toMillis()), TimeUnit.MILLISECONDS);
        }

        return builder.build();
    }

    private Duration resolveSlowQueryThreshold(Duration configured) {
        if (configured == null || configured.isNegative()) {
            return DEFAULT_SLOW_QUERY_THRESHOLD;
        }
        return configured;
    }

    private String resolveProxyName(String proxyName) {
        if (proxyName == null || proxyName.isBlank()) {
            return DEFAULT_PROXY_NAME;
        }
        return proxyName;
    }
}
