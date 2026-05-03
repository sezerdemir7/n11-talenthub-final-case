package com.demir.ecommerce.notificationservice.unit.service;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.notificationservice.dto.NotificationResponse;
import com.demir.ecommerce.notificationservice.entity.Notification;
import com.demir.ecommerce.notificationservice.repository.NotificationRepository;

import com.demir.ecommerce.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private static final Long USER_ID = 1L;
    private static final Long NOTIFICATION_ID = 10L;

    private Notification notification(Long id, boolean read) {
        Notification n = new Notification(USER_ID, "ORDER_CREATED", "Your order has been created", "{\"orderId\":1}");
        n.setId(id);
        n.setRead(read);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Should save and return notification")
        void save_validInput_savesAndReturnsNotification() {
            Notification saved = notification(NOTIFICATION_ID, false);
            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            Notification result = notificationService.save(USER_ID, "ORDER_CREATED", "Your order has been created", "{\"orderId\":1}");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(NOTIFICATION_ID);
            assertThat(result.getType()).isEqualTo("ORDER_CREATED");
            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("getUnread()")
    class GetUnread {

        @Test
        @DisplayName("Should return unread notifications mapped to response")
        void getUnread_withUnreadNotifications_returnsMappedResponses() {
            List<Notification> unread = List.of(
                    notification(1L, false),
                    notification(2L, false)
            );
            when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(unread);

            List<NotificationResponse> result = notificationService.getUnread(USER_ID);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).type()).isEqualTo("ORDER_CREATED");
        }

        @Test
        @DisplayName("Should return empty list when no unread notifications exist")
        void getUnread_noUnreadNotifications_returnsEmptyList() {
            when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of());

            List<NotificationResponse> result = notificationService.getUnread(USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Should return paginated notifications mapped to response")
        void getAll_withNotifications_returnsMappedPageResponse() {
            List<Notification> notifications = List.of(
                    notification(1L, false),
                    notification(2L, true)
            );
            Page<Notification> page = new PageImpl<>(notifications, PageRequest.of(0, 10), 2);

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<NotificationResponse> result = notificationService.getAll(USER_ID, 0, 10);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should return empty page when no notifications exist")
        void getAll_noNotifications_returnsEmptyPage() {
            Page<Notification> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<NotificationResponse> result = notificationService.getAll(USER_ID, 0, 10);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsRead {

        @Test
        @DisplayName("Should call repository to mark all notifications as read")
        void markAllAsRead_callsRepository() {
            doNothing().when(notificationRepository).markAllAsReadByUserId(USER_ID);

            notificationService.markAllAsRead(USER_ID);

            verify(notificationRepository).markAllAsReadByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("Should set read=true and save when notification exists")
        void markAsRead_existingNotification_setsReadAndSaves() {
            Notification n = notification(NOTIFICATION_ID, false);
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(n));
            when(notificationRepository.save(n)).thenReturn(n);

            notificationService.markAsRead(NOTIFICATION_ID);

            assertThat(n.isRead()).isTrue();
            verify(notificationRepository).save(n);
        }

        @Test
        @DisplayName("Should do nothing when notification does not exist")
        void markAsRead_notificationNotFound_doesNothing() {
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

            notificationService.markAsRead(NOTIFICATION_ID);

            verify(notificationRepository, never()).save(any());
        }
    }
}