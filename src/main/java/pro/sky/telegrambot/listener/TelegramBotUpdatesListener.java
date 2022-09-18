package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() == null) return;

            if (update.message().text().equals("/start")) {
                // answer to start message
                SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), "Добрый день. Установите напоминание сделать домашнее задание в формате <дд.мм.гггг чч:мм Сделать домашнюю работу>. "));
                if (!response.isOk()) {
                    logger.info("Chat have started. ");
                }
                return;
            }

            // test task-message by pattern
            String patternForm = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)";
            Pattern pattern = Pattern.compile(patternForm);
            Matcher matcher = pattern.matcher(update.message().text());
            if (matcher.matches()) {
                // work with task-message
                String date = matcher.group(1);
                String item = matcher.group(3);
                NotificationTask task = new NotificationTask(update.message().chat().id(), item, LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                // test task-time to be after our time
                if (task.getTime().isBefore(LocalDateTime.now())) {
                    SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), "Неверное время. "));
                    return;
                }
                try {
                    // test to find index chat-id + date&time + text
                    notificationTaskRepository.save(task);
                    SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), "Задание получено. " + task.getChatId() + " " + task.getText() + " " + task.getTime()));
                    if (!response.isOk()) {
                        logger.info("Task has written to DB. ");
                    }
                } catch (DataIntegrityViolationException e) {
                    SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), "Задание уже есть в базе. "));
                }
            } else {
                // work with underfind message
                SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), "Начните беседу с команды /start. "));
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    // every minute test DB to find tasks to send by date and time
    // sending
    @Scheduled(cron = "0 0/1 * * * *")
    public void runTask() {
        Set<NotificationTask> tasks = notificationTaskRepository.findNotificationTasksByTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        for (NotificationTask task : tasks) {
            SendResponse response = telegramBot.execute(new SendMessage(task.getChatId(), task.getText()));
            if (!response.isOk()) {
                logger.info("Task to " + task.getChatId() + "was sended. ");
            }
        }
    }
}
