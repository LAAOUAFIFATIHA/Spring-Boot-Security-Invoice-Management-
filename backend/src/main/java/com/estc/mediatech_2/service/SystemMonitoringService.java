package com.estc.mediatech_2.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class SystemMonitoringService {

    @Data
    @Builder
    public static class PortStatus {
        private int port;
        private boolean isOpen;
        private String service; // Educated guess
    }

    /**
     * Scans a range of ports on the local machine.
     * Rationale: Security monitoring requires knowing what surfaces are exposed.
     */
    public List<PortStatus> scanLocalPorts(int startPort, int endPort) {
        log.info("🔍 Starting port scan: {}-{}", startPort, endPort);
        List<PortStatus> results = new ArrayList<>();

        // We use a thread pool to avoid blocking the main thread for too long
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<PortStatus>> futures = new ArrayList<>();

        for (int port = startPort; port <= endPort; port++) {
            final int p = port;
            futures.add(executor.submit(() -> checkPort("localhost", p)));
        }

        for (Future<PortStatus> future : futures) {
            try {
                results.add(future.get(500, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                // Ignore timeout or errors for specific ports
            }
        }

        executor.shutdown();
        log.info("✅ Port scan completed. Found {} open ports.",
                results.stream().filter(PortStatus::isOpen).count());
        return results;
    }

    private PortStatus checkPort(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 200);
            return PortStatus.builder()
                    .port(port)
                    .isOpen(true)
                    .service(guessService(port))
                    .build();
        } catch (IOException e) {
            return PortStatus.builder()
                    .port(port)
                    .isOpen(false)
                    .service(null)
                    .build();
        }
    }

    private String guessService(int port) {
        switch (port) {
            case 80:
                return "HTTP";
            case 443:
                return "HTTPS";
            case 3306:
                return "MySQL";
            case 8080:
                return "HTTP-ALT";
            case 8090:
                return "MediaTech-Backend";
            case 4200:
                return "MediaTech-Frontend";
            case 22:
                return "SSH";
            case 21:
                return "FTP";
            default:
                return "Unknown";
        }
    }
}
