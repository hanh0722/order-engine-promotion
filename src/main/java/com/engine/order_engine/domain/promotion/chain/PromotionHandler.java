package com.engine.order_engine.domain.promotion.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;

public abstract class PromotionHandler {
    private PromotionHandler next;

    public PromotionHandler link(PromotionHandler next) {
        this.next = next;
        return next;
    }

    public final List<PromotionDetail> handle(PromotionContext context) {
        List<PromotionDetail> results = new ArrayList<>();
        doHandle(context).ifPresent(results::add);
        if(next != null) {
            results.addAll(next.handle(context));
        }
        return results;
    }

    protected abstract Boolean supports(PromotionContext context);

    protected abstract Optional<PromotionDetail> doHandle(PromotionContext context);
}
