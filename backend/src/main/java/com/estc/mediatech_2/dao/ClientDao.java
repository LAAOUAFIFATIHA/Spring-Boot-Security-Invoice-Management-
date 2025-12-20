package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // means
public interface ClientDao extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByTelephone(String telephone);

    Optional<ClientEntity> findByUser_Username(String username);
}
