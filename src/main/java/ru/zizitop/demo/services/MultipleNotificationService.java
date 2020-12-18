package ru.zizitop.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.repositories.NotificationRepository;
import ru.zizitop.demo.senders.adapters.SenderAdapter;
import ru.zizitop.demo.senders.adapters.SenderAdapterFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MultipleNotificationService implements NotificationService {

    private final SenderAdapterFactory senderAdapterFactory;

    private final NotificationRepository notificationRepository;

    @Override
    public void notify(Notification notification) {
        Optional<SenderAdapter> adapterOptional = senderAdapterFactory.getAdapter(notification.getNotificationType());
        if(adapterOptional.isPresent()){
            adapterOptional.get().send(notification);
        } else {
            throw new UnsupportedOperationException("Unknown notification type: " + notification.getNotificationType());
        }
        notificationRepository.save(notification);
    }
}
