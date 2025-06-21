package com.example.serverstatuschecker.controller;

import com.example.serverstatuschecker.model.ServerStatus;
import com.example.serverstatuschecker.service.ServerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/server-status")
@RequiredArgsConstructor
public class ServerStatusController {

    private final ServerStatusService serverStatusService;

    @GetMapping("/check")
    public ResponseEntity<ServerStatus> checkServerStatus(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        ServerStatus request = new ServerStatus();
        request.setUrl(url);
        ServerStatus response = serverStatusService.checkServerStatus(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ServerStatus> createServerStatus(@RequestBody ServerStatus serverStatus) {
        if (serverStatus == null || serverStatus.getUrl() == null || serverStatus.getUrl().trim().isEmpty() ||
                serverStatus.getServer() == null || serverStatus.getServer().getId() == null) {
            throw new IllegalArgumentException("Invalid server status data");
        }
        return ResponseEntity.ok(serverStatusService.createServerStatus(serverStatus));
    }

    @GetMapping
    public ResponseEntity<List<ServerStatus>> getAllServerStatuses() {
        return ResponseEntity.ok(serverStatusService.getAllServerStatuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerStatus> getServerStatusById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid status ID");
        }
        return ResponseEntity.ok(serverStatusService.getServerStatusById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerStatus> updateServerStatus(@PathVariable Long id, @RequestBody ServerStatus serverStatus) {
        if (id == null || id <= 0 || serverStatus == null) {
            throw new IllegalArgumentException("Invalid ID or status data");
        }
        return ResponseEntity.ok(serverStatusService.updateServerStatus(id, serverStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServerStatus(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid status ID");
        }
        serverStatusService.deleteServerStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statuses-by-server")
    public ResponseEntity<List<ServerStatus>> getStatusesByServerName(@RequestParam String serverName) {
        if (serverName == null || serverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Server name cannot be null or empty");
        }
        return ResponseEntity.ok(serverStatusService.getStatusesByServerName(serverName));
    }
}