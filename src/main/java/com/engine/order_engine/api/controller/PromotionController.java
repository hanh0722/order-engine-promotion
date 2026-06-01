package com.engine.order_engine.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.api.dto.response.BaseResponse;
import com.engine.order_engine.api.service.PromotionService;
import com.engine.order_engine.entity.Promotion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<Promotion>>> getActivePromotions() {
        List<Promotion> activePromotions = this.promotionService.getListActivePromotions();
        return ResponseEntity.ok(BaseResponse.success(activePromotions));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Promotion>> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        Promotion promotion = this.promotionService.createPromotion(request);
        return ResponseEntity.ok(BaseResponse.success(promotion));
    }
}
