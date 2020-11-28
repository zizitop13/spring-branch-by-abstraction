package ru.zizitop.demo.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.senders.EmailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

    @Autowired
    NotificationProperties properties;

    @MockBean
    EmailSender emailSender;

    @Test
    void emailNotification() {
        Notification notification = Notification.builder()
                .recipient("test@email.com")
                .text("some text")
                .build();

        notificationService.notify(notification);

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
}