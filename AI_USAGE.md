# AI 使用紀錄

依照分工計畫第 7.3 節，記錄兩人使用 AI 生成較大段程式碼的紀錄。口頭答辯前，兩人都要能不看稿逐段講解自己模組的程式碼與複雜度分析，AI 生成不等於可以不懂。

| 日期 | 使用者 | 模組 | AI 用途(查語法/除錯/生成雛型/測試想法) | 是否經本人理解並修改 |
|---|---|---|---|---|
| 2026-08-25 | B | datastructure/CustomHashTable | 生成雛型(separate chaining hash table)＋測試想法(碰撞、resize、null key 邊界情況) | 是 |
| 2026-08-25 | B | datastructure/CustomGraph | 生成雛型(adjacency list、BFS/DFS、Kahn's 拓撲排序、DFS 三色環偵測)＋測試想法 | 是 |
| 2026-08-25 | B | datastructure/MinHeap、MaxHeap | 生成雛型(共用 BinaryHeap base、sift up/down)＋測試想法 | 是 |
| 2026-08-25 | B | algorithm/MergeSort | 生成雛型(top-down 遞迴分治、stable merge)＋測試想法(穩定性驗證) | 是 |
| 2026-08-25 | B | algorithm/BinarySearch、LinearSearch | 生成雛型＋測試想法(邊界情況：空 list、單一元素、重複值) | 是 |
| 2026-08-26 | B | 專案文件(pr_description_*.txt) | 協助撰寫 PR 說明稿(整理已完成程式碼的設計決策與 review 重點) | 是 |
| 2026-08-27 | B | analytics/ 全套 + service/AnalyticsService + ui/AnalyticsController | 生成雛型(第一~三階段分析器、把 CustomHashTable/MaxHeap/CustomGraph/MergeSort/BinarySearch/LinearSearch 接進真實呼叫路徑)＋單元測試 | 是 |
| 2026-08-27 | B | service/CourseService.addPrerequisite + ui/CourseFormController 先修課程區塊 | 生成雛型(寫入前用 CustomGraph 偵測循環、UI 攔截 PrerequisiteCycleException) | 是 |
| 2026-08-27 | B | service/AlertGenerationService + AlertService.findOpenAlertsByDueDate | 生成雛型(掃描低庫存/逾期回訪/逾期課程產生警示，CustomHashTable 去重；MinHeap 依到期日排序) ＋單元測試 | 是 |
| 2026-08-27 | B | service/EnrollmentService + ui/Enrollment 報名管理畫面 | 生成雛型(報名 CRUD、漏斗狀態、取消報名軟刪除) ＋單元測試 | 是 |
| 2026-08-27 | B | service/AuditService + util/AuditContext + ui/AuditLogController | 生成雛型(主資料異動寫 append-only 稽核、唯讀檢視畫面) | 是 |
| 2026-08-27 | B | util/PerformanceBenchmark | 生成雛型(100/1000/10000 筆 MergeSort / BinarySearch vs LinearSearch / HashTable vs 線性掃描 對照) | 是 |
| 2026-08-27 | B | ui/PersonListController | 生成雛型(名單排序改呼叫 MergeSort、跳號查找改呼叫 BinarySearch) | 是 |

> 待補：成員 A 的 model/repository/schema.sql 相關 AI 使用紀錄，請 A 自行補上對應列。
