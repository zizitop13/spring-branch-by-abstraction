package ru.zizitop.demo.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Notification {
    private String text;
    private String recipient;
}
