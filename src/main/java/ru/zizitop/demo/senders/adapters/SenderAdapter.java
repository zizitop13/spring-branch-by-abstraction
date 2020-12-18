package ru.zizitop.demo.senders.adapters;

import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;

public interface SenderAdapter {
    void send(Notification notification);
    NotificationType getNotificationType();
}
