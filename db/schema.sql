-- ============================================================
-- 中心營運分析系統 — Database Schema (DRAFT v2)
-- 依 期末專題_分工與Git協作計畫.md 命名規則：資料表/欄位一律 snake_case
-- 狀態：草稿，已比對「中心電腦系統功能需求說明.docx」與課程作業書調整，
--       仍有幾處標記 [待確認]，需與 B / 老師確認後才算定案
-- 正規化：目標 3NF
--
-- 表格總覽（10 張，超過老師要求的「至少 8 張」）：
--   people, accounts, courses, course_prerequisites, enrollments,
--   contact_records, books, inventory_transactions, alerts, audit_logs
--
-- 設計決策記錄（非文件明文規定，屬本專案自行判斷，詳見對話討論）：
--   1. accounts 為新增表，與 people 分離：people 是中心的會員/學員資料，
--      accounts 是系統操作人員登入帳號，兩者概念不同（需求文件 FR-001/002 也是分開的）。
--   2. course_prerequisites 為新增表，滿足「課程先修關係」需求，
--      並直接對應 B 的 CustomGraph（BFS/DFS/拓撲排序偵測循環）。
--   3. people 刻意不建「是否曾服用精神科藥物」「品格狀況」欄位——
--      文件本身要求這類敏感欄位不得用於評分/排序/預測，一週衝刺專案不處理這類個資以降低風險。
--   4. alerts 欄位無文件明文規定，是依「通知中心」行為描述反推設計，非照抄需求。
--   5. 狀態欄位一律不做硬刪除（DELETE），用 status 欄位表示作廢/停用。
--
-- 執行方式：本檔案不寫死資料庫名稱，執行前請先在你的 client 選好/切換到目標資料庫
--   （本機開發：先 CREATE DATABASE 一個自己取的名稱再 USE；
--    老師的共用 server：老師已分配好資料庫名稱，直接 USE 該資料庫即可，
--    帳號通常沒有 CREATE DATABASE 權限）。
-- ============================================================

-- ------------------------------------------------------------
-- 1. accounts：系統操作帳號（登入用，對應老師文件 FR-001/FR-002，
--    與 people 分離；角色權限本專案僅做簡化版，非老師文件要求的完整 RBAC）
-- ------------------------------------------------------------
CREATE TABLE accounts (
    account_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)     NOT NULL UNIQUE,
    password_hash  VARCHAR(255)    NOT NULL,
    role           VARCHAR(20)     NOT NULL,   -- 'admin' / 'registrar' / 'finance' / 'central_file' / 'classroom' / 'bookstore'  [待確認：是否需要更多角色]
    person_id      BIGINT UNSIGNED,            -- 若此帳號同時也是 people 裡的人員，可選填關聯
    is_active      BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_login_count INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at  DATETIME,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- fk_accounts_person 待 people 表建立後，於下方用 ALTER TABLE 補上（避免循環相依）
);

-- ------------------------------------------------------------
-- 2. people：中心人員主檔（會員/學員，非系統登入帳號）
-- ------------------------------------------------------------
CREATE TABLE people (
    person_id       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,   -- 穩定不變的唯一編號，不可用姓名/電話當 key
    start_date      DATE,                    -- 開始接觸日期
    contact_source  VARCHAR(50),             -- 接觸來源
    referrer_name   VARCHAR(100),            -- FSM：引導進來上課的人名，自己來的可為 NULL
    name            VARCHAR(100)    NOT NULL,
    gender          VARCHAR(10),
    occupation      VARCHAR(50),
    office_phone    VARCHAR(20),
    home_phone      VARCHAR(20),
    mobile_phone    VARCHAR(20),
    email           VARCHAR(100),
    line_id         VARCHAR(50),
    birthday        DATE,
    interests       VARCHAR(255),
    contactable     BOOLEAN         NOT NULL DEFAULT TRUE,   -- 可聯絡否：名單產製的強制篩選條件
    mailable        BOOLEAN         NOT NULL DEFAULT TRUE,   -- 可寄信否：同上
    preferred_channel VARCHAR(20),           -- 偏好聯絡管道
    stop_contact_reason VARCHAR(255),        -- 停止聯絡原因
    note            TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'active',  -- 'active' / 'inactive'（作廢用狀態，不硬刪除）
    entered_by      BIGINT UNSIGNED,          -- 輸入者 -> accounts.account_id
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_entered_by
        FOREIGN KEY (entered_by) REFERENCES accounts(account_id),
    INDEX idx_people_phone_email (mobile_phone, email)   -- 查重/查找用
);

ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_person
    FOREIGN KEY (person_id) REFERENCES people(person_id);

-- ------------------------------------------------------------
-- 3. courses：課程
-- ------------------------------------------------------------
CREATE TABLE courses (
    course_id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL,
    instructor_id       BIGINT UNSIGNED,      -- -> people.person_id
    capacity            INT UNSIGNED    NOT NULL DEFAULT 0,
    payment_type        VARCHAR(20),          -- [待確認：正式列舉值]
    class_time_slot     VARCHAR(50),          -- 上課時段
    classroom           VARCHAR(50),          -- 教室/座位
    start_date          DATE,
    completion_date     DATE,
    first_class_date    DATE,                 -- 首次進教室日期
    status              VARCHAR(20)     NOT NULL DEFAULT 'planned', -- 'planned' / 'ongoing' / 'ended' / 'suspended'
    suspend_reason      VARCHAR(255),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_instructor
        FOREIGN KEY (instructor_id) REFERENCES people(person_id)
);

-- ------------------------------------------------------------
-- 4. course_prerequisites：課程先修關係（自我參照多對多，供 CustomGraph 使用）
-- ------------------------------------------------------------
CREATE TABLE course_prerequisites (
    course_id             BIGINT UNSIGNED NOT NULL,
    prerequisite_course_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (course_id, prerequisite_course_id),
    CONSTRAINT fk_prereq_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT fk_prereq_prerequisite
        FOREIGN KEY (prerequisite_course_id) REFERENCES courses(course_id),
    CONSTRAINT chk_prereq_not_self CHECK (course_id <> prerequisite_course_id)
);

-- ------------------------------------------------------------
-- 5. enrollments：報名/選課紀錄（people <-> courses 多對多）
--    status 涵蓋漏斗分析所需階段：接觸→接受介紹→完成註冊→開始上課→完成課程
-- ------------------------------------------------------------
CREATE TABLE enrollments (
    enrollment_id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    person_id        BIGINT UNSIGNED NOT NULL,
    course_id        BIGINT UNSIGNED NOT NULL,
    registrar_id     BIGINT UNSIGNED,          -- 負責註冊員 -> accounts.account_id
    amount           DECIMAL(10,2)   DEFAULT 0,
    payment_type     VARCHAR(20),
    enrolled_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attendance_date DATE,                 -- 供風險/預警分析使用
    status           VARCHAR(20)     NOT NULL DEFAULT 'contacted',
        -- 漏斗階段：'contacted' / 'introduced' / 'registered' / 'started' / 'completed' / 'cancelled'
    cancel_reason    VARCHAR(255),
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollments_person
        FOREIGN KEY (person_id) REFERENCES people(person_id),
    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT fk_enrollments_registrar
        FOREIGN KEY (registrar_id) REFERENCES accounts(account_id),
    CONSTRAINT uq_enrollments_person_course UNIQUE (person_id, course_id),
    INDEX idx_enrollments_course (course_id),
    INDEX idx_enrollments_status (status)   -- 漏斗分析
);

-- ------------------------------------------------------------
-- 6. contact_records：聯絡/追蹤紀錄（用於流失風險分析）
-- ------------------------------------------------------------
CREATE TABLE contact_records (
    contact_id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    person_id           BIGINT UNSIGNED NOT NULL,
    contact_date        DATETIME        NOT NULL,
    method              VARCHAR(20)     NOT NULL,  -- 'phone' / 'email' / 'line' / 'letter' / 'in_person'
    content             TEXT,
    mood_rating         TINYINT,                   -- 情緒度 [待確認：量表範圍]
    result              VARCHAR(50),                -- 聯絡結果
    follow_up_action    VARCHAR(255),
    next_contact_date   DATE,
    created_by          BIGINT UNSIGNED,            -- 承辦人員 -> accounts.account_id
    CONSTRAINT fk_contact_person
        FOREIGN KEY (person_id) REFERENCES people(person_id),
    CONSTRAINT fk_contact_created_by
        FOREIGN KEY (created_by) REFERENCES accounts(account_id),
    INDEX idx_contact_next_followup (next_contact_date)   -- 待辦/逾期查詢
);

-- ------------------------------------------------------------
-- 7. books：書籍主檔
-- ------------------------------------------------------------
CREATE TABLE books (
    book_id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(200)    NOT NULL,
    isbn               VARCHAR(20),
    author             VARCHAR(100),
    category           VARCHAR(50),
    supplier           VARCHAR(100),         -- 來源書局/供應商（文字欄位，未獨立成表）
    cost               DECIMAL(10,2),
    list_price         DECIMAL(10,2),
    storage_location   VARCHAR(50),
    safety_stock       INT UNSIGNED    NOT NULL DEFAULT 0,
    current_stock      INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 8. inventory_transactions：進銷存交易（進貨/銷售/退貨/盤點調整，用 type 區分）
--    需求：銷售需 commit/rollback（扣庫存+產生紀錄，庫存不足需 rollback）
-- ------------------------------------------------------------
CREATE TABLE inventory_transactions (
    transaction_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    book_id            BIGINT UNSIGNED NOT NULL,
    transaction_type   VARCHAR(20)     NOT NULL,  -- 'purchase' / 'sale' / 'return' / 'adjustment'
    person_id          BIGINT UNSIGNED,           -- 購買人（sale 時使用），可為 NULL
    supplier           VARCHAR(100),              -- 供應商（purchase 時使用）
    quantity           INT             NOT NULL,
    unit_price         DECIMAL(10,2),
    discount           DECIMAL(10,2)   DEFAULT 0,
    net_amount         DECIMAL(10,2),             -- 實收/實付金額
    invoice_type       VARCHAR(10),               -- 二聯/三聯（sale 用）[待確認]
    invoice_number     VARCHAR(30),
    document_number    VARCHAR(30),               -- 進貨單據號碼（purchase 用）
    inspection_status  VARCHAR(20)     DEFAULT 'pending',  -- 驗收狀態：'pending' / 'accepted' / 'rejected'
    transaction_date   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status             VARCHAR(20)     NOT NULL DEFAULT 'completed', -- 'completed' / 'reversed'
    note               VARCHAR(255),
    CONSTRAINT fk_inventory_book
        FOREIGN KEY (book_id) REFERENCES books(book_id),
    CONSTRAINT fk_inventory_person
        FOREIGN KEY (person_id) REFERENCES people(person_id),
    INDEX idx_inventory_book_date (book_id, transaction_date)   -- 期間彙總
);

-- ------------------------------------------------------------
-- 9. alerts：系統警示（用自訂 Heap 依 severity 排優先序輸出）
--    欄位無文件明文規定，依「通知中心」行為描述設計
-- ------------------------------------------------------------
CREATE TABLE alerts (
    alert_id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    alert_type         VARCHAR(30)     NOT NULL,  -- 'low_stock' / 'overdue_contact' / 'incomplete_course' / 'data_quality'
    severity           INT             NOT NULL,  -- 數字越大優先度越高，供 MaxHeap 使用
    due_date           DATE,
    priority_tier      VARCHAR(20),               -- '正常' / '注意' / '優先處理'
    source_table       VARCHAR(50),               -- 觸發來源資料表（多型參照，非外鍵）
    source_id          BIGINT UNSIGNED,
    related_person_id  BIGINT UNSIGNED,
    trigger_reason     VARCHAR(255)    NOT NULL,
    message            VARCHAR(255)    NOT NULL,
    status             VARCHAR(20)     NOT NULL DEFAULT 'open',  -- 'open' / 'assigned' / 'resolved' / 'snoozed'
    assigned_to        BIGINT UNSIGNED,           -- -> accounts.account_id
    read_at            DATETIME,
    snoozed_until      DATETIME,
    created_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at        DATETIME,
    CONSTRAINT fk_alerts_person
        FOREIGN KEY (related_person_id) REFERENCES people(person_id),
    CONSTRAINT fk_alerts_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES accounts(account_id),
    INDEX idx_alerts_status_severity (status, severity)   -- Heap 來源查詢
);

-- ------------------------------------------------------------
-- 10. audit_logs：稽核紀錄（一般使用者不得修改/刪除，僅能新增）
-- ------------------------------------------------------------
CREATE TABLE audit_logs (
    log_id       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    actor_id     BIGINT UNSIGNED,             -- -> accounts.account_id，系統自動觸發時可為 NULL
    action       VARCHAR(20)     NOT NULL,    -- 'CREATE' / 'UPDATE' / 'VOID' / 'EXPORT' / 'APPROVE'
    table_name   VARCHAR(50)     NOT NULL,
    record_id    BIGINT UNSIGNED,
    field_name   VARCHAR(50),
    old_value    TEXT,
    new_value    TEXT,
    reason       VARCHAR(255),
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_id) REFERENCES accounts(account_id)
);

-- 索引（非主鍵，滿足「至少3個非主鍵索引」要求，並附效益說明見報告）：
-- 已改為寫在各自 CREATE TABLE 內（idx_people_phone_email、idx_enrollments_course、
-- idx_enrollments_status、idx_contact_next_followup、idx_inventory_book_date、
-- idx_alerts_status_severity），避免事後補加索引需要額外的 INDEX 權限
-- （老師的共用 server 帳號只給 CREATE，沒給 INDEX，事後 CREATE INDEX 會被拒絕）。
