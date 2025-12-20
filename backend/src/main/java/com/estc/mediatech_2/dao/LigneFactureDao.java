package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.LigneFactureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneFactureDao extends JpaRepository<LigneFactureEntity, Long> {
}
