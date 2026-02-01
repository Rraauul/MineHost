package com.example.APIGateWay.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestAPI1.dto.ServerDTO;
import com.example.RestAPI1.dto.WorldDTO;
import com.example.RestAPI1.model.Server;
import com.example.RestAPI1.model.World;
import com.example.RestAPI1.service.ServerService;
import com.example.RestAPI1.service.WorldService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class ServerController {
    private final WorldService worldService;
    private final ServerService serverService;

    @Autowired
    public ServerController(WorldService worldService, ServerService serverService) {
        this.worldService = worldService;
        this.serverService = serverService;
    }

    @GetMapping(path = "/infoServer")

    public ResponseEntity<List<ServerDTO>> getServerInfo(@RequestParam Integer serverId) {

        ServerDTO server = serverService.getServerdInfo(serverId);
        if (server == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body(List.of(server));
    }

}