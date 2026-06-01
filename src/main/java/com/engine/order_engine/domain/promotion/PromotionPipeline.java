package com.engine.order_engine.domain.promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.chain.PromotionHandler;

public class PromotionPipeline {

    private final PromotionHandler chainHead;

    public PromotionPipeline(PromotionHandler head) {
        this.chainHead = head;
    }

    public List<PromotionDetail> process(PromotionContext context) {
        return this.chainHead.handle(context);
    }
    
    public static Boolean isVipCustomer(CustomerType customerType) {
        return customerType.equals(CustomerType.VIP);
    }

    public static BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
