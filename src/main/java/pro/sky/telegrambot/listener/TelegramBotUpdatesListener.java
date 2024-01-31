package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    @Autowired
    private TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long chatId = update.message().chat().id();

            //Показать список команд
            if (update.message().text().equals("/help")) {
                SendMessage message = new SendMessage(chatId,
                        "/deleteAllTask : Очистить список задач\n" +
                                "/deleteOldTask : Удалить старые задачи\n" +
                                "/allTask : Показать все задачи");
                SendResponse response = telegramBot.execute(message);
                return;
            }

            // Приветствие
            if (update.message().text().equals("/start")) {
                SendMessage message = new SendMessage(chatId, String.format("Привет, давай начнем работу, %s", update.message().from().firstName()));
                SendResponse response = telegramBot.execute(message);
                return;
            }

            //Очистить список задач
            if (update.message().text().equals("/deleteAllTask")) {
                notificationTaskRepository.findAllByExecDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                        .forEach(notificationTaskRepository::delete);
                SendMessage message = new SendMessage(chatId, "Эта задача удалена");
                SendResponse response = telegramBot.execute(message);
                return;
            }

            //Удалить старые задачи
            if (update.message().text().equals("/deleteOldTask")) {
                notificationTaskRepository.findByExecDateLessThan(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                        .forEach(notificationTaskRepository::delete);
                SendMessage message = new SendMessage(chatId, "Все старые задачи удалены");
                SendResponse response = telegramBot.execute(message);
                return;
            }

            //Показать все задачи
            if (update.message().text().equals("/allTask")) {
                notificationTaskRepository.findAll()
                        .forEach(notificationTask -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                            SendMessage message = new SendMessage(notificationTask.getChatId(),
                                    String.format("%s: %s", notificationTask.getText(), notificationTask.getExecDate().format(formatter)));
                            telegramBot.execute(message);
                        });
                return;
            }

            Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
            Matcher matcher = pattern.matcher(update.message().text());
            String date = null;
            String item = null;

            //Проверка формата сообщения
            if (matcher.matches()) {
                date = matcher.group(1);
                item = matcher.group(3);
                logger.info("Date: {}, utem: {}", date, item);
            } else {
                SendMessage message = new SendMessage(chatId, "Неверный формат задачи");
                SendResponse response = telegramBot.execute(message);
            }

            //Запись задачи
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            if (date != null) {
                LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
                notificationTaskRepository.save(new NotificationTask(chatId, item, dateTime));
                SendMessage message = new SendMessage(chatId, "Задача записана");
                telegramBot.execute(message);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
