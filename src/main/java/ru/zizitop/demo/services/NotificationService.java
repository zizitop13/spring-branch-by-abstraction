package ru.zizitop.demo.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.senders.EmailSender;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    public void notify(Notification notification){
        String from = notificationProperties.getNotificationSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getNotificationEmailSubject();
        String text = notification.getText();
        emailSender.sendEmail(from, to, subject, text);
    }

}
