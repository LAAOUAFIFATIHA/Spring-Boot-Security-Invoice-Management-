package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.service.SystemMonitoringService;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SystemMonitoringController {

    private final SystemMonitoringService systemMonitoringService;
    private final MeterRegistry meterRegistry;

    /**
     * Retrieves key system metrics from Actuator.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Map<String, Object> response = new HashMap<>();

        response.put("jvm_memory_used", getMetricValue("jvm.memory.used"));
        response.put("jvm_memory_max", getMetricValue("jvm.memory.max"));
        response.put("jvm_threads_live", getMetricValue("jvm.threads.live"));
        response.put("process_cpu_usage", getMetricValue("process.cpu.usage"));
        response.put("system_cpu_usage", getMetricValue("system.cpu.usage"));
        response.put("uptime", getMetricValue("process.uptime"));

        return ResponseEntity.ok(response);
    }

    /**
     * Performs a port scan on the server.
     */
    @GetMapping("/scan-ports")
    public ResponseEntity<List<SystemMonitoringService.PortStatus>> scanPorts(
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "10000") int end) {
        // Limit range for safety and performance
        int rangeStart = Math.max(1, start);
        int rangeEnd = Math.min(65535, Math.min(rangeStart + 2000, end));

        return ResponseEntity.ok(systemMonitoringService.scanLocalPorts(rangeStart, rangeEnd));
    }

    private Double getMetricValue(String metricName) {
        try {
            var gauge = meterRegistry.find(metricName).gauge();
            if (gauge != null) {
                return gauge.value();
            }
            var counter = meterRegistry.find(metricName).counter();
            if (counter != null) {
                return counter.count();
            }
            var timer = meterRegistry.find(metricName).timer();
            if (timer != null) {
                return timer.totalTime(java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            // Metric might not be available
        }
        return 0.0;
    }
}
