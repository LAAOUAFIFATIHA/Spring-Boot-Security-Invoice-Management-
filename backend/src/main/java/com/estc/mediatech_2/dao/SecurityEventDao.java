package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.SecurityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Security Event Repository for Forensic Analysis
 */
@Repository
public interface SecurityEventDao extends JpaRepository<SecurityEventEntity, Long> {

    /**
     * Find events by type
     */
    List<SecurityEventEntity> findByEventType(String eventType);

    /**
     * Find events by severity
     */
    List<SecurityEventEntity> findBySeverity(String severity);

    /**
     * Find events by username
     */
    List<SecurityEventEntity> findByUsername(String username);

    /**
     * Find events by IP address
     */
    List<SecurityEventEntity> findByIpAddress(String ipAddress);

    /**
     * Find recent events (last N hours)
     */
    @Query("SELECT e FROM SecurityEventEntity e WHERE e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<SecurityEventEntity> findRecentEvents(Instant since);

    /**
     * Find critical events
     */
    @Query("SELECT e FROM SecurityEventEntity e WHERE e.severity = 'CRITICAL' ORDER BY e.timestamp DESC")
    List<SecurityEventEntity> findCriticalEvents();

    /**
     * Count events by type in time range
     */
    @Query("SELECT COUNT(e) FROM SecurityEventEntity e WHERE e.eventType = :eventType AND e.timestamp BETWEEN :start AND :end")
    long countByEventTypeAndTimestampBetween(String eventType, Instant start, Instant end);

    /**
     * Count events by username in time range
     */
    @Query("SELECT COUNT(e) FROM SecurityEventEntity e WHERE e.username = :username AND e.timestamp BETWEEN :start AND :end")
    long countByUsernameAndTimestampBetween(String username, Instant start, Instant end);

    /**
     * Delete old events (for cleanup)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SecurityEventEntity e WHERE e.timestamp < :before")
    long deleteByTimestampBefore(Instant before);

    /**
     * Get event statistics summary
     */
    @Query("SELECT e.eventType, e.severity, COUNT(e) as count FROM SecurityEventEntity e " +
            "WHERE e.timestamp >= :since GROUP BY e.eventType, e.severity")
    List<Object[]> getEventStatistics(Instant since);
}
