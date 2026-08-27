package org.center.service;

/**
 * 加入某條先修關係後，課程先修圖會形成循環時拋出，供 UI 攔截並提示使用者（不寫入資料庫）。
 */
public class PrerequisiteCycleException extends RuntimeException {

    public PrerequisiteCycleException(String message) {
        super(message);
    }
}
