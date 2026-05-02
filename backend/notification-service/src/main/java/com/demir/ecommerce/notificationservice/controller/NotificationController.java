package com.demir.ecommerce.notificationservice.controller;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.notificationservice.dto.NotificationResponse;
import com.demir.ecommerce.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "Notification management operations")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get unread notifications", description = "Returns unread notifications for authenticated user")
    @GetMapping("/unread")
    public ResponseEntity<RestResponse<List<NotificationResponse>>> getUnread() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(RestResponse.of(notificationService.getUnread(userId)));
    }


    @Operation(summary = "Get all notifications", description = "Returns all notifications for authenticated user")
    @GetMapping
    public ResponseEntity<RestResponse<PageResponse<NotificationResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(RestResponse.of(notificationService.getAll(userId, page, size)));
    }

    @Operation(summary = "Mark all as read", description = "Marks all notifications as read")
    @PatchMapping("/read-all")
    public ResponseEntity<RestResponse<Void>> markAllAsRead() {
        Long userId = SecurityUtils.getUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(RestResponse.of(null, "All notifications marked as read"));
    }

    @Operation(summary = "Mark as read", description = "Marks a single notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<RestResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(RestResponse.of(null, "Notification marked as read"));
    }
}