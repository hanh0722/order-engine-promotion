package com.engine.order_engine.api.service;

import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.service.implement.OrderService;
import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.entity.CouponEntity;
import com.engine.order_engine.repository.CouponRepository;

@SpringBootTest
@ActiveProfiles("test")
class CouponConcurrencyTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderService orderService;

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    @BeforeEach
    void setup() {
        CouponEntity existingCoupon = couponRepository.findByCodeIgnoreCase("SUMMER5");
        if (existingCoupon != null) {
            couponRepository.delete(existingCoupon);
        }

        CouponEntity coupon = new CouponEntity();
        coupon.setCode("SUMMER5");
        coupon.setQuantity(1);
        coupon.setActive(true);
        coupon.setDiscountAmount(new BigDecimal(10));
        coupon.setExpiryDate(Instant.parse("2099-01-01T00:00:00Z"));
        couponRepository.save(coupon);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void should_not_overconsume_coupon_under_concurrency() throws Exception {

        int threads = 10;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    start.await();

                    orderService.calculate(createRequest());

                    success.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();

        CouponEntity coupon = couponRepository.findByCodeIgnoreCase("SUMMER5");

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(success.get()).isEqualTo(1);
        assertThat(failed.get()).isEqualTo(9);
    }

    private CalculateOrderRequest createRequest() {
        CalculateOrderRequest req = new CalculateOrderRequest();
        req.setCouponCode("SUMMER5");
        req.setCustomerType(CustomerType.REGULAR);
        req.setItems(List.of(item("A100", "100", 1)));
        return req;
    }
}