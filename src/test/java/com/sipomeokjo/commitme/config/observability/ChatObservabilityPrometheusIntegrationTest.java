package com.sipomeokjo.commitme.config.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = ChatObservabilityPrometheusIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:chat-observability;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.hikari.pool-name=chatObservabilityTestPool",
            "spring.datasource.hikari.register-mbeans=true",
            "management.endpoints.web.exposure.include=health,info,metrics,prometheus",
            "management.endpoint.prometheus.enabled=true",
            "management.metrics.enable.hikari=true",
            "management.metrics.distribution.percentiles-histogram.jdbc.query.execution=true",
            "app.chat.observability.datasource-proxy.enabled=true",
            "app.chat.observability.datasource-proxy.proxy-name=chat-observability-test-proxy",
            "app.chat.observability.datasource-proxy.datasource-tag=chat-test",
            "app.chat.observability.datasource-proxy.slow-query-threshold=200ms",
            "app.chat.observability.datasource-proxy.slow-query-log-enabled=false"
        })
class ChatObservabilityPrometheusIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void actuatorPrometheus_containsJvmHikariAndJdbcMetrics() throws Exception {
        mockMvc.perform(post("/test/observability/fast")).andExpect(status().isNoContent());
        mockMvc.perform(post("/test/observability/slow")).andExpect(status().isNoContent());

        String prometheusBody =
                mockMvc.perform(get("/actuator/prometheus"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        assertThat(prometheusBody).contains("jvm_memory_used_bytes");
        assertThat(prometheusBody).contains("hikaricp_connections_active");
        assertThat(prometheusBody).contains("jdbc_query_execution_seconds_count");
        assertThat(prometheusBody).contains("jdbc_query_slow_total");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class,
                FlywayAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
            })
    @EnableConfigurationProperties(ChatObservabilityProperties.class)
    @Import({ChatDataSourceProxyMetricsConfig.class, TestQueryController.class})
    static class TestApplication {}

    @RestController
    @RequestMapping("/test/observability")
    static class TestQueryController {

        private final JdbcTemplate jdbcTemplate;

        TestQueryController(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @PostConstruct
        void initialize() {
            jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS SLEEP FOR \"java.lang.Thread.sleep\"");
        }

        @PostMapping("/fast")
        ResponseEntity<Void> fastQuery() {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/slow")
        ResponseEntity<Void> slowQuery() {
            jdbcTemplate.execute("CALL SLEEP(250)");
            return ResponseEntity.noContent().build();
        }
    }
}
