package com.partshub.notification.controller;

import com.partshub.notification.entity.Notification;
import com.partshub.notification.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void seedInitialNotifications() {
        if (notificationRepository.count() == 0) {
            List<Notification> initial = List.of(
                // Storekeeper notifications
                new Notification("ROLE_STOREKEEPER", "Low Stock Alert", "AMD Ryzen 7 7800X3D Processor quantity is below reorder threshold (4 units remaining).", "WARNING", "inventory"),
                new Notification("ROLE_STOREKEEPER", "Stock Restock Received", "25 units of Intel Core i9-14900K added to Colombo WH Bin 01.", "SUCCESS", "inventory"),

                // Purchasing Agent notifications
                new Notification("ROLE_PURCHASING", "PO Discrepancy Flagged", "PO-2026-DISCREPANCY received with damaged goods (12 units damaged).", "DANGER", "purchases"),
                new Notification("ROLE_PURCHASING", "PO Order Sent", "PO-2026-FULLMATCH successfully transmitted to Tech Lanka Pvt Ltd.", "INFO", "purchases"),

                // Procurement Officer notifications
                new Notification("ROLE_PROCUREMENT", "Supplier Score Milestone", "Tech Lanka Pvt Ltd reached Tier 1 Platinum rating with 95% on-time delivery.", "SUCCESS", "suppliers"),
                new Notification("ROLE_PROCUREMENT", "Supplier Contract Review", "Annual vendor agreement for Nanotech Hardware Distributors due in 14 days.", "WARNING", "suppliers"),

                // Sales Representative notifications
                new Notification("ROLE_SALES", "New High-Value Sale Issued", "Invoice INV-2026-1049 issued to Apex Gaming Arena for LKR 1,250,000.", "SUCCESS", "sales"),
                new Notification("ROLE_SALES", "Customer Credit Approval", "Lanka Compute Systems approved for 30-day corporate payment terms.", "INFO", "sales"),

                // Returns Clerk notifications
                new Notification("ROLE_RETURNS", "New Warranty Claim Filed", "Claim CLM-2026-8091 filed for ASUS ROG Strix RTX 4080 (Display artifacting).", "WARNING", "returns"),
                new Notification("ROLE_RETURNS", "RMA Refund Approved", "Credit memo issued for Claim CLM-2026-8042 following technician inspection.", "SUCCESS", "returns"),

                // Business Owner / Executive notifications
                new Notification("ROLE_OWNER", "Q3 Revenue Target Achieved", "Monthly sales volume passed LKR 18.4M across retail and enterprise channels.", "SUCCESS", "system"),
                new Notification("ROLE_OWNER", "Supply Chain Audit Logged", "Quarterly ISO-9001 inventory & supplier audit completed.", "INFO", "system"),

                // System-wide notification
                new Notification("ALL", "System Update Completed", "PartsHub Pro ERP engine updated to release v2.4 with enhanced RBAC security.", "INFO", "system")
            );
            notificationRepository.saveAll(initial);
        }
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(required = false) String role) {

        String target = roleHeader != null ? roleHeader : (role != null ? role : "ROLE_OWNER");
        String normalizedRole = target.trim().toUpperCase().replace(" ", "_");
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        List<String> targetRoles = new ArrayList<>();
        targetRoles.add(normalizedRole);
        targetRoles.add("ALL");

        if ("ROLE_PURCHASING_AGENT".equals(normalizedRole) || "ROLE_PURCHASING".equals(normalizedRole)) {
            targetRoles.add("ROLE_PURCHASING");
            targetRoles.add("ROLE_PURCHASING_AGENT");
        } else if ("ROLE_PROCUREMENT_OFFICER".equals(normalizedRole) || "ROLE_PROCUREMENT".equals(normalizedRole)) {
            targetRoles.add("ROLE_PROCUREMENT");
            targetRoles.add("ROLE_PROCUREMENT_OFFICER");
        } else if ("ROLE_SALES_REP".equals(normalizedRole) || "ROLE_SALES".equals(normalizedRole)) {
            targetRoles.add("ROLE_SALES");
            targetRoles.add("ROLE_SALES_REP");
        } else if ("ROLE_RETURNS_CLERK".equals(normalizedRole) || "ROLE_RETURNS".equals(normalizedRole)) {
            targetRoles.add("ROLE_RETURNS");
            targetRoles.add("ROLE_RETURNS_CLERK");
        }

        // Business Owner receives all notifications across all modules
        if ("ROLE_OWNER".equals(normalizedRole) || "ROLE_BUSINESS_OWNER".equals(normalizedRole)) {
            List<Notification> all = notificationRepository.findAll();
            all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            long unreadCount = all.stream().filter(n -> !n.isRead()).count();
            return ResponseEntity.ok(Map.of("notifications", all, "unreadCount", unreadCount));
        }

        List<Notification> list = notificationRepository.findByTargetRoleInOrderByCreatedAtDesc(targetRoles);
        long unreadCount = notificationRepository.countByTargetRoleInAndIsReadFalse(targetRoles);

        return ResponseEntity.ok(Map.of("notifications", list, "unreadCount", unreadCount));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) {
            Notification notification = opt.get();
            notification.setRead(true);
            notificationRepository.save(notification);
            return ResponseEntity.ok(Map.of("success", true, "id", id));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        List<Notification> all = notificationRepository.findAll();
        for (Notification n : all) {
            n.setRead(true);
        }
        notificationRepository.saveAll(all);
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }
}
