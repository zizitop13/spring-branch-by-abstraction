package ru.zizitop.demo.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    @Email
    @NotBlank
    private String senderEmail;

    @NotBlank
    private String emailSubject;

    @Email
    private String subscriberEmail;
}
