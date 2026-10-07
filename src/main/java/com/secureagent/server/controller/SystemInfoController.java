package com.secureagent.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.model.SystemInfoRequest;
import com.secureagent.server.entity.AgentSystemInfo;
import com.secureagent.server.service.SystemInfoService;

@RestController
@RequestMapping("/api/agents")
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    public SystemInfoController(
            SystemInfoService systemInfoService) {

        this.systemInfoService = systemInfoService;
    }

    @PostMapping("/system-info")
    public ResponseEntity<String> addOrUpdateSystemInfo(
            @RequestBody SystemInfoRequest request) {

        systemInfoService.addOrUpdateSystemInfo(request);

        return ResponseEntity.ok("저장 또는 수정 완료");
    }

    @GetMapping("/system-info")
    public ResponseEntity<List<AgentSystemInfo>> getAllSystemInfo() {

        return ResponseEntity.ok(
                systemInfoService.getAllSystemInfo());
    }

    @GetMapping("/system-info/{id}")
    public ResponseEntity<AgentSystemInfo> getSystemInfoById(
            @PathVariable("id") Long id) {

        return systemInfoService.getSystemInfoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @GetMapping("/system-info/computer/{computerName}")
    public ResponseEntity<List<AgentSystemInfo>>
            getSystemInfoByComputerName(
                    @PathVariable("computerName")
                    String computerName) {

        return ResponseEntity.ok(
                systemInfoService
                        .getSystemInfoByComputerName(computerName));
    }

    @PutMapping("/system-info/{id}")
    public ResponseEntity<AgentSystemInfo> updateSystemInfo(
            @PathVariable("id") Long id,
            @RequestBody SystemInfoRequest request) {

        return systemInfoService.updateSystemInfo(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @DeleteMapping("/system-info/{id}")
    public ResponseEntity<Void> deleteSystemInfo(
            @PathVariable("id") Long id) {

        boolean deleted =
                systemInfoService.deleteSystemInfo(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}