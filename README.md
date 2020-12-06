# Trunk Based Development и Spring Boot, или ветвись оно все по абстракции

Всем привет! 

Закончилась осень, зима вступила в свои законные права, листья уже давно опали и перепутанные ветви кустарников наталкивают меня 
на мысли о моём рабочем Git репозитории... 
Но вот начался новый проект: новая команда, новый девственно чистый, как только что выпавший снег, репозиторий.
"Тут все будет по другому" - думаю я и начинаю "гуглить" про Trunk Based Development. 

Если у вас никак не получается поддерживать git flow, вам надоели кучи этих непонятных веток и правил для них,
если в вашем проекте появляются ветки вида "develop/ivanov" и вы пишите на Spring Boot, то добро пожаловать в под кат! 
Там я пробегусь по основным моментам Trunk Based Development и расскажу о том, как реализовать такой подход, используя Spring Boot.
В своей статье я разберу практические аспекты Trunk Based Development,а по теоритической части в конце и походу статьи будут ссылки.  
<img src="https://habrastorage.org/webt/eg/ge/bd/eggebdf6kbkduv9iws-ieolkk8a.jpeg" />  
<cut />
<h2>Введение</h2>
Trunk Based Development ([TBD](https://trunkbaseddevelopment.com/)) это подход,
 при котором вся разработка ведется на основе единственной ветки trunk (ствол).
Чтобы воплотить такой подход в жизнь, нам нужно следовать трем основным правилам:

1) Любые коммиты в trunk не должны ломать сборку.

2) Любые коммиты в trunk должны быть маленькими, на столько, что ревью нового кода не должно занимать более 10 минут.

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

Ещё нам конечно понадобится тесты. Напишем тест для NotificationService
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
второму правилу - ревью менешь чем за 10 минут. 
Так что на первом этапе можно и даже нужно делать вместо настоящих реализаций заглушки.    


## Профили
Начнем с самого простого и наверное для большенства известного приема, использования профилей.
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

Теперь прогоним наши тесты и получим исключение для теста сервиса: org.mockito.exceptions.verification.TooManyActualInvocations.
Конечно, ведь в нашем тесте ожидался один вызов метода notify, а получилось больше,
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
Используйте профили тогда когда вам нужно включать или выключать целый слой какой-либо логики, причем на постоянной основе,
например безопасность или мониторинг.

Для более точечного управления функциями приложения лучше использовать
 [Feature flags](https://trunkbaseddevelopment.com/feature-flags/), 
но этот способ мы рассмотри уже после первого нашего релиза.
Сделали rebase, прогнали сборку с тестами и запушили в trunk.

## Первый релиз
Настало время выпустить первый релиз нашего приложения.
В TBD описано два способа выпускать релизы: первый из [релизной ветки](https://trunkbaseddevelopment.com/branch-for-release/),
второй прямо из [trunk](https://trunkbaseddevelopment.com/release-from-trunk/).
Здесь я разберу первый способ, так как второй сам ещё не пробовал.

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


 
## Feature flags
Для второй версии нашего приложения у нас стоит задача добавить немного аудита в нашу систему,
 теперь каждое оповещение должно сохраняться в базу данных.
Только вот проблема в том, что не известно когда на production развернут базу данных. Тогда чтобы ни кого не ждать
и не откладывать, то что можно сделать сейчас мы обернем данную функциональность в feature flag. Это позволит внедрить 
код функциональности уже в следующем релизе, а вот включить её можно будет как только появится возможность это сделать 
на production, а вслучае если что-то пойдет не так, её можно будет просто выключитью 

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
Все свойства должны быть boolean переменными.
Флаг persistence - будет включать и выключать сохранение оповещений в БД. 
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
чтобы Spring не падал с ошибкой UnsatisfiedDependencyException. 

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
Создадим отдельную конфигурацию где укажем, что автоконфигурация для БД должна быть исключена, 
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

Запускаем приложение с флагом features.active.persistence: off. Теперь должно работать.
>Самый простой способ управлять флагами в среде развертывания, это указать spring через командную строку параметр
для дополнительных свойств, например: <br>
-Dspring.config.additional-location=file:/etc/config/features.yaml

 Можно конечно, написать на всю
эту логику с флагами тесты, но я не нашел элегнтного спопоба это сделать. 
Да и необходимо понимать, что от флагов нужно будет в конце концов избавится, они и все ухещирения с ними связанные
 нужны лишь на время!  

## Branch by Abstraction




https://trunkbaseddevelopment.com/
https://habr.com/ru/post/519314/
https://tproger.ru/translations/benefits-of-trunk-based-development/
https://bitworks.software/2019-03-22-trunk-based-development.html
https://www.baeldung.com/spring-feature-flags
https://onix-systems.com/blog/introduction-to-feature-flags-in-java-using-the-spring-boot-framework
  
