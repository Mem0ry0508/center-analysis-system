package org.center.ui;

/**
 * IDE 直接執行用的進入點：不繼承 Application，繞開 JavaFX 對「主類別是否為 Application 子類別」的
 * module path 檢查，讓 IntelliJ 用一般 classpath 直接跑也不會噴 "JavaFX runtime components are missing"。
 * `mvn javafx:run` 不受影響，繼續用 pom.xml 設定的 org.center.ui.MainApp。
 */
public final class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
