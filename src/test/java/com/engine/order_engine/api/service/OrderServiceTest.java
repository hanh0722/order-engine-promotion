package com.engine.order_engine.api.service;

import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static com.engine.order_engine.support.PromotionTestFixtures.productionPipeline;
import static com.engine.order_engine.support.PromotionTestFixtures.promotion;
import static com.engine.order_engine.support.PromotionTestFixtures.summer10Coupon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.dto.response.order.CreateOrderResponse;
import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.PromotionType;
import com.engine.order_engine.entity.Coupon;
import com.engine.order_engine.entity.Order;
import com.engine.order_engine.entity.Promotion;
import com.engine.order_engine.exception.BusinessException;
import com.engine.order_engine.exception.coupon.CouponStatusMessage;
import com.engine.order_engine.repository.CouponRepository;
import com.engine.order_engine.repository.OrderRepository;
import com.engine.order_engine.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private OrderRepository orderRepository;

    private PromotionPipeline promotionPipeline;
    private PromotionService promotionService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        promotionPipeline = productionPipeline();
        promotionService = new PromotionService(promotionRepository);
        orderService = new OrderService(
                promotionPipeline,
                couponRepository,
                promotionRepository,
                promotionService,
                orderRepository);
    }

    @Test
    void calculate_appliesAllRulesAndReturnsAssignmentExampleTotals() {
        stubAssignmentExample();

        CalculateOrderRequest request = assignmentExampleRequest();
        CreateOrderResponse response = orderService.calculate(request);

        assertThat(response.getSubTotal()).isEqualByComparingTo("250.00");
        assertThat(response.getTotalDiscount()).isEqualByComparingTo("147.50");
        assertThat(response.getFinalPrice()).isEqualByComparingTo("102.50");
        assertThat(response.getOrderId()).isEqualTo(42L);
        assertThat(response.getDiscounts()).extracting("discountType")
                .containsExactlyInAnyOrder(
                        "PERCENTAGE_DISCOUNT",
                        "BUY2_GET1_FREE",
                        "COUPON_SUMMER10",
                        "VIP_DISCOUNT");
    }

    @Test
    void calculate_throwsWhenCouponNotFound() {
        when(couponRepository.findByCode("MISSING")).thenReturn(null);

        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.REGULAR);
        request.setCouponCode("MISSING");
        request.setItems(List.of(item("A100", "100", 1)));

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not existed")
                .extracting("code")
                .isEqualTo(CouponStatusMessage.COUPON_NOT_FOUND.name());
    }

    @Test
    void calculate_throwsWhenCouponIsInactive() {
        Coupon inactive = mock(Coupon.class);
        when(inactive.getActive()).thenReturn(false);
        when(couponRepository.findByCode("SAVE20")).thenReturn(inactive);

        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.REGULAR);
        request.setCouponCode("SAVE20");
        request.setItems(List.of(item("A100", "100", 1)));

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not valid")
                .extracting("code")
                .isEqualTo(CouponStatusMessage.COUPON_INVALID.name());
    }

    @Test
    void calculate_withoutCoupon_skipsCouponDiscount() {
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(
                promotion(PromotionType.PERCENTAGE_DISCOUNT, "10")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.REGULAR);
        request.setItems(List.of(item("A100", "100", 1)));

        CreateOrderResponse response = orderService.calculate(request);

        assertThat(response.getSubTotal()).isEqualByComparingTo("100.00");
        assertThat(response.getTotalDiscount()).isEqualByComparingTo("10.00");
        assertThat(response.getFinalPrice()).isEqualByComparingTo("90.00");
        assertThat(response.getDiscounts()).extracting("discountType")
                .containsExactlyInAnyOrder("PERCENTAGE_DISCOUNT", "BUY2_GET1_FREE");
    }

    @Test
    void createOrder_persistsOrderWithLineItems() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            return order;
        });

        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.VIP);
        request.setItems(List.of(item("A100", "100", 2)));

        Order saved = orderService.createOrder(
                request,
                new BigDecimal("200.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order captured = captor.getValue();
        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(captured.getSubTotal()).isEqualByComparingTo("200.00");
        assertThat(captured.getFinalPrice()).isEqualByComparingTo("150.00");
        assertThat(captured.getCustomerType()).isEqualTo(CustomerType.VIP);
        assertThat(captured.getItems()).hasSize(1);
        assertThat(captured.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(captured.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    private void stubAssignmentExample() {
        Coupon coupon = summer10Coupon();
        when(couponRepository.findByCode("SUMMER10")).thenReturn(coupon);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(
                promotion(PromotionType.PERCENTAGE_DISCOUNT, "10"),
                promotion(PromotionType.VIP_DISCOUNT, "5"),
                promotion(PromotionType.BUY2_GET1_FREE, "0")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });
    }

    private static CalculateOrderRequest assignmentExampleRequest() {
        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.VIP);
        request.setCouponCode("SUMMER10");
        request.setItems(List.of(
                item("A100", "100", 2),
                item("B200", "50", 1)));
        return request;
    }
}
