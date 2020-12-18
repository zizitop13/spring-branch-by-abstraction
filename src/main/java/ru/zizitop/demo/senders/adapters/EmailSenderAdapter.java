package ru.zizitop.demo.senders.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.senders.EmailSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderAdapter implements SenderAdapter {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    @Override
    public void send(Notification notification) {
        String from = notificationProperties.getSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getEmailSubject();
        String text = notification.getText();
        emailSender.sendEmail(from, to, subject, text);
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }
}
