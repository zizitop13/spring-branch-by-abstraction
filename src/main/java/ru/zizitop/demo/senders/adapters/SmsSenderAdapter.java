package ru.zizitop.demo.senders.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.Notification;
import ru.zizitop.demo.model.NotificationType;
import ru.zizitop.demo.senders.SmsSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSenderAdapter implements SenderAdapter {

    private final SmsSender smsSender;

    @Override
    public void send(Notification notification) {
        smsSender.sendSms(notification.getRecipient(), notification.getText());
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.SMS;
    }
}
