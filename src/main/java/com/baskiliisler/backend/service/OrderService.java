package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.repository.OrderRepository;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final FactoryService factoryService;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;

    @Transactional
    public Order createOrderFromQuote(Quote quote,
                                      Map<Long,LocalDate> deadlines) {

        Order order = orderRepository.save(Order.builder()
                .quote(quote)
                .createdAt(LocalDateTime.now())
                .totalPrice(quote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build());

        orderItemService.assembleAndSaveOrderItems(quote, deadlines, order);
        return order;
    }

    @Transactional
    public Order assignFactory(Long orderId, Long factoryId, LocalDate deadline) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING)
            throw new IllegalStateException("Sadece PENDING sipariş atanabilir");

        Factory factory = factoryService.getFactoryById(factoryId);

        order.setFactory(factory);
        if (deadline != null) order.setDeadline(deadline);

        order.setStatus(OrderStatus.IN_PRODUCTION);
        order.setUpdatedAt(LocalDateTime.now());

        BrandProcess brandProcess = brandProcessService.updateBrandProcessStatus(order.getQuote().getBrand().getId(), ProcessStatus.SENT_TO_FACTORY);

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.SENT_TO_FACTORY,  // toStatus
                ProcessStatus.ORDER_PLACED,  // fromStatus
                "BrandId: " + order.getQuote().getBrand().getId() +
                        ", OrderId: " + order.getId() +
                        ", FactoryId: " + factory.getId() +
                        ", Deadline: " + order.getDeadline()
        );

        return order;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));
    }

    public List<Order> getOrdersByBrand(Long brandId) {
        return orderRepository.findByQuoteBrandId(brandId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Eğer DELIVERED durumuna geçiyorsa deliveredAt'i set et
        if (newStatus == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

}
