package com.engine.order_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.engine.order_engine.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
}
