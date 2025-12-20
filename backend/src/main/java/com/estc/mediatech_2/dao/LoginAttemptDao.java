package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.LoginAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LoginAttemptDao extends JpaRepository<LoginAttemptEntity, Long> {

    @Query("SELECT la FROM LoginAttemptEntity la WHERE la.username = :username AND la.attemptTime > :since ORDER BY la.attemptTime DESC")
    List<LoginAttemptEntity> findRecentAttemptsByUsername(String username, Instant since);

    @Query("SELECT la FROM LoginAttemptEntity la WHERE la.ipAddress = :ipAddress AND la.attemptTime > :since ORDER BY la.attemptTime DESC")
    List<LoginAttemptEntity> findRecentAttemptsByIp(String ipAddress, Instant since);

    @Modifying
    @Query("DELETE FROM LoginAttemptEntity la WHERE la.attemptTime < :before")
    void deleteOldAttempts(Instant before);
}
