package com.engine.order_engine.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.chain.PromotionHandler;
import com.engine.order_engine.domain.promotion.chain.PromotionStrategyHandler;
import com.engine.order_engine.domain.promotion.strategy.Buy2Get1Strategy;
import com.engine.order_engine.domain.promotion.strategy.CouponStrategy;
import com.engine.order_engine.domain.promotion.strategy.PercentageDiscountStrategy;
import com.engine.order_engine.domain.promotion.strategy.PromotionStrategy;
import com.engine.order_engine.domain.promotion.strategy.VipCustomerStrategy;

@Configuration
public class PromotionPipelineConfiguration {
    
    @Bean
    public PromotionPipeline promotionPipeline() {
        PromotionStrategy percentageDiscountStrategy = new PercentageDiscountStrategy();
        PromotionStrategy buy2Get1Strategy = new Buy2Get1Strategy();
        PromotionStrategy couponStrategy = new CouponStrategy();
        PromotionStrategy vipCustomerStrategy = new VipCustomerStrategy();
        
        PromotionHandler head = new PromotionStrategyHandler(percentageDiscountStrategy);
        head.link(new PromotionStrategyHandler(buy2Get1Strategy)).link(new PromotionStrategyHandler(couponStrategy)).link(new PromotionStrategyHandler(vipCustomerStrategy));
        return new PromotionPipeline(head);
    }
}
