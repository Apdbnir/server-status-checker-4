package com.example.serverstatuschecker.service;

import com.example.serverstatuschecker.cache.CommonCache;
import com.example.serverstatuschecker.model.Server;
import com.example.serverstatuschecker.repository.ServerRepository;
import com.example.serverstatuschecker.repository.ServerStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final ServerStatusRepository serverStatusRepository;
    private final CommonCache cache;

    @Transactional
    public Server createServer(Server server) {
        if (cache.getServerById(server.getId()) != null) {
            log.info("Cache hit for server id: {}", server.getId());
            return cache.getServerById(server.getId());
        }
        Server savedServer = serverRepository.save(server);
        cache.putServer(savedServer);
        cache.putAllServers(serverRepository.findAll());
        return savedServer;
    }

    @Transactional
    public List<Server> getAllServers() {
        if (cache.getAllServers() != null) {
            log.info("Cache hit for all servers");
            return cache.getAllServers();
        }
        List<Server> servers = serverRepository.findAll();
        cache.putAllServers(servers);
        return servers;
    }

    @Transactional
    public Server getServerById(Long id) {
        if (cache.getServerById(id) != null) {
            log.info("Cache hit for server id: {}", id);
            return cache.getServerById(id);
        }
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Server not found with id: " + id));
        cache.putServer(server);
        return server;
    }

    @Transactional
    public Server updateServer(Long id, Server updatedServer) {
        if (cache.getServerById(id) != null) {
            log.info("Cache hit for server id: {}", id);
            return cache.getServerById(id);
        }
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Server not found with id: " + id));
        server.setName(updatedServer.getName());
        Server savedServer = serverRepository.save(server);
        cache.putServer(savedServer);
        cache.putAllServers(serverRepository.findAll());
        if (savedServer.getName() != null) {
            cache.putStatusesByServerName(savedServer.getName(), serverStatusRepository.findByServerName(savedServer.getName()));
        }
        return savedServer;
    }

    @Transactional
    public void deleteServer(Long id) {
        if (cache.getServerById(id) != null) {
            log.info("Cache hit for server id: {}", id);
        }
        serverRepository.deleteById(id);
        cache.clearServerCache();
    }
}