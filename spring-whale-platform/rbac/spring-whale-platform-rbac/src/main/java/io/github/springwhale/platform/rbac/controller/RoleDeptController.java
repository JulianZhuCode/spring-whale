package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.platform.rbac.service.RoleDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rbac/roles/{roleId}/depts")
@RequiredArgsConstructor
public class RoleDeptController {

    private final RoleDeptService roleDeptService;

    @PreAuthorize("hasAnyAuthority('rbac:role', '*')")
    @GetMapping
    public List<Integer> getDepts(@PathVariable Integer roleId) {
        return roleDeptService.getDeptIdsByRoleId(roleId);
    }

    @PreAuthorize("hasAnyAuthority('rbac:role:update', '*')")
    @PostMapping
    public ResponseEntity<Map<String, String>> addDepts(@PathVariable Integer roleId, @RequestBody List<Integer> deptIds) {
        roleDeptService.addDepts(roleId, deptIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @PreAuthorize("hasAnyAuthority('rbac:role:update', '*')")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeDepts(@PathVariable Integer roleId, @RequestBody List<Integer> deptIds) {
        roleDeptService.removeDepts(roleId, deptIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}