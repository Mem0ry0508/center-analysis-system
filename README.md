# 中心營運分析系統

資料結構與演算法期末作業 — 2人小組。Java 26 + JavaFX 26 + JDBC(MySQL) + Maven。

## 專案結構

```
src/main/java/org/center/
  model/           // A 主導：Entity/DTO
  repository/      // A 主導：JDBC/DAO
  datastructure/    // B 主導：CustomHashTable, Graph, MinHeap
  algorithm/        // B 主導：Sort, Search, BFS/DFS, TopologicalSort
  analytics/        // B 主導：Dashboard, Funnel, Risk
  service/          // 共同：業務邏輯，串接上述模組
  ui/               // B 主導：JavaFX 畫面與 Controller
  util/             // 共同：設定、驗證、測試資料產生器
db/                 // schema.sql、sample_data.sql
```

## 環境需求

- JDK 26（兩人版本需一致，例如都用 26.0.1）
- Maven 3.9+
- MySQL（資料庫課老師已架設 server）

## 第一次設定（兩人都要各自做一次）

1. 複製 `config.properties.example` 為 `config.properties`，填入資料庫連線資訊（此檔已被 `.gitignore` 排除，不會進版控，也不會覆蓋到對方的設定）
2. 確認 JDK 版本：`java -version`（應顯示 26.x）
3. 建置：`mvn clean compile`
4. 執行測試：`mvn test`
5. 執行 JavaFX 程式：`mvn javafx:run`

## Git 分支策略

```
main        # 永遠可執行、可展示，只接受從 develop merge
develop     # 整合分支，每天至少merge一次到這裡跑測試
a/xxx       # A 的功能分支
b/xxx       # B 的功能分支
```

## 已建立的共用契約（Day1 定案，勿隨意修改）

- `datastructure/IHashTable.java`
- `datastructure/IGraph.java`
- `datastructure/IHeap.java`
- `repository/IRepository.java`

如需調整介面簽章，需雙方在群組同步後才可變更。

## AI 使用紀錄

見 `AI_USAGE.md`（首次使用AI生成較大段程式碼時建立）。
