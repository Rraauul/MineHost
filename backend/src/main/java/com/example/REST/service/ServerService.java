package com.example.REST.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.REST.dto.ServerDTO;
import com.example.REST.dto.WorldDTO;
import com.example.REST.model.Server;
import com.example.REST.model.World;
import com.example.REST.repository.ServerRepository;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class ServerService {

    private final ServerRepository serverRepository;

    @Autowired
    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Transactional
    public String addServer(String name, String ipAddress, Float diskSize, Integer ram) {
        Server newServer = new Server();
        newServer.setName(name);
        newServer.setIpAddress(ipAddress);
        newServer.setDiskSize(diskSize);
        newServer.setRam(ram);
        serverRepository.save(newServer);
        return "Server added successfully!";
    }

    @Transactional
    public ServerDTO getServerdInfo(Integer serverId) {
        reloadStockageInfo(serverId);
        Server server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            return null;
        }

        // Mapper les données de l'entité World vers le DTO
        return new ServerDTO(
                server.getId(),
                server.getName(),
                server.getIpAddress(),
                server.getRam(),
                server.getDiskSize(),
                server.getDiskAvailable());
    }

    public void reloadStockageInfo(Integer serverId) {
        Optional<Server> serverOptional = serverRepository.findById(serverId);
        if (serverOptional.isPresent()) {
            Server server = serverOptional.get();

            // Calculer l'espace disque disponible
            float totalDiskUsed = 0;
            for (World world : server.getWorlds()) {
                totalDiskUsed += world.getSize();
            }
            float diskAvailable = server.getDiskSize() - totalDiskUsed;

            // Mettre à jour le champ `diskAvailable` dans l'entité Server
            server.setDiskAvailable(diskAvailable);
            serverRepository.save(server);

        }

    }

    public void getDiskAvailable(Integer serverId) {
        Optional<Server> serverOptional = serverRepository.findById(serverId);
        if (serverOptional.isPresent()) {
            Server server = serverOptional.get();
            float totalDiskUsed = 0;
            for (World world : server.getWorlds()) {
                totalDiskUsed += world.getSize();
            }
            float totalDiskAvailable = server.getDiskSize() - totalDiskUsed;
            server.setDiskAvailable(totalDiskAvailable);
            serverRepository.save(server);
        }
    }

}
