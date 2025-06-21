package com.example.serverstatuschecker.cache;

import com.example.serverstatuschecker.model.Server;
import com.example.serverstatuschecker.model.ServerStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommonCache {
    private final Map<String, Object> cache = new HashMap<>();

    public Server getServerById(Long id) {
        return (Server) cache.get("server_" + id);
    }

    public List<Server> getAllServers() {
        return (List<Server>) cache.get("all_servers");
    }

    public void putServer(Server server) {
        cache.put("server_" + server.getId(), server);
    }

    public void putAllServers(List<Server> servers) {
        cache.put("all_servers", servers);
    }

    public ServerStatus getServerStatusById(Long id) {
        return (ServerStatus) cache.get("status_" + id);
    }

    public List<ServerStatus> getAllServerStatuses() {
        return (List<ServerStatus>) cache.get("all_statuses");
    }

    public List<ServerStatus> getStatusesByServerName(String serverName) {
        return (List<ServerStatus>) cache.get("statuses_by_server_" + serverName);
    }

    public void putServerStatus(ServerStatus status) {
        cache.put("status_" + status.getId(), status);
    }

    public void putAllServerStatuses(List<ServerStatus> statuses) {
        cache.put("all_statuses", statuses);
    }

    public void putStatusesByServerName(String serverName, List<ServerStatus> statuses) {
        cache.put("statuses_by_server_" + serverName, statuses);
    }

    public void clearServerCache() {
        cache.keySet().removeIf(key -> key.startsWith("server_") || "all_servers".equals(key));
    }

    public void clearServerStatusCache() {
        cache.keySet().removeIf(key -> key.startsWith("status_") || key.startsWith("statuses_by_server_") || "all_statuses".equals(key));
    }

    public void clearAllCache() {
        cache.clear();
    }
}