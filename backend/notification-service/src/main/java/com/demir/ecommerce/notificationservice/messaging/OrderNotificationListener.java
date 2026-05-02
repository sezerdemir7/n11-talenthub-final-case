package com.demir.ecommerce.notificationservice.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import com.demir.ecommerce.notificationservice.dto.NotificationMessage;
import com.demir.ecommerce.notificationservice.dto.OrderNotificationData;
import com.demir.ecommerce.notificationservice.entity.Notification;
import com.demir.ecommerce.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);
    private static final String NOTIFICATION_DESTINATION = "/queue/notifications";
    private static final String DIRECT_NOTIFICATION_DESTINATION_PREFIX = "/queue/notifications/user-";

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderNotificationListener(SimpMessagingTemplate messagingTemplate,
                                     NotificationService notificationService,
                                     ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConstants.NOTIFICATION_ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent received — orderId: {}, userId: {}", event.orderId(), event.userId());

        OrderNotificationData data = new OrderNotificationData(event.orderId(), event.totalPrice());
        String message = "Siparişiniz başarıyla oluşturuldu! Sipariş No: #" + event.orderId();

        Notification saved = saveNotification(event.userId(), "Sipariş Oluşturuldu", message, data);

        sendToUser(event.userId(), new NotificationMessage(
                "Sipariş Oluşturuldu",
                message,
                data,
                saved.getId()
        ));
    }

    @RabbitListener(queues = RabbitMqConstants.NOTIFICATION_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent received — orderId: {}, userId: {}", event.orderId(), event.userId());

        String message = "Siparişiniz iptal edildi. Sipariş No: #" + event.orderId();

        Notification saved = saveNotification(event.userId(), "ORDER_CANCELLED", message, null);

        sendToUser(event.userId(), new NotificationMessage(
                "Sipariş İptal Edildi",
                message,
                null,
                saved.getId()
        ));
    }


    private Notification saveNotification(Long userId, String type, String message, Object data) {
        String dataJson = null;
        if (data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                log.warn("Data JSON'a çevrilemedi: {}", e.getMessage());
            }
        }
        return notificationService.save(userId, type, message, dataJson);
    }

    private void sendToUser(Long userId, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                NOTIFICATION_DESTINATION,
                notification
        );
        messagingTemplate.convertAndSend(
                DIRECT_NOTIFICATION_DESTINATION_PREFIX + userId,
                notification
        );
        log.info("Notification sent — type: {}, userId: {}", notification.type(), userId);
    }
}
