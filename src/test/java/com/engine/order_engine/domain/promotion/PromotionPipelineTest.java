package com.engine.order_engine.domain.promotion;

import static com.engine.order_engine.support.PromotionTestFixtures.assignmentExampleContext;
import static com.engine.order_engine.support.PromotionTestFixtures.productionPipeline;
import static com.engine.order_engine.support.PromotionTestFixtures.summer10Coupon;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PromotionPipelineTest {

    @Test
    void process_runsFullChainInConfiguredOrder() {
        var pipeline = productionPipeline();
        var context = assignmentExampleContext(summer10Coupon());

        var discounts = pipeline.process(context);

        assertThat(discounts).hasSize(4);
        assertThat(discounts.stream().map(d -> d.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("147.50");
    }
}
