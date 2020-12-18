# Trunk Based Development и Spring Boot, или ветвись оно все по абстракции

Всем привет! 

Закончилась осень, зима вступила в свои законные права, листья уже давно опали и перепутанные ветви кустарников наталкивают меня 
на мысли о моём рабочем Git репозитории... 
Но вот начался новый проект: новая команда, чистый, как только что выпавший снег, репозиторий.
"Тут все будет по другому" - думаю я и начинаю "гуглить" про Trunk Based Development. 

Если у вас никак не получается поддерживать git flow, вам надоели кучи этих непонятных веток и правил для них,
если в вашем проекте появляются ветки вида "develop/ivanov" и вы пишите на Spring Boot, то добро пожаловать в под кат! 
Там я пробегусь по основным моментам Trunk Based Development и расскажу о том, как реализовать такой подход, используя Spring Boot.

<img src="https://habrastorage.org/webt/eg/ge/bd/eggebdf6kbkduv9iws-ieolkk8a.jpeg" />  
<cut />

## Введение
Trunk Based Development ([TBD](https://trunkbaseddevelopment.com/)) это подход,
 при котором вся разработка ведется на основе единственной ветки trunk (ствол).
Чтобы воплотить такой подход в жизнь, нам нужно следовать трем основным правилам:

1) Любые коммиты в trunk не должны ломать сборку.

2) Любые коммиты в trunk должны быть маленькими, на столько, что review нового кода не должно занимать более 10 минут.

3) Релиз выпускается только на основе trunk.

Договорились? Теперь давайте разбираться на примере. 


## Первый коммит
Я не придумал ни чего лучше как написать приложения "оповещатель", REST сервис которому мы передаем оповещение в виде
json, а он уже оповещает кого написано. Для начала собирем наш проект на [spring initializr](https://start.spring.io/). 
Я сделал Maven Project, язык Java версия 8, Spring Boot 2.4.0.
Зависимости нам понадобятся следующие:

<details>
<summary>Зависимости</summary>
   
|Название|Тип|Описание|
|-------|-------|-------|
|Spring Configuration Processor| DEVELOPER TOOLS| Generate metadata for developers to offer contextual help and "code completion" when working with custom configuration keys (ex.application.properties/.yml files).|
|Validation| I/O| JSR-303 validation with Hibernate validator.|
|Spring Web| WEB| Build web, including RESTful, applications using Spring MVC. Uses Apache Tomcat as the default embedded container.|
|Lombok| DEVELOPER TOOLS| Java annotation library which helps to reduce boilerplate code.|

</details>

Инициализируем git репозиторий и пушим на [GitHub](https://github.com/) или куда вам больше нравится. Основную ветку можно назвать 
как вам больше нравится: main, master или даже так и назвать - trunk, чтобы всем сразу было понятно чем вы тут занимаетесь. 
Все. Посадили деревце. Теперь будем бережено его выращивать. 
 
## Первая фича

Напишем первую реализацию которая будет отправлять сообщение на почту. 
Для начала опишем свойства нашего сервиса в виде [ConfigurationProperties](https://www.baeldung.com/configuration-properties-in-spring-boot). 
У приложения пока будут  только два свойства: 
sender-email - почтовый адресс отправителя и email-subject - тема письма в оповещении.

<details>

 ```java
 @Getter
 @Setter
 @Component
 @Validated //говорим, что свойства должны проверяться
 @ConfigurationProperties(prefix = "notification")
 public class NotificationProperties {
 
     @Email //проверяем что это почта
     @NotBlank //проверяем что поле заполнено
     private String senderEmail;
 
     @NotBlank
     private String emailSubject;
 }
 ```
</details>

Теперь сделаем компонент который будет отправлять оповещения на почту, делаем просто заглушу. 
>Собственно реализация для данного примера нам вообще не понадобится.

<details>

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
</details>

Напишем простую модельку для оповещения 
<details>

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

</details>

Сервис оповещения
<details>

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
</details>

И наконец контроллер
<details>

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
</details>

Ещё нам конечно понадобится тесты, без них TBD не получится. Напишем тест для NotificationService
<details>

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

</details>

И для NotificationController 
<details>

```java
@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    NotificationService notificationService;

    @SneakyThrows
    @Test
    void testNotify() {
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);
        Notification notification = Notification.builder()
                .recipient("test@email.com")
                .text("some text")
                .build();

        mockMvc.perform(post("/notification/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).notify(notificationArgumentCaptor.capture());
        assertThat(notificationArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(notification);
    }
}
```
</details>

Написали, сделали rebase, прогнали сборку с тестами и запушили в trunk - эта послдеовательность 
должна войти в привычку и делаться как можно чаще.


Для настоящих проектов я очень рекомендую делать первый коммит именно таким,
 чтобы он был как можно меньше и удовлетворял нашему
второму правилу - code review менешь чем за 10 минут. 
Так что на первом этапе можно и даже нужно делать вместо настоящих реализаций заглушки.    


## Профили
Начнем с самого простого и наверное для большенства известного приема - использование профилей.
Для начала вам нужно включить все ваше воображение и представить, что нам вдруг понадобилось оповещать кого-то по расписанию. 
Ну что же сделаем отдельный клас под эту задачу.  
<details>

```java
@Component
@EnableScheduling
@RequiredArgsConstructor
public class NotificationTask {

    private final NotificationService notificationService;

    private final NotificationProperties notificationProperties;

    @Scheduled(fixedDelay = 1000)
    public void notifySubscriber(){
        notificationService.notify(Notification.builder()
                .recipient(notificationProperties.getSubscriberEmail())
                .text("Notification is worked")
                .build());
    }
}
```

</details>

Теперь прогоним наши тесты и получим исключение для теста сервиса: "org.mockito.exceptions.verification.TooManyActualInvocations".
Конечно, ведь в нашем тесте ожидался один вызов метода sendEmail, а получилось больше,
 так как теперь этот же метод вызывается в задаче.
Не порядок.
Можно конечно выставить задаче initialDelay, что бы тест успел запустится раньше чем задача, но это будет кастыль.
Вместо этого как вы уже наверное догадались мы применим профиль. 
Вынисем аннотацию @EnableScheduling в отдельную конфигурацию и добавим аннотацию @Profile где скажем,
что нужно запускать задачи всегда, кроме как в профиле "test".
<details>

```java
@Profile("!test")
@Configuration
@EnableScheduling
public class SchedulingConfig {}
```
</details>

В тестовых ресурсах, в application.yaml добавим включение профиля:
<details>

```yaml
spring:
  profiles:
    active: test
notification:
  email-subject: Auto notification
  sender-email: robot@somecompany.com
```
</details>
Теперь все должно заработать, в тестах задачи по расписанию больше не запускаются, 
но если мы просто запустим приложение из main метода, то задачи будут исправно тикать.

В своей работе в основном я использую профили именно для задач тестирования, но ни кто вам не запретит
использовать их для своих целей, главное как мне кажется с ними не мельчить и не создавать их много.
Используйте профили тогда когда вам нужно включать или выключать целый слой какой-либо логики, 
причем на постоянной основе, т.е. вы не плнируете выкинуть эту возможность когда-нибудь потом.
Примерами могут служить безопасность, мониторинг или как в нашем случае задачи по расписанию.

Для более точечного управления функциями приложения лучше использовать
 [Feature flags](https://trunkbaseddevelopment.com/feature-flags/), 
но этот способ мы рассмотри уже после нашего первого релиза.
Сделали rebase, прогнали сборку с тестами и запушили в trunk.

## Первый релиз
Давайте немного отвлечемся от кодирования и посмотри, что делать с релизами.
В TBD описано два способа выпускать релизы: первый из [релизной ветки](https://trunkbaseddevelopment.com/branch-for-release/),
второй прямо из [trunk](https://trunkbaseddevelopment.com/release-from-trunk/).
Здесь я разберу первый способ, так как второй сам ещё не опробовал в бою.

Первым делом нам нужно взять коммит из которого мы будем делать релиз, это может быть как последний коммит
 в trunk, так и коммит который вы сделали в прошлом,
  все зависит от того, из какой ревизии кода вы хотите сделать релиз.
Для git выкачать прошлый коммит можно так:
```shell script
git checkout <hash>
```
Теперь создаем новую релизную ветку и пушим её в удаленный репозиторий. Для git вот так: 
```shell script
git checkout -b Release_1.0.0
git push -u origin Release_1.0.0
```
Готово! Можно разворачивать код из этой ветке в stage, а затем и в production.

Теперь мы добавим ещё парочку правил, которые будем соблюдать при работе, но уже с релизными ветками:
1) Разработчики не ведут в релизной ветки какие-либо работы
2) Релизная ветка не сливается с trunk
3) Если нужен Hotfix, делаем Cherry-pick из trunk

Таким образом релизная ветка как бы "замораживается" и нужна только для того чтобы выпустить из неё релиз
 и хранить в себе код который соответствующей версии приложения. Релизные ветки можно и даже нужно удалять как только они 
 становятся не актуальным.


 
## Feature flags
Для второй версии нашего приложения у нас стоит задача добавить немного аудита в нашу систему,
 теперь каждое оповещение должно сохраняться в базу данных.
Только вот проблема в том, что не известно когда на production развернут базу данных. Тогда чтобы ни кого не ждать
и не откладывать, то что можно сделать сейчас мы обернем данную функциональность в feature flag. Это позволит внедрить 
код функциональности уже в следующем релизе, а вот включить её можно будет как только появится возможность это сделать, 
а вслучае если что-то пойдет не так, её можно будет снова выключить. 

Добавляем зависимости для взаимодействия с базой данных. 
БД на production у нас будет oracle, а для тестов будем использовать h2.
<details>

```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.oracle.ojdbc</groupId>
        <artifactId>ojdbc10</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

```
</details>

Теперь добавим отдельный класс, где будем описывать только свойства для включения разных фич.
Примим конвенцию, что все свойства в этом классе должны быть boolean.
Добавим туда флаг "persistence", который будет включать и выключать сохранение оповещений в базу. 
<details>
 
```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "features.active")
public class FeatureProperties {
    boolean persistence;
}
```
</details>

Сразу запишем в application.yaml в тестовых ресурсах features.active.persistence: on (spring сам поймет, что on==true).
>Только не забудте сначала скомпилировать проект, чтобы включилось автодополнение в свойствах приложения.  

Нашу модель переделываем в Entity. 
>Осторожно много аннтоаций!
<details>
 
```java
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
    private String recipient;
    @CreationTimestamp
    private LocalDateTime time;
}
```
</details>

Добавляем репозиторий
<details>
 
```java
public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
```
</details>

В NotificationService добавим NotificationRepository и FeatureProperties как зависимость, в конце метода notify
 вызовем метод репозитория save, обернув его в обычный if.  
>Забегая немного вперед, аннотация @Nullable для поля NotificationRepository нам нужна, 
чтобы Spring не падал с ошибкой UnsatisfiedDependencyException, если не найдет такой бин у себя в контексте. 

<details>

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;

    private final NotificationProperties notificationProperties;

    private final FeatureProperties featureProperties;

    @Nullable
    private final NotificationRepository notificationRepository;

    public void notify(Notification notification){
        String from = notificationProperties.getSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getEmailSubject();
        String text = notification.getText();
        emailSender.sendEmail(from, to, subject, text);

        if(featureProperties.isPersistence()){
            notificationRepository.save(notification);
        }
    }

}
```
</details>

Теперь можно запустить тесты, и увидеть, что все они прошли, но если мы запустим наше приложение
оно будет требовать указать url для базы данных и не будет запускаться.
Исправлять будем примерно так же как и для Scheduling. 
Создадим отдельную конфигурацию где укажем, что автоконфигурация для базы данных должна быть исключена, 
если флаг features.active.persistence равен off (spring сам поймет, что off==false).

<details>

```java
@Configuration
@ConditionalOnProperty(prefix = "features.active", name = "persistence", havingValue = "false", matchIfMissing = true)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class DataJpaConfig {
}
```
</details>

Запускаем приложение с флагом features.active.persistence: off в свойствах. Теперь должно работать.
>Для того чтобы управлять флагами в среде развертывания, можно указать spring через командную строку параметр
для дополнительных свойств, например: <br>
-Dspring.config.additional-location=file:/etc/config/features.yaml

Правил с флагами будет два:

1) После того как функциональность полностью протестирована и стабильно работает в проде, флаг этой функции нужно удалить
2) Мест в коде, где идет ветвление по одному и тому же feature флагу, должно быть минимальное количество

По второму правилу поясню подробнее. Если ваша новая функциональность которую вы хотите обернуть в feature флаг, 
заставляет вас писать код вида: "if (flag) {...}" в нескольких местах сразу, то вам стоит задуматься либо над дизайном вашей системы,
либо о приеме "ветвления по абстракции", который как раз сейчас и разберем. 

## Branch by Abstraction

Настало время расширять функциональность. Теперь с клиентской части нашего приложения в сообщениях, будет приходить 
тип оповещения: EMAIL, SMS или PUSH. Следовательно нам необходимо реализовать два дополнительных "отправщика" 
сообщений, а ещё логику в самом сервесе уведомлений которая будет определять реализацию.

Это довольно серьезня доработка, поэтому мы не хотим с ней торопится, мы хотим тщательно проработать  
архитектуру решения, да так чтобы в его развитие принемало как можно больше разработчиков. Поэтому 
мы не будет делать в Git отдельную ветку, в которой можно было бы хранить нестабильный код, а сделаем 
ветку внтури программы с помощью [выделения абстрактного слоя](https://martinfowler.com/bliki/BranchByAbstraction.html).  

Первым делом нам нужно сделать интерфейс NotificationService вместо класса, а сам класс переименовать в 
EmailNotificationService. В Inellij IDEA это можно провернуть с помощью рефакторинга:

1) Правой кнопкой по классу, выбрать Refactor/Extract interface...
2) Выбрать опцию "Rename original class and use interface where possible"
3) В поле "Rename implementation class to" вписываем "EmailNotificationService"
4) В "Members to from interface" нажать галочку напротив метода "notify"
5) Нажать кнопку "Refactor"

После этого все классы должны ссылаться на интерфейс NotificationService, 
а рядом в пакете появится EmailNotificationService где будет страря реализация.

Сделали rebase, прогнали сборку с тестами и запушили в trunk. 
После этого можно спокойно продолжать работу уже над новой реализацией.
Добавим в модель поле с типом оповещения, пусть это просто Enum.

<details>

```java
public enum NotificationType {
    EMAIL, SMS, PUSH
}
```
</details>

Так же нам нужно будет добавить два новых компонента "отправителя":

SmsSender и PushSender.
<details>

```java
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
```
</details>

Новую реалзиацию сервиса назовем MultipleNotificationService, и для начала реалзиуем "в лоб".

<details>

```java
@Service
@RequiredArgsConstructor
public class MultipleNotificationService implements NotificationService {

    private final EmailSender emailSender;

    private final PushSender pushSender;

    private final SmsSender smsSender;

    private final NotificationProperties notificationProperties;
   
    private final NotificationRepository notificationRepository;


    @Override
    public void notify(Notification notification) {
        String from = notificationProperties.getSenderEmail();
        String to = notification.getRecipient();
        String subject = notificationProperties.getEmailSubject();
        String text = notification.getText();

        switch (notification.getNotificationType()) {
            case PUSH:
                pushSender.push(to, text);
                break;
            case SMS:
                smsSender.sendSms(to, text);
                break;
            case EMAIL:
            default:
                emailSender.sendEmail(from, to, subject, text);
                break;

        }
        notificationRepository.save(notification);
    }
}
```
</details>

Запустив тесты, обнаружим, что NotificationServiceTest стал падать с ошибкой: 
"expected single matching bean but found 2: emailNotificationService,multipleNotificationService".
На данном этапе лечить будем добавлением аннотации **@Primary** над старой реализацией сервиса - EmailNotificationService.

Сделали rebase, прогнали сборку с тестами, запушили в trunk, получили от команды по шапке за switch-case.

>Реализовать логику красиво позовлит шаблон "[Стратегия](https://medium.com/@ravthiru/strategy-design-pattern-with-in-spring-boot-application-2ff5a7486cd8)",
но тут есть прблема в том, что у всех компонент "отправителей" разные интерфейсы, собственно как скорее всего и будет в 
реальности, ведь обычно такие компоненты предоставляются внешними библиотекам. 
Решить проблему с разными интерфейсами можно с помощью шаблона "[Адаптер](https://springframework.guru/gang-of-four-design-patterns/adapter-pattern/)".
Расписывать подробно здесь не буду, статья всетаки о другом, но код вы можете
посмотреть на [GitHub](https://github.com/zizitop13/spring-branch-by-abstraction).


После того как сделали все красиво, пробуем ещё раз: rebase, прогнали сборку с тестами, запушили в trunk.
На этот раз код не вызвал ни у кого негатива в команде, и можно его включать в программу.
Сделать это можно по-разному, в зависимости от ситуации:

1) Переставить аннотацию @Primary на новую реализацию - нужно учитывать, что Spring создаст обе реализации, но инъекция будет 
приоритетна для аннотрованного @Primary, если не указана аннтоация @Qualifier которая говорит об обратном, 
что может запутать и привести к ошибкам.
Ещё одним минусом будет, что после сборки нельзя будет откатить изменения. Плюс тут пожалуй один - это просто сделать.

2) Использовать аннотацию @Profile - такой вариант даст возможность поменять реализацию без пересборки проекта, но 
как говорилось выше профили лучше задействовать на что-то более долговременное. 

3) Использовать "feature flag" - пожалуй мой любимый вариант, он не вносит сильной путаницы как это могут сделать @Primary 
и @Profile, позволяет переключать реализацию без пересборки (он для этого и придуман), а так же, если постараться можно сделать 
возможность переключения прямо 
в [runtime](https://onix-systems.medium.com/introduction-to-feature-flags-in-java-using-the-spring-boot-framework-9167d67e4d27)!

После того как новая версия будет обкатана, вместе с feature флагом нужно будет удалить и старую реализацию, 
а вот выделенную абстракцию лучше всетаки оставить.  


   



https://trunkbaseddevelopment.com/
https://habr.com/ru/post/519314/
https://tproger.ru/translations/benefits-of-trunk-based-development/
https://bitworks.software/2019-03-22-trunk-based-development.html
https://www.baeldung.com/spring-feature-flags
https://onix-systems.com/blog/introduction-to-feature-flags-in-java-using-the-spring-boot-framework
  
