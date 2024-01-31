package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    //Найти список задач по определенной дате
    List<NotificationTask> findAllByExecDate(LocalDateTime localDateTime);

    //Найти список задач до определенной даты
    List<NotificationTask> findByExecDateLessThan(LocalDateTime localDateTime);


}
