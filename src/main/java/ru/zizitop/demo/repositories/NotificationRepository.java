package ru.zizitop.demo.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.zizitop.demo.model.Notification;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
