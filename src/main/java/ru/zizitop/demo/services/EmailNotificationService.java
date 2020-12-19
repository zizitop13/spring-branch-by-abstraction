package ru.zizitop.demo.services;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.FeatureProperties;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.repositories.NotificationRepository;
import ru.zizitop.demo.senders.EmailSender;

@Primary
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.active", name = "multiple-senders", havingValue = "false", matchIfMissing = true)
public class EmailNotificationService implements NotificationService {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    private final FeatureProperties featureProperties;

    @Nullable
    private final NotificationRepository notificationRepository;


    @Override
    public void notify(Notification notification){
        String from = notificationProperties.getSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getEmailSubject();
        String text = notification.getText();
        emailSender.sendEmail(from, to, subject, text);

        if(featureProperties.isPersistence()){
            notificationRepository.save(notification);
        }
    }

}
