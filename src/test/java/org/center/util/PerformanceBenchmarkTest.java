package org.center.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 效能測試較耗時，預設不執行：{@code mvn test -Dbench=true -Dtest=PerformanceBenchmarkTest}
 * 平時直接跑 {@code PerformanceBenchmark.main} 產生報告用的數據表。
 */
@EnabledIfSystemProperty(named = "bench", matches = "true")
class PerformanceBenchmarkTest {

    @Test
    void benchmarkRunsWithoutError() {
        assertDoesNotThrow(() -> PerformanceBenchmark.main(new String[0]));
    }
}
