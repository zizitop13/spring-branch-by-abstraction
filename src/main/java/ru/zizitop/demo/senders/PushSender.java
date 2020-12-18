package ru.zizitop.demo.senders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushSender {
    /**
     * Отправляет push уведомления
     */
    public void push(String id, String text){
        log.info("Push {}\nto: {}\nwith text: {}", id, text);
    }
}
