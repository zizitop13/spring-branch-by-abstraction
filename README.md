##Spring Boot и TBD, или ветвись оно все по абстракции
Всем привет! 

Наступила осень, приближается зима, листья уже давно опали и перепутанные ветви кустарников наталкивают меня на мысли о моём рабочем Git репозитории... Но вот начался новый проект: новая команда, новый девственно чистый репозиторий, с единственной веткой master. "Тут все будет по другому" - думаю я и начинаю гуглить про TBD. 

Если у вас никак не получается поддерживать git flow, вам надоели кучи этих непонятных веток и правил для них, а главное вы пишите на spring boot, то добро пожаловать в под кат. Там я пробегусь по основным моментам Trunc Based Development и уделю особое внимание такому приему, как Branch By Abstraction и как spring boot нам поможет в этом.  
<img src="https://habrastorage.org/webt/eg/ge/bd/eggebdf6kbkduv9iws-ieolkk8a.jpeg" />  
<cut />
<h2>Initial commit</h2>
Без лишних пояснений, давайте опробуем основные концепции на примере, который вы будете делать вместе со мной. 
Если вам будет, что то не понятно, то в конце я оставлю материалы по которым сам все это осваивал.


###Профили
Начнем с самого простого и наверное для большенства известного приема, использования профилей, 
но для начала нам нужно приложение которое будет что-то делать.
Я не придумал ни чего лучше как написать приложения "оповещатель", REST сервис которому мы передаем оповещение в виде
 json а он уже оповещает кого написано. Напишем первую реализацию которая будет отправлять сообщение на почту. 
 
Email заглушка
 ```java
@Slf4j
@Component
public class EmailSender {
    /**
     * Отправляет сообщение на почту понарошку
     */
    public void sendEmail(String from, String to, String subject, String text){
        log.info("Send email\nfrom: {}\nto: {}\nwith subject: {}\nwith\n text: {}", from, to, subject, text);
    }
}
 ```  
Напишем простую модельку для оповещения 
```java
@Getter
@Setter
@Builder
public class Notification {
    private String text;
    private String recipient;
}
```

Опишем свойства нашего сервиса в виде ConfigurationProperties
```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {    
    private String notificationSenderEmail;
    private String notificationEmailSubject;
}
```

Сервис оповещения
```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    public void notify(Notification notification){
        String from = notificationProperties.getNotificationSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getNotificationEmailSubject();
        String text = notification.getText();
        emailSender.sendEmail(from, to, subject, text);
    }
}
```

Контроллер
```java
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    
    @PostMapping("/notification/notify")
    public void notify(Notification notification){
        notificationService.notify(notification);
    }
}
```













https://trunkbaseddevelopment.com/
https://habr.com/ru/post/519314/
https://tproger.ru/translations/benefits-of-trunk-based-development/
https://bitworks.software/2019-03-22-trunk-based-development.html
https://www.baeldung.com/spring-feature-flags
https://onix-systems.com/blog/introduction-to-feature-flags-in-java-using-the-spring-boot-framework
  
