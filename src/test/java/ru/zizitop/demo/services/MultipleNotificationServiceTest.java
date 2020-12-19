package ru.zizitop.demo.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.senders.EmailSender;
import ru.zizitop.demo.senders.PushSender;
import ru.zizitop.demo.senders.SmsSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class MultipleNotificationServiceTest {

    @Autowired
    MultipleNotificationService multipleNotificationService;

    @Autowired
    NotificationProperties properties;

    @MockBean
    EmailSender emailSender;

    @MockBean
    PushSender pushSender;

    @MockBean
    SmsSender smsSender;

    @Test
    void emailNotification() {
        Notification notification = Notification.builder()
                .recipient("test@email.com")
                .text("some text")
                .notificationType(NotificationType.EMAIL)
                .build();
        multipleNotificationService.notify(notification);

        ArgumentCaptor<String> emailCapture = ArgumentCaptor.forClass(String.class);
        verify(emailSender, times(1))
                .sendEmail(emailCapture.capture(),emailCapture.capture(),emailCapture.capture(),emailCapture.capture());
        assertThat(emailCapture.getAllValues())
                .containsExactly(properties.getSenderEmail(),
                        notification.getRecipient(),
                        properties.getEmailSubject(),
                        notification.getText()
                );
    }

    @Test
    void pushNotification() {
        Notification notification = Notification.builder()
                .recipient("id:1171110")
                .text("some text")
                .notificationType(NotificationType.PUSH)
                .build();
        multipleNotificationService.notify(notification);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(pushSender, times(1))
                .push(captor.capture(),captor.capture());

        assertThat(captor.getAllValues())
                .containsExactly(notification.getRecipient(),  notification.getText());
    }

    @Test
    void smsNotification() {
        Notification notification = Notification.builder()
                .recipient("+79157775522")
                .text("some text")
                .notificationType(NotificationType.SMS)
                .build();
        multipleNotificationService.notify(notification);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(smsSender, times(1))
                .sendSms(captor.capture(),captor.capture());

        assertThat(captor.getAllValues())
                .containsExactly(notification.getRecipient(),  notification.getText());
    }

    @Test
    void unsupportedNotification() {
        Notification notification = Notification.builder()
                .recipient("+79157775522")
                .text("some text")
                .build();
        assertThrows(UnsupportedOperationException.class, () -> {
            multipleNotificationService.notify(notification);
        });
    }
}