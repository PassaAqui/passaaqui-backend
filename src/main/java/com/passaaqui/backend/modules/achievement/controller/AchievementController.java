package com.passaaqui.backend.modules.achievement.controller;

import com.passaaqui.backend.modules.achievement.dto.AchievementResponseDTO;
import com.passaaqui.backend.modules.achievement.dto.CreateAchievementDTO;
import com.passaaqui.backend.modules.achievement.dto.UnlockAchievementRequestDTO;
import com.passaaqui.backend.modules.achievement.dto.UpdateAchievementDTO;
import com.passaaqui.backend.modules.achievement.service.AchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<AchievementResponseDTO> create(
            @RequestPart("data") @Valid CreateAchievementDTO dto,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(achievementService.create(dto, photo));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<AchievementResponseDTO> update(
            @PathVariable Integer id,
            @RequestPart("data") @Valid UpdateAchievementDTO dto,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(achievementService.update(id, dto, photo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        achievementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AchievementResponseDTO> getById(@PathVariable Integer id) {
        Integer userId = getCurrentUserIdOrNull();
        return ResponseEntity.ok(achievementService.getById(id, userId));
    }

    @GetMapping
    public ResponseEntity<List<AchievementResponseDTO>> listAll(
            @RequestParam(value = "category_id", required = false) Integer categoryId) {
        Integer userId = getCurrentUserIdOrNull();
        return ResponseEntity.ok(achievementService.listAll(userId, categoryId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<List<AchievementResponseDTO>> getMyAchievements() {
        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(achievementService.listUserUnlockedAchievements(userId));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT', 'TOURIST')")
    public ResponseEntity<AchievementResponseDTO> unlock(
            @PathVariable Integer id,
            @RequestBody(required = false) UnlockAchievementRequestDTO requestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer currentUserId = Integer.parseInt(auth.getPrincipal().toString());

        boolean isAdmin = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN"));

        Integer targetUserId = currentUserId;
        if (isAdmin && requestDTO != null && requestDTO.touristId() != null) {
            targetUserId = requestDTO.touristId();
        }

        return ResponseEntity.ok(achievementService.unlock(id, targetUserId));
    }

    private Integer getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return Integer.parseInt(auth.getPrincipal().toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
