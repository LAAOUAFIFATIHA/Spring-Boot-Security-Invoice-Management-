package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.TokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenBlacklistDao extends JpaRepository<TokenBlacklistEntity, Long> {

    Optional<TokenBlacklistEntity> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM TokenBlacklistEntity tb WHERE tb.expiryDate < :now")
    void deleteExpiredTokens(Instant now);
}
