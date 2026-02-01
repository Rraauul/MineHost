package com.example.APIGateWay.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RestAPI1.dto.WorldDTO;
import com.example.RestAPI1.model.Server;
import com.example.RestAPI1.model.World;
import com.example.RestAPI1.service.ServerService;
import com.example.RestAPI1.service.WorldService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class WorldController {
    private final WorldService worldService;
    private final ServerService serverService;

    @Autowired
    public WorldController(WorldService worldService, ServerService serverService) {
        this.worldService = worldService;
        this.serverService = serverService;
    }

    @PostMapping(path = "/createWorld")
    public ResponseEntity<World> createWorld(
            @RequestParam String name,
            @RequestParam String template,
            @RequestParam Integer ram,
            @RequestParam Integer userId,
            @RequestParam Integer serverId) {

        return worldService.createWorld(name, template, ram, userId, serverId);
    }

    // meme chose
    @PostMapping(path = "/startWorld")
    public ResponseEntity<World> startWorld(@RequestParam Integer worldId) {
        return worldService.startWorld(worldId);
    }

    // meme chose
    @PostMapping(path = "/stopWorld")
    public ResponseEntity<World> stopWorld(@RequestParam Integer worldId) {
        return worldService.stopWorld(worldId);
    }

    // meme chose
    @DeleteMapping(path = "/deleteWorld")
    public ResponseEntity<World> deleteWorld(@RequestParam Integer worldId) {
        return worldService.deleteWorld(worldId);
    }

    // pas meme chose
    @GetMapping(path = "/infoWorld")
    public ResponseEntity<List<WorldDTO>> getWorldInfo(@RequestParam Integer worldId) {

        WorldDTO worldDto = worldService.getWorldInfo(worldId);
        if (worldDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body(List.of(worldDto));
    }

    // Pas meme chose
    @GetMapping("/downloadLog")
    public ResponseEntity<Resource> downloadLog(@RequestParam Integer worldId) {
        File logFile = worldService.downloadLog(worldId);

        if (logFile == null || !logFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource fileResource = new FileSystemResource(logFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + logFile.getName())
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .contentLength(logFile.length())
                .body(fileResource);
    }

    @GetMapping(path = "/getWorldsByUserId")
    public ResponseEntity<List<WorldDTO>> getWorldsByUserId(@RequestParam Integer userId) {
        List<WorldDTO> worlds = worldService.getWorldsByUserId(userId);
        if (worlds == null || worlds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body(worlds);
    }

    @GetMapping("getTemplates")
    public ResponseEntity<List<String>> getTemplates() {
        List<String> templates = worldService.getTemplates();
        if (templates == null || templates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body(templates);
    }

}