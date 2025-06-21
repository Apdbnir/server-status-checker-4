package com.example.serverstatuschecker.service;

import com.example.serverstatuschecker.cache.CommonCache;
import com.example.serverstatuschecker.model.Server;
import com.example.serverstatuschecker.model.ServerStatus;
import com.example.serverstatuschecker.repository.ServerRepository;
import com.example.serverstatuschecker.repository.ServerStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerStatusService {

    private final ServerRepository serverRepository;
    private final ServerStatusRepository serverStatusRepository;
    private final CommonCache cache;

    @Transactional
    public ServerStatus checkServerStatus(ServerStatus request) {
        String cacheKey = "status_" + request.getUrl().hashCode();
        if (cache.getServerStatusById((long) cacheKey.hashCode()) != null) {
            log.info("Cache hit for key: {}", cacheKey);
            return cache.getServerStatusById((long) cacheKey.hashCode());
        }
        ServerStatus response = new ServerStatus();
        response.setUrl(request.getUrl());
        try {
            URL serverUrl = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            response.setIsAvailable(responseCode >= 200 && responseCode < 300);
            response.setMessage(response.isAvailable() ? "Server is available" :
                    "Server responded with status code: " + responseCode);
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error checking server status for URL: {}", request.getUrl(), e);
            response.setIsAvailable(false);
            response.setMessage("Failed to connect: " + e.getMessage());
        }
        Server server = serverRepository.findById(1L).orElseGet(() -> {
            Server newServer = new Server();
            newServer.setName("Default Server");
            return serverRepository.save(newServer);
        });
        response.setServer(server);
        ServerStatus savedResponse = serverStatusRepository.save(response);
        cache.putServerStatus(savedResponse);
        cache.putAllServerStatuses(serverStatusRepository.findAll());
        if (server.getName() != null) {
            cache.putStatusesByServerName(server.getName(), serverStatusRepository.findByServerName(server.getName()));
        }
        return savedResponse;
    }

    @Transactional
    public ServerStatus createServerStatus(ServerStatus serverStatus) {
        if (cache.getServerStatusById(serverStatus.getId()) != null) {
            log.info("Cache hit for status id: {}", serverStatus.getId());
            return cache.getServerStatusById(serverStatus.getId());
        }
        ServerStatus savedStatus = serverStatusRepository.save(serverStatus);
        cache.putServerStatus(savedStatus);
        cache.putAllServerStatuses(serverStatusRepository.findAll());
        if (savedStatus.getServer() != null && savedStatus.getServer().getName() != null) {
            cache.putStatusesByServerName(savedStatus.getServer().getName(), serverStatusRepository.findByServerName(savedStatus.getServer().getName()));
        }
        return savedStatus;
    }

    @Transactional
    public List<ServerStatus> getAllServerStatuses() {
        if (cache.getAllServerStatuses() != null) {
            log.info("Cache hit for all statuses");
            return cache.getAllServerStatuses();
        }
        List<ServerStatus> statuses = serverStatusRepository.findAll();
        cache.putAllServerStatuses(statuses);
        return statuses;
    }

    @Transactional
    public ServerStatus getServerStatusById(Long id) {
        if (cache.getServerStatusById(id) != null) {
            log.info("Cache hit for status id: {}", id);
            return cache.getServerStatusById(id);
        }
        ServerStatus status = serverStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ServerStatus not found with id: " + id));
        cache.putServerStatus(status);
        return status;
    }

    @Transactional
    public ServerStatus updateServerStatus(Long id, ServerStatus updatedStatus) {
        if (cache.getServerStatusById(id) != null) {
            log.info("Cache hit for status id: {}", id);
            return cache.getServerStatusById(id);
        }
        ServerStatus status = serverStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ServerStatus not found with id: " + id));
        status.setUrl(updatedStatus.getUrl());
        status.setIsAvailable(updatedStatus.isAvailable());
        status.setMessage(updatedStatus.getMessage());
        ServerStatus savedStatus = serverStatusRepository.save(status);
        cache.putServerStatus(savedStatus);
        cache.putAllServerStatuses(serverStatusRepository.findAll());
        if (savedStatus.getServer() != null && savedStatus.getServer().getName() != null) {
            cache.putStatusesByServerName(savedStatus.getServer().getName(), serverStatusRepository.findByServerName(savedStatus.getServer().getName()));
        }
        return savedStatus;
    }

    @Transactional
    public void deleteServerStatus(Long id) {
        if (cache.getServerStatusById(id) != null) {
            log.info("Cache hit for status id: {}", id);
        }
        serverStatusRepository.deleteById(id);
        cache.clearServerStatusCache();
    }

    @Transactional
    public List<ServerStatus> getStatusesByServerName(String serverName) {
        if (cache.getStatusesByServerName(serverName) != null) {
            log.info("Cache hit for server name: {}", serverName);
            return cache.getStatusesByServerName(serverName);
        }
        List<ServerStatus> statuses = serverStatusRepository.findByServerName(serverName);
        cache.putStatusesByServerName(serverName, statuses);
        return statuses;
    }
}