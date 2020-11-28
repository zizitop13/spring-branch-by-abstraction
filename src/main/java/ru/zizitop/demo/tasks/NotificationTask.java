package ru.zizitop.demo.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.NotificationProperties;
import ru.zizitop.demo.services.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationTask {

    private final NotificationService notificationService;

    private final NotificationProperties notificationProperties;

    @Scheduled(fixedDelay = 1000)
    public void notifySubscriber(){
        notificationService.notify(Notification.builder()
                .recipient(notificationProperties.getSubscriberEmail())
                .text("Notification is worked")
                .build());
    }
}
