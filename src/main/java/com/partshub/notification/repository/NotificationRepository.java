package com.partshub.notification.repository;

import com.partshub.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetRoleInOrderByCreatedAtDesc(List<String> roles);
    long countByTargetRoleInAndIsReadFalse(List<String> roles);
}
