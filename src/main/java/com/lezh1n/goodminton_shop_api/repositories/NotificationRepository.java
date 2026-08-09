package com.lezh1n.goodminton_shop_api.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lezh1n.goodminton_shop_api.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Integer recipientId, Pageable pageable);

    /** Drives the bell badge; hits the partial index from V12. */
    long countByRecipient_IdAndReadAtIsNull(Integer recipientId);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.readAt = :now
            WHERE n.recipient.id = :recipientId AND n.readAt IS NULL
            """)
    int markAllRead(@Param("recipientId") Integer recipientId, @Param("now") LocalDateTime now);
}
