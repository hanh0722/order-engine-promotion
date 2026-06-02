package com.engine.order_engine.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionType;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void getTotalDiscount_sumsAllPromotionAmounts() {
        var discounts = List.of(
                PromotionDetail.builder().discountType("PERCENTAGE_DISCOUNT").amount(new BigDecimal("25.00")).build(),
                PromotionDetail.builder().discountType("VIP_DISCOUNT").amount(new BigDecimal("12.50")).build());

        assertThat(promotionService.getTotalDiscount(discounts)).isEqualByComparingTo("37.50");
    }

    @Test
    void getListActivePromotions_returnsActivePromotionsFromRepository() {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setType(PromotionType.PERCENTAGE_DISCOUNT);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(promotion));

        assertThat(promotionService.getListActivePromotions()).containsExactly(promotion);
    }

    @Test
    void createPromotion_persistsPromotionFromRequest() {
        CreatePromotionRequest request = new CreatePromotionRequest();
        request.setType(PromotionType.PERCENTAGE_DISCOUNT);
        request.setValue(new BigDecimal("10"));
        request.setActive(true);

        PromotionEntity saved = new PromotionEntity();
        saved.setId(1L);
        saved.setType(PromotionType.PERCENTAGE_DISCOUNT);
        saved.setValue(new BigDecimal("10"));
        saved.setActive(true);
        when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(saved);

        PromotionEntity result = promotionService.createPromotion(request);

        assertThat(result.getType()).isEqualTo(PromotionType.PERCENTAGE_DISCOUNT);
        assertThat(result.getValue()).isEqualByComparingTo("10");
        assertThat(result.getActive()).isTrue();
        verify(promotionRepository).save(any(PromotionEntity.class));
    }
}
