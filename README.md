##Spring Boot и TBD, или ветвись оно все по абстракции
Всем привет! 

Наступила осень, приближается зима, листья уже давно опали и перепутанные ветви кустарников наталкивают меня на мысли о моём рабочем Git репозитории... Но вот начался новый проект: новая команда, новый девственно чистый репозиторий, с единственной веткой master. "Тут все будет по другому" - думаю я и начинаю гуглить про TBD. 

Если у вас никак не получается поддерживать git flow, вам надоели кучи этих непонятных веток и правил для них, а главное вы пишите на spring boot, то добро пожаловать в под кат. Там я пробегусь по основным моментам Trunc Based Development и уделю особое внимание такому приему, как Branch By Abstraction и как spring boot нам поможет в этом.  
<img src="https://habrastorage.org/webt/eg/ge/bd/eggebdf6kbkduv9iws-ieolkk8a.jpeg" />  
<cut />
<h2>Initial commit</h2>
Без лишних пояснений, давайте опробуем основные концепции на примере, который вы будете делать вместе со мной. 
Если вам будет, что то не понятно, то в конце я оставлю материалы по которым сам все это осваивал.


###Приложение
Я не придумал ни чего лучше как написать приложения "оповещатель", REST сервис которому мы передаем оповещение в виде
 json а он уже оповещает кого написано. Напишем первую реализацию которая будет отправлять сообщение на почту. 
 
 Для начала опишем свойства нашего сервиса в виде ConfigurationProperties. 
 У нас будут два свойства: sender-email - почтовый адресс отправителя и email-subject - тема письма в оповещении.

 ```java
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
 }
 ```

Теперь сделаем компонент который будет отправлять оповещения на почту, сделаем просто заглушу, реализация для данного примера нам не понадобится.
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
@AllArgsConstructor
public class Notification {
    private String text;
    private String recipient;
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

И наконец контроллер
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

Ещё нам конечно понадобится тест, напишем его для NotificationService
```java
@SpringBootTest
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

    @Autowired
    NotificationProperties properties;

    @MockBean
    EmailSender emailSender;

    @Test
    void emailNotification() {
        Notification notification = Notification.builder()
                .recipient("test@email.com")
                .text("some text")
                .build();

        notificationService.notify(notification);

        ArgumentCaptor<String> emailCapture = ArgumentCaptor.forClass(String.class);
        verify(emailSender, times(1))
                .sendEmail(emailCapture.capture(),emailCapture.capture(),emailCapture.capture(),emailCapture.capture());
        assertThat(emailCapture.getAllValues())
                .containsExactly(properties.getSenderEmail(),
                                notification.getRecipient(),
                                properties.getEmailSubject(),
                                notification.getText()
                );
    }
}
```

Написали, сделали rebase, прогнали сборку с тестами и запушили - эта послдеовательность должна войти в привычку и делаться как можно чаще.

###Профили
Начнем с самого простого и наверное для большенства известного приема, использования профилей.
Для начала вам нужно включить все ваше воображение и представить, что нам понадобилось оповещать кого-то по расписанию. 
Что же сделаем отдельные класс под эту задачу.  








https://trunkbaseddevelopment.com/
https://habr.com/ru/post/519314/
https://tproger.ru/translations/benefits-of-trunk-based-development/
https://bitworks.software/2019-03-22-trunk-based-development.html
https://www.baeldung.com/spring-feature-flags
https://onix-systems.com/blog/introduction-to-feature-flags-in-java-using-the-spring-boot-framework
  
