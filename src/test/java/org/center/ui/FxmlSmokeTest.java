package org.center.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * 手動端對端檢查：啟動 JavaFX toolkit，實際載入每個 FXML（會跑 controller.initialize()，
 * 進而打到資料庫），確認 fx:id / onAction / controller 綁定沒問題。
 *
 * <p>需要可連線的資料庫與 GUI 環境，因此預設不執行。開啟方式：
 * {@code mvn test -Dfxml.smoke=true -Dtest=FxmlSmokeTest}
 */
@EnabledIfSystemProperty(named = "fxml.smoke", matches = "true")
class FxmlSmokeTest {

    private static final String[] FXMLS = {
            "/fxml/main-menu.fxml",
            "/fxml/dashboard.fxml",
            "/fxml/analytics.fxml",
            "/fxml/course-list.fxml",
            "/fxml/person-list.fxml",
            "/fxml/enrollment-list.fxml",
            "/fxml/enrollment-form.fxml",
            "/fxml/alert-list.fxml",
            "/fxml/audit-log.fxml",
            "/fxml/course-form.fxml",
    };

    @Test
    void everyScreenLoads() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        started.await(10, TimeUnit.SECONDS);

        for (String fxml : FXMLS) {
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    new FXMLLoader(getClass().getResource(fxml)).load();
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    done.countDown();
                }
            });
            done.await(20, TimeUnit.SECONDS);
            if (error.get() != null) {
                fail("載入 " + fxml + " 失敗：" + error.get(), error.get());
            }
        }
    }
}
