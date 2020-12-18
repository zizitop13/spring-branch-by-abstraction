package ru.zizitop.demo.senders.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;
import ru.zizitop.demo.senders.PushSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushSenderAdapter implements SenderAdapter {

    private final PushSender pushSender;

    @Override
    public void send(Notification notification) {
        pushSender.push(notification.getRecipient(), notification.getText());
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.PUSH;
    }
}
