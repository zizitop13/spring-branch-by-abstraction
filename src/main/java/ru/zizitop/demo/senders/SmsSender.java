package ru.zizitop.demo.senders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsSender {
    /**
     * Отправляет сообщение на телефон
     */
    public void sendSms(String phoneNumber, String text){
        log.info("Send sms {}\nto: {}\nwith text: {}", phoneNumber, text);
    }
}
