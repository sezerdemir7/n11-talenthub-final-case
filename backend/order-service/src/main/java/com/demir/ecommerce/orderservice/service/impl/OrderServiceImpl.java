package com.demir.ecommerce.orderservice.service.impl;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.excepption.message.GeneralErrorMessage;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.orderservice.client.CartServiceClient;
import com.demir.ecommerce.orderservice.client.ProductServiceClient;
import com.demir.ecommerce.orderservice.client.UserServiceClient;
import com.demir.ecommerce.orderservice.dto.AddressDto;
import com.demir.ecommerce.orderservice.dto.CheckoutRequest;
import com.demir.ecommerce.orderservice.dto.OrderItemResponse;
import com.demir.ecommerce.orderservice.dto.OrderResponse;
import com.demir.ecommerce.orderservice.dto.cart.CartInternalItem;
import com.demir.ecommerce.orderservice.dto.cart.CartInternalResponse;
import com.demir.ecommerce.orderservice.dto.product.ProductInternalResponse;
import com.demir.ecommerce.orderservice.dto.user.AddressInternalResponse;
import com.demir.ecommerce.orderservice.entity.AddressEmbeddable;
import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderItem;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import com.demir.ecommerce.orderservice.exception.message.OrderErrorMessage;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import com.demir.ecommerce.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderEventPublisher orderEventPublisher;
    private final UserServiceClient userServiceClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartServiceClient cartServiceClient,
                            ProductServiceClient productServiceClient,
                            OrderEventPublisher orderEventPublisher,
                            UserServiceClient userServiceClient) {
        this.orderRepository = orderRepository;
        this.cartServiceClient = cartServiceClient;
        this.productServiceClient = productServiceClient;
        this.orderEventPublisher = orderEventPublisher;
        this.userServiceClient = userServiceClient;
    }


    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorMessage.ORDER_NOT_FOUND));

        if (!SecurityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }

        return mapToResponse(order);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUserId(int page, int size) {

        Long userId = SecurityUtils.getUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository
                .findAllByUserIdAndStatusNot(userId, OrderStatus.EXPIRED, pageable);

        Page<OrderResponse> mapped = orders.map(this::mapToResponse);

        return PageResponse.of(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.isFirst(),
                mapped.isLast(),
                mapped.isEmpty()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderItemResponse> getOrderItems(Long orderId, int page, int size) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorMessage.ORDER_NOT_FOUND));

        if (!SecurityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }

        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity()
                ))
                .toList();

        int start = Math.min(page * size, items.size());
        int end = Math.min(start + size, items.size());
        List<OrderItemResponse> paged = items.subList(start, end);
        int totalPages = (int) Math.ceil((double) items.size() / size);

        return PageResponse.of(
                paged,
                page,
                size,
                items.size(),
                totalPages,
                page == 0,
                page >= totalPages - 1,
                paged.isEmpty()
        );
    }


    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorMessage.ORDER_NOT_FOUND));

        if (!SecurityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(OrderErrorMessage.INVALID_ORDER_STATUS);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity()
                ))
                .toList();

        orderEventPublisher.publishOrderCancelled(
                new OrderCancelledEvent(
                        order.getId(),
                        order.getUserId(),
                        "Order cancelled by user",
                        items
                )
        );
    }


    @Override
    public OrderResponse checkout(CheckoutRequest request) {

        Long userId = SecurityUtils.getUserId();

        CartInternalResponse cart = getCart(userId);
        Map<Long, ProductInternalResponse> productMap = getProductMap(cart);
        List<OrderItem> items = buildOrderItems(cart, productMap);
        BigDecimal totalPrice = calculateTotal(items);
        AddressInternalResponse address = userServiceClient.getAddress(userId, request.addressId());
        Order order = createWaitingPaymentOrder(userId, items, totalPrice, address);

        orderEventPublisher.publishOrderCreated(toOrderCreatedEvent(order));

        return mapToResponse(order);
    }


    private CartInternalResponse getCart(Long userId) {
        CartInternalResponse cart = cartServiceClient.getCart(userId);

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new BusinessException(OrderErrorMessage.ORDER_IS_EMPTY);
        }

        return cart;
    }


    private Map<Long, ProductInternalResponse> getProductMap(CartInternalResponse cart) {
        List<Long> ids = cart.items().stream()
                .map(CartInternalItem::productId)
                .toList();

        return productServiceClient.getByIds(ids).stream()
                .collect(Collectors.toMap(ProductInternalResponse::id, p -> p));
    }


    private List<OrderItem> buildOrderItems(CartInternalResponse cart,
                                            Map<Long, ProductInternalResponse> productMap) {
        return cart.items().stream()
                .map(ci -> {
                    ProductInternalResponse p = productMap.get(ci.productId());

                    if (p == null) throw new BusinessException(OrderErrorMessage.PRODUCT_NOT_FOUND);
                    if (!Boolean.TRUE.equals(p.active())) throw new BusinessException(OrderErrorMessage.PRODUCT_INACTIVE);
                    if (p.stock() < ci.quantity()) throw new BusinessException(OrderErrorMessage.INSUFFICIENT_STOCK);

                    OrderItem item = new OrderItem();
                    item.setProductId(p.id());
                    item.setProductName(p.name());
                    item.setUnitPrice(p.price());
                    item.setQuantity(ci.quantity());

                    return item;
                })
                .toList();
    }


    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private Order createWaitingPaymentOrder(Long userId,
                                            List<OrderItem> items,
                                            BigDecimal total,
                                            AddressInternalResponse addressResponse) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.WAITING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(total);

        AddressEmbeddable address = new AddressEmbeddable();
        address.setCity(addressResponse.city());
        address.setDistrict(addressResponse.district());
        address.setFullAddress(addressResponse.fullAddress());
        address.setPostalCode(addressResponse.postalCode());

        order.setAddress(address);
        items.forEach(i -> i.setOrder(order));
        order.setItems(items);

        return orderRepository.save(order);
    }

    // ================= EVENT MAPPER =================

    private OrderCreatedEvent toOrderCreatedEvent(Order order) {
        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity()
                ))
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                items
        );
    }

    // ================= MAPPERS =================

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity()
                ))
                .toList();

        AddressDto address = new AddressDto(
                order.getAddress().getCity(),
                order.getAddress().getDistrict(),
                order.getAddress().getFullAddress(),
                order.getAddress().getPostalCode()
        );

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getStatus().name(),
                address,
                items
        );
    }
}