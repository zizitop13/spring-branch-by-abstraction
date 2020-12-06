package ru.zizitop.demo.services;


import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.FeatureProperties;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.repositories.NotificationRepository;
import ru.zizitop.demo.senders.EmailSender;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    private final FeatureProperties featureProperties;

    @Nullable
    private final NotificationRepository notificationRepository;


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
