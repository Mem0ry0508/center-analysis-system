package org.center.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.center.algorithm.MergeSort;
import org.center.analytics.CoursePathReport;
import org.center.analytics.FunnelReport;
import org.center.analytics.FunnelStage;
import org.center.analytics.PersonDirectory;
import org.center.analytics.RiskEntry;
import org.center.model.Course;
import org.center.model.Person;
import org.center.service.AnalyticsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 營運分析畫面：報名漏斗（第二階段）、流失風險名單（第二階段）＋ MaxHeap 優先名單（第三階段）、
 * 課程先修路徑（CustomGraph）、搜尋效能對照（MergeSort / BinarySearch / LinearSearch）。
 * 只透過 {@link AnalyticsService} 取資料，不直接碰 Repository 或資料結構內部。
 */
public class AnalyticsController {

    @FXML private TableView<FunnelStage> funnelTable;
    @FXML private TableColumn<FunnelStage, String> funnelStageColumn;
    @FXML private TableColumn<FunnelStage, String> funnelCountColumn;
    @FXML private TableColumn<FunnelStage, String> funnelConversionColumn;
    @FXML private TableColumn<FunnelStage, String> funnelDropColumn;
    @FXML private Label funnelSummaryLabel;

    @FXML private TableView<RiskEntry> riskTable;
    @FXML private TableColumn<RiskEntry, String> riskIdColumn;
    @FXML private TableColumn<RiskEntry, String> riskNameColumn;
    @FXML private TableColumn<RiskEntry, String> riskScoreColumn;
    @FXML private TableColumn<RiskEntry, String> riskContactColumn;
    @FXML private TableColumn<RiskEntry, String> riskAttendanceColumn;
    @FXML private TableColumn<RiskEntry, String> riskReasonColumn;
    @FXML private Spinner<Integer> topNSpinner;
    @FXML private TextField jumpIdField;
    @FXML private Label riskStatusLabel;

    @FXML private Label coursePathStatusLabel;
    @FXML private ListView<String> coursePathList;

    @FXML private Label perfResultLabel;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final ObservableList<FunnelStage> funnelStages = FXCollections.observableArrayList();
    private final ObservableList<RiskEntry> riskEntries = FXCollections.observableArrayList();
    private List<RiskEntry> allRiskEntries = List.of();

    @FXML
    private void initialize() {
        funnelStageColumn.setCellValueFactory(c -> new SimpleStringProperty(stageLabel(c.getValue().getStageName())));
        funnelCountColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCount())));
        funnelConversionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConversionText()));
        funnelDropColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getDropOff())));
        funnelTable.setItems(funnelStages);

        riskIdColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPersonId())));
        riskNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPersonName()));
        riskScoreColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getRiskScore())));
        riskContactColumn.setCellValueFactory(c -> new SimpleStringProperty(days(c.getValue().getDaysSinceLastContact())));
        riskAttendanceColumn.setCellValueFactory(
                c -> new SimpleStringProperty(days(c.getValue().getDaysSinceLastAttendance())));
        riskReasonColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReasonText()));
        riskTable.setItems(riskEntries);

        topNSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 10));

        loadAll();
    }

    @FXML
    private void handleRefresh() {
        loadAll();
    }

    private void loadAll() {
        try {
            FunnelReport funnel = analyticsService.enrollmentFunnel();
            funnelStages.setAll(funnel.getStages());
            funnelSummaryLabel.setText(String.format(
                    "已取消報名：%d 筆　｜　整體轉換率（completed / contacted）：%.1f%%",
                    funnel.getCancelledCount(), funnel.getOverallConversion()));

            allRiskEntries = analyticsService.riskTable();
            riskEntries.setAll(allRiskEntries);
            riskStatusLabel.setText("共 " + allRiskEntries.size() + " 位在冊學員");

            loadCoursePath();
            perfResultLabel.setText("尚未執行");
        } catch (RuntimeException e) {
            showError("讀取分析資料失敗：" + rootMessage(e));
        }
    }

    private void loadCoursePath() {
        CoursePathReport report = analyticsService.coursePath();
        coursePathStatusLabel.getStyleClass().remove("error-label");
        if (report.isHasCycle()) {
            coursePathStatusLabel.getStyleClass().add("error-label");
            coursePathStatusLabel.setText("⚠ " + report.getCycleHint());
            coursePathList.getItems().clear();
            return;
        }
        coursePathStatusLabel.setText("建議修課順序（拓撲排序），共 " + report.getRecommendedOrder().size() + " 門課：");
        List<String> lines = new ArrayList<>();
        int index = 1;
        for (Course course : report.getRecommendedOrder()) {
            lines.add(index++ + ". " + course.getName());
        }
        coursePathList.getItems().setAll(lines);
    }

    @FXML
    private void handleSortByScore() {
        resort(Comparator.comparingInt(RiskEntry::getRiskScore).reversed());
    }

    @FXML
    private void handleSortById() {
        resort(Comparator.comparingLong(RiskEntry::getPersonId));
    }

    @FXML
    private void handleSortByName() {
        resort(Comparator.comparing(RiskEntry::getPersonName));
    }

    private void resort(Comparator<RiskEntry> comparator) {
        List<RiskEntry> working = new ArrayList<>(riskEntries);
        MergeSort.sort(working, comparator);
        riskEntries.setAll(working);
    }

    @FXML
    private void handleShowTopN() {
        int n = topNSpinner.getValue() == null ? 10 : topNSpinner.getValue();
        riskEntries.setAll(analyticsService.topRisk(allRiskEntries, n));
        riskStatusLabel.setText("顯示風險最高的前 " + riskEntries.size() + " 位（MaxHeap）");
    }

    @FXML
    private void handleShowAll() {
        riskEntries.setAll(allRiskEntries);
        riskStatusLabel.setText("共 " + allRiskEntries.size() + " 位在冊學員");
    }

    @FXML
    private void handleJump() {
        String text = jumpIdField.getText() == null ? "" : jumpIdField.getText().trim();
        long id;
        try {
            id = Long.parseLong(text);
        } catch (NumberFormatException e) {
            riskStatusLabel.setText("請輸入數字編號");
            return;
        }
        var person = analyticsService.jumpToPerson(id);
        if (person.isEmpty()) {
            riskStatusLabel.setText("查無編號 " + id + " 的人員");
            return;
        }
        for (RiskEntry entry : riskEntries) {
            if (entry.getPersonId() == id) {
                riskTable.getSelectionModel().select(entry);
                riskTable.scrollTo(entry);
                riskStatusLabel.setText("已定位：#" + id + " " + person.get().getName());
                return;
            }
        }
        riskStatusLabel.setText("#" + id + " " + person.get().getName() + "（不在目前名單，可按「全部」）");
    }

    @FXML
    private void handlePerfCompare() {
        try {
            PersonDirectory directory = analyticsService.personDirectory();
            List<Person> people = directory.sortedById();
            int size = people.size();
            if (size == 0) {
                perfResultLabel.setText("沒有人員資料");
                return;
            }
            int rounds = 200;
            long linearNs = timeSearch(directory, people, rounds, true);
            long binaryNs = timeSearch(directory, people, rounds, false);
            double linearPer = linearNs / 1000.0 / (rounds * (double) size);
            double binaryPer = binaryNs / 1000.0 / (rounds * (double) size);
            perfResultLabel.setText(String.format(
                    "資料筆數 %d，重複 %d 輪：LinearSearch 平均 %.3f µs／次；BinarySearch 平均 %.3f µs／次（約快 %.1f 倍）",
                    size, rounds, linearPer, binaryPer, binaryPer == 0 ? 0 : linearPer / binaryPer));
        } catch (RuntimeException e) {
            showError("效能對照失敗：" + rootMessage(e));
        }
    }

    private long timeSearch(PersonDirectory directory, List<Person> people, int rounds, boolean linear) {
        long start = System.nanoTime();
        for (int r = 0; r < rounds; r++) {
            for (Person person : people) {
                long id = person.getPersonId();
                if (linear) {
                    directory.findByIdLinear(id);
                } else {
                    directory.findById(id);
                }
            }
        }
        return System.nanoTime() - start;
    }

    @FXML
    private void handleBack() {
        SceneRouter.show("/fxml/main-menu.fxml", "中心營運分析系統 - 主選單");
    }

    private static String stageLabel(String stage) {
        return switch (stage) {
            case "contacted" -> "接觸";
            case "introduced" -> "接受介紹";
            case "registered" -> "完成註冊";
            case "started" -> "開始上課";
            case "completed" -> "完成課程";
            default -> stage;
        };
    }

    private static String days(Integer value) {
        return value == null ? "—" : value + " 天";
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }
}
