package com.engine.order_engine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.engine.order_engine.entity.PromotionEntity;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    
    List<PromotionEntity> findByActiveTrue();
}
