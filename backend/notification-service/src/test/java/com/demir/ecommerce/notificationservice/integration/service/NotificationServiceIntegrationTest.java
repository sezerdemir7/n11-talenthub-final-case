package com.demir.ecommerce.notificationservice.integration.service;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.notificationservice.dto.NotificationResponse;
import com.demir.ecommerce.notificationservice.entity.Notification;
import com.demir.ecommerce.notificationservice.repository.NotificationRepository;
import com.demir.ecommerce.notificationservice.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NotificationService Integration Tests")
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Long USER_ID = 1L;

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
    }

    private Notification savedNotification(boolean read) {
        Notification n = new Notification(USER_ID, "ORDER_CREATED", "Your order has been created", "{\"orderId\":1}");
        n.setRead(read);
        return notificationRepository.save(n);
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Should persist notification to database")
        void save_validInput_persistsToDatabase() {
            Notification result = notificationService.save(
                    USER_ID, "ORDER_CREATED", "Your order has been created", "{\"orderId\":1}"
            );

            assertThat(result.getId()).isNotNull();
            Optional<Notification> fromDb = notificationRepository.findById(result.getId());
            assertThat(fromDb).isPresent();
            assertThat(fromDb.get().getType()).isEqualTo("ORDER_CREATED");
            assertThat(fromDb.get().isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("getUnread()")
    class GetUnread {

        @Test
        @DisplayName("Should return only unread notifications from database")
        void getUnread_withMixedNotifications_returnsOnlyUnread() {
            savedNotification(false);
            savedNotification(false);
            savedNotification(true);

            List<NotificationResponse> result = notificationService.getUnread(USER_ID);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(n -> !n.read());
        }

        @Test
        @DisplayName("Should return empty list when all notifications are read")
        void getUnread_allRead_returnsEmptyList() {
            savedNotification(true);
            savedNotification(true);

            List<NotificationResponse> result = notificationService.getUnread(USER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when user has no notifications")
        void getUnread_noNotifications_returnsEmptyList() {
            List<NotificationResponse> result = notificationService.getUnread(USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Should return all notifications paginated")
        void getAll_withNotifications_returnsPaginatedResults() {
            savedNotification(false);
            savedNotification(true);
            savedNotification(false);

            PageResponse<NotificationResponse> result = notificationService.getAll(USER_ID, 0, 10);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should return correct page when paginating")
        void getAll_withPagination_returnsCorrectPage() {
            for (int i = 0; i < 5; i++) {
                savedNotification(false);
            }

            PageResponse<NotificationResponse> result = notificationService.getAll(USER_ID, 0, 2);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("Should return empty page when user has no notifications")
        void getAll_noNotifications_returnsEmptyPage() {
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
        @DisplayName("Should mark all unread notifications as read in database")
        void markAllAsRead_withUnreadNotifications_marksAllAsRead() {
            savedNotification(false);
            savedNotification(false);
            savedNotification(false);

            notificationService.markAllAsRead(USER_ID);

            List<Notification> unread = notificationRepository
                    .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(USER_ID);
            assertThat(unread).isEmpty();
        }

        @Test
        @DisplayName("Should not affect already read notifications")
        void markAllAsRead_alreadyRead_remainsRead() {
            savedNotification(true);
            savedNotification(true);

            notificationService.markAllAsRead(USER_ID);

            List<Notification> all = notificationRepository.findAll();
            assertThat(all).allMatch(Notification::isRead);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("Should mark single notification as read in database")
        void markAsRead_existingNotification_updatesInDatabase() {
            Notification n = savedNotification(false);

            notificationService.markAsRead(n.getId());

            Notification fromDb = notificationRepository.findById(n.getId()).orElseThrow();
            assertThat(fromDb.isRead()).isTrue();
        }

        @Test
        @DisplayName("Should do nothing when notification does not exist")
        void markAsRead_notificationNotFound_doesNothing() {
            assertThatNoException().isThrownBy(() ->
                    notificationService.markAsRead(999L)
            );
        }
    }
}