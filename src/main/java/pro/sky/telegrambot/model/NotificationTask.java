package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity (name="notification_task")
public class NotificationTask {
    @Id
    @GeneratedValue (strategy= GenerationType.IDENTITY)
    private long id;

    private long chatId;

    private String text;

    private LocalDateTime time;

    public NotificationTask () {}

    public NotificationTask (long chatId, String text, LocalDateTime time) {
        this.chatId = chatId;
        this.text = text;
        this.time = time;
    }

    public long getId () {
        return id;
    }

    public long getChatId () {
        return chatId;
    }

    public String getText () {
        return text;
    }

    public LocalDateTime getTime () {
        return time;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {return true;}
        if (o == null && o.getClass() != this.getClass()) {return false;}
        NotificationTask o2 = (NotificationTask)o;
        if (this.getId() == o2.getId()) {return true;}
        else {return false;}
    }

    @Override
    public int hashCode () {
        return Objects.hash(id);
    }
}
