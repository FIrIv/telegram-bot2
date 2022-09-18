package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.Set;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    Set<NotificationTask> findNotificationTasksByTime (LocalDateTime time);
}
