package ru.zizitop.demo.senders.adapters;

import org.springframework.stereotype.Component;
import ru.zizitop.demo.model.NotificationType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SenderAdapterFactory {

    private Map<NotificationType, SenderAdapter> adapters;

    public SenderAdapterFactory(Set<SenderAdapter> senderAdapterSet){
        adapters = senderAdapterSet.stream()
                .collect(Collectors.toMap(SenderAdapter::getNotificationType, senderAdapter -> senderAdapter));
    }

    public Optional<SenderAdapter> getAdapter(NotificationType notificationType){
        return Optional.ofNullable(adapters.get(notificationType));
    }
}
