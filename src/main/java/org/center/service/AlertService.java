package org.center.service;

import org.center.datastructure.IHeap;
import org.center.datastructure.MaxHeap;
import org.center.model.Alert;
import org.center.repository.AlertRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService() {
        this(new AlertRepository());
    }

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<Alert> findAll() {
        return alertRepository.findAll();
    }

    /**
     * 未結案警示依 severity 用 MaxHeap 建堆輸出，數字越大越優先（對應 db/schema.sql alerts.severity 註解）。
     */
    public List<Alert> findOpenAlertsByPriority() {
        return orderByPriority(alertRepository.findOpenAlerts());
    }

    static List<Alert> orderByPriority(List<Alert> alerts) {
        IHeap<Alert> heap = new MaxHeap<>(Comparator.comparingInt(Alert::getSeverity));
        for (Alert alert : alerts) {
            heap.insert(alert);
        }
        List<Alert> ordered = new ArrayList<>(alerts.size());
        while (!heap.isEmpty()) {
            ordered.add(heap.remove());
        }
        return ordered;
    }

    public Alert save(Alert alert) {
        if (alert.getAlertId() == null) {
            return alertRepository.save(alert);
        }
        alertRepository.update(alert);
        return alert;
    }

    public boolean resolve(Long id) {
        return alertRepository.deleteById(id);
    }
}
