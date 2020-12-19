package ru.zizitop.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;
import ru.zizitop.demo.repositories.NotificationRepository;
import ru.zizitop.demo.senders.adapters.SenderAdapter;
import ru.zizitop.demo.senders.adapters.SenderAdapterFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.active", name = "multiple-senders", havingValue = "true", matchIfMissing = true)
public class MultipleNotificationService implements NotificationService {

    private final SenderAdapterFactory senderAdapterFactory;

    private final NotificationRepository notificationRepository;

    @Override
    public void notify(Notification notification) {
        NotificationType notificationType = notification.getNotificationType();
        Optional<SenderAdapter> adapterOptional = senderAdapterFactory.getAdapter(notificationType!=null ? notificationType : NotificationType.UNKNOWN);
        if(adapterOptional.isPresent()){
            adapterOptional.get().send(notification);
        } else {
            throw new UnsupportedOperationException("Unknown notification type: " + notification.getNotificationType());
        }
        notificationRepository.save(notification);
    }
}
