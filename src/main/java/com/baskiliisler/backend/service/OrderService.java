package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.repository.OrderRepository;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.repository.DealerRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final FactoryService factoryService;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final BrandRepository brandRepository;
    private final DealerRepository dealerRepository;

    @Transactional
    public Order createOrderFromQuote(Quote quote,
                                      Map<Long,LocalDate> deadlines,
                                      String customerLogoUrl,
                                      String description,
                                      String customerTaxNumber) {

        // Brand'den logo URL'ini al, eğer customerLogoUrl null ise
        String logoUrl = customerLogoUrl;
        if (logoUrl == null && quote.getBrand() != null) {
            logoUrl = quote.getBrand().getLogoUrl();
        }
        
        Order order = orderRepository.save(Order.builder()
                .quote(quote)
                .dealer(quote.getDealer())  // Quote'dan dealer'ı al
                .createdAt(LocalDateTime.now())
                .totalPrice(quote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .customerLogoUrl(logoUrl)
                .description(description)
                .customerTaxNumber(customerTaxNumber)
                .build());

        orderItemService.assembleAndSaveOrderItems(quote, deadlines, order);
        
        // Notification gönder - hata durumunda ana işlem devam etsin
        try {
            notificationService.notifyNewOrder(order);
            notificationService.notifyFactoryAssignmentNeeded(order);
        } catch (Exception e) {
            log.warn("Notification gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        return order;
    }

    @Transactional
    public Order assignFactory(Long orderId, Long factoryId, LocalDate deadline, String description, List<String> imageUrls) {
        log.info("=== FACTORY ASSIGNMENT DEBUG START ===");
        log.info("OrderId: {}, FactoryId: {}, Deadline: {}, Description: {}, ImageUrls: {}", 
                orderId, factoryId, deadline, description, imageUrls);
        
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişlere fabrika atayabilir
            if (order.getQuote().getBrand().getAssignedUser() == null || 
                !order.getQuote().getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu siparişe fabrika atama yetkiniz yok");
            }
        }

        if (order.getStatus() != OrderStatus.PENDING)
            throw new IllegalStateException("Sadece PENDING sipariş atanabilir");

        Factory factory = factoryService.getFactoryById(factoryId);

        log.info("Before setting values - Order Description: {}, Order ImageUrls: {}", 
                order.getDescription(), order.getImageUrls());

        order.setFactory(factory);
        if (deadline != null) {
            order.setDeadline(deadline);
            log.info("Deadline set to: {}", deadline);
        }
        if (description != null) {
            order.setDescription(description);
            log.info("Description set to: {}", description);
        } else {
            log.warn("Description is NULL, not setting");
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            order.setImageUrls(imageUrls);
            log.info("ImageUrls set to: {}", imageUrls);
        } else {
            log.warn("ImageUrls is NULL or empty, not setting. ImageUrls: {}", imageUrls);
        }

        log.info("After setting values - Order Description: {}, Order ImageUrls: {}", 
                order.getDescription(), order.getImageUrls());

        order.setStatus(OrderStatus.IN_PRODUCTION);
        order.setUpdatedAt(LocalDateTime.now());
        
        log.info("=== FACTORY ASSIGNMENT DEBUG END ===");


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
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm siparişleri görebilir
            return orderRepository.findAll();
        } else if (currentUser.getRole() == Role.FACTORY_USER) {
            // FACTORY_USER sadece kendi factory'sine atanmış siparişleri görebilir
            Long factoryId = userService.getCurrentUserFactoryId();
            return orderRepository.findByFactoryId(factoryId);
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri görebilir
            return orderRepository.findByQuoteBrandAssignedUser(currentUser);
        }
    }

    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri görebilir
            if (order.getQuote().getBrand().getAssignedUser() == null || 
                !order.getQuote().getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu siparişe erişim yetkiniz yok");
            }
        }
        
        return order;
    }

    public List<Order> getOrdersByBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Marka bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri görebilir
            if (brand.getAssignedUser() == null || 
                !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markaya ait siparişlere erişim yetkiniz yok");
            }
        }
        
        return orderRepository.findByQuoteBrandId(brandId);
    }

    public List<Order> getOrdersByFactory(Long factoryId) {
        return orderRepository.findByFactoryId(factoryId);
    }

    public List<Order> getDealerOrders() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm siparişleri görebilir
            return orderRepository.findAll();
        } else if (currentUser.getRole() == Role.FACTORY_USER) {
            // FACTORY_USER sadece kendi factory'sine atanmış siparişleri görebilir
            Long factoryId = userService.getCurrentUserFactoryId();
            return orderRepository.findByFactoryId(factoryId);
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri görebilir
            return orderRepository.findByQuoteBrandAssignedUser(currentUser);
        }
    }

    public List<Order> getOrdersByDealer(Long dealerId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın siparişlerini görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            return orderRepository.findByDealer(dealer);
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri görebilir
            return orderRepository.findByQuoteBrandAssignedUser(currentUser);
        }
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri güncelleyebilir
            if (order.getQuote().getBrand().getAssignedUser() == null || 
                !order.getQuote().getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu siparişi güncelleme yetkiniz yok");
            }
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Eğer DELIVERED durumuna geçiyorsa deliveredAt'i set et
        if (newStatus == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        // Log status değişikliği
        log.info("Order {} status updated from {} to {}", orderId, oldStatus, newStatus);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateFactoryOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));

        // Factory kullanıcısının kendi fabrikasına ait sipariş olup olmadığını kontrol et
        Long currentUserFactoryId = userService.getCurrentUserFactoryId();
        if (order.getFactory() == null || !order.getFactory().getId().equals(currentUserFactoryId)) {
            throw new IllegalStateException("Bu sipariş sizin fabrikanıza ait değil");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Eğer DELIVERED durumuna geçiyorsa deliveredAt'i set et
        if (newStatus == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        // Log status değişikliği
        log.info("Factory Order {} status updated from {} to {}", orderId, oldStatus, newStatus);

        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri silebilir
            if (order.getQuote().getBrand().getAssignedUser() == null || 
                !order.getQuote().getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu siparişi silme yetkiniz yok");
            }
        }
        
        // Sadece belirli durumlardaki siparişler silinebilir
        if (order.getStatus() != OrderStatus.PENDING && 
            order.getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Bu sipariş silinemez. Sadece bekleyen veya iptal edilmiş siparişler silinebilir.");
        }
        
        orderRepository.delete(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sipariş bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait siparişleri iptal edebilir
            if (order.getQuote().getBrand().getAssignedUser() == null || 
                !order.getQuote().getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu siparişi iptal etme yetkiniz yok");
            }
        }
        
        // Sadece belirli durumlardaki siparişler iptal edilebilir
        if (order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Bu sipariş iptal edilemez. Teslim edilmiş veya zaten iptal edilmiş siparişler iptal edilemez.");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }
}
