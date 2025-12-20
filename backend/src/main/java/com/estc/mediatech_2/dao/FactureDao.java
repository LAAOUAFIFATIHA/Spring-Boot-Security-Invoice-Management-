package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.FactureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactureDao extends JpaRepository<FactureEntity, Long> {
}
