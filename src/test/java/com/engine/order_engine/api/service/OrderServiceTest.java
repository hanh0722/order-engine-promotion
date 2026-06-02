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
import java.time.Instant;
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
import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.domain.dto.promotion.Promotion;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.PromotionType;
import com.engine.order_engine.entity.CouponEntity;
import com.engine.order_engine.entity.OrderEntity;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.exception.BusinessException;
import com.engine.order_engine.exception.coupon.CouponStatusMessage;
import com.engine.order_engine.mapper.coupon.CouponMapper;
import com.engine.order_engine.mapper.promotion.PromotionMapper;
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
    private CouponMapper couponMapper;
    private PromotionMapper promotionMapper;

    @BeforeEach
    void setUp() {
        promotionPipeline = productionPipeline();
        promotionService = new PromotionService(promotionRepository);
        couponMapper = new CouponMapper();
        promotionMapper = new PromotionMapper();
        orderService = new OrderService(
                promotionPipeline,
                couponRepository,
                promotionRepository,
                promotionService,
                orderRepository,
                couponMapper,
                promotionMapper);
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
        when(couponRepository.findForUpdate("MISSING")).thenReturn(null);

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
        CouponEntity inactive = mock(CouponEntity.class);
        when(inactive.getActive()).thenReturn(false);
        when(couponRepository.findForUpdate("SAVE20")).thenReturn(inactive);

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
        Promotion promotion = promotion(PromotionType.PERCENTAGE_DISCOUNT, "10");
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(
                promotionMapper.toEntity(promotion)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
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
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(99L);
            return order;
        });

        CalculateOrderRequest request = new CalculateOrderRequest();
        request.setCustomerType(CustomerType.VIP);
        request.setItems(List.of(item("A100", "100", 2)));

        OrderEntity saved = orderService.createOrder(
                request,
                new BigDecimal("200.00"),
                new BigDecimal("150.00"),
                new BigDecimal("50.00"));

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity captured = captor.getValue();
        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(captured.getSubTotal()).isEqualByComparingTo("200.00");
        assertThat(captured.getFinalPrice()).isEqualByComparingTo("150.00");
        assertThat(captured.getCustomerType()).isEqualTo(CustomerType.VIP);
        assertThat(captured.getItems()).hasSize(1);
        assertThat(captured.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(captured.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    private void stubAssignmentExample() {
        CouponEntity entity = mock(CouponEntity.class);
        when(entity.getActive()).thenReturn(true);
        when(entity.getQuantity()).thenReturn(10);
        when(entity.getCode()).thenReturn("SUMMER10");
        when(entity.getDiscountAmount()).thenReturn(new BigDecimal("10.00"));
        when(entity.getExpiryDate()).thenReturn(Instant.parse("2099-12-31T23:59:59Z"));
    
        when(couponRepository.findForUpdate("SUMMER10"))
            .thenReturn(entity);
    
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(
            promotionMapper.toEntity(promotion(PromotionType.PERCENTAGE_DISCOUNT, "10")),
            promotionMapper.toEntity(promotion(PromotionType.VIP_DISCOUNT, "5")),
            promotionMapper.toEntity(promotion(PromotionType.BUY2_GET1_FREE, "0"))
        ));
    
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
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
