package ru.zizitop.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.properties.NotificationProperties;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTask {

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
