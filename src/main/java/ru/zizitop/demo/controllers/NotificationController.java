package ru.zizitop.demo.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.services.NotificationService;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notification/notify")
    public void notify(Notification notification){
        notificationService.notify(notification);
    }
}
