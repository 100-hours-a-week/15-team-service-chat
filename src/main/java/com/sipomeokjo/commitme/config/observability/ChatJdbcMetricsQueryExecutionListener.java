package com.sipomeokjo.commitme.config.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

public class ChatJdbcMetricsQueryExecutionListener implements QueryExecutionListener {

    public static final String JDBC_QUERY_EXECUTION_TIMER = "jdbc.query.execution";
    public static final String JDBC_QUERY_SLOW_COUNTER = "jdbc.query.slow";
    private static final Duration DEFAULT_SLOW_QUERY_THRESHOLD = Duration.ofMillis(300);
    private static final String DEFAULT_DATASOURCE_TAG = "chat-mysql";

    private final MeterRegistry meterRegistry;
    private final Duration slowQueryThreshold;
    private final String datasourceTag;

    public ChatJdbcMetricsQueryExecutionListener(
            MeterRegistry meterRegistry, Duration slowQueryThreshold, String datasourceTag) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        this.slowQueryThreshold =
                slowQueryThreshold == null || slowQueryThreshold.isNegative()
                        ? DEFAULT_SLOW_QUERY_THRESHOLD
                        : slowQueryThreshold;
        this.datasourceTag =
                datasourceTag == null || datasourceTag.isBlank()
                        ? DEFAULT_DATASOURCE_TAG
                        : datasourceTag;
    }

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> queryInfoList) {
        // no-op
    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> queryInfoList) {
        if (executionInfo == null) {
            return;
        }

        long elapsedTimeMillis = Math.max(0L, executionInfo.getElapsedTime());
        Tags tags = buildTags(executionInfo);

        Timer.builder(JDBC_QUERY_EXECUTION_TIMER)
                .description("JDBC query execution time collected via datasource-proxy")
                .tags(tags)
                .register(meterRegistry)
                .record(elapsedTimeMillis, TimeUnit.MILLISECONDS);

        if (elapsedTimeMillis >= slowQueryThreshold.toMillis()) {
            Counter.builder(JDBC_QUERY_SLOW_COUNTER)
                    .description("Slow JDBC query count collected via datasource-proxy")
                    .tags(tags)
                    .register(meterRegistry)
                    .increment();
        }
    }

    private Tags buildTags(ExecutionInfo executionInfo) {
        String statementType =
                executionInfo.getStatementType() == null
                        ? "unknown"
                        : executionInfo.getStatementType().name().toLowerCase(Locale.ROOT);

        return Tags.of(
                "datasource",
                datasourceTag,
                "success",
                Boolean.toString(executionInfo.isSuccess()),
                "batch",
                Boolean.toString(executionInfo.isBatch()),
                "statement_type",
                statementType);
    }
}
