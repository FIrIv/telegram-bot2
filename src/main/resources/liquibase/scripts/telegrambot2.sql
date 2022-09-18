-- liquibase formatted sql

-- changeSet firiv:1

CREATE TABLE notification_task
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    chat_id BIGINT                                  NOT NULL,
    text    VARCHAR(255),
    time    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_notificationtask PRIMARY KEY (id)
);

-- changeSet firiv:2

ALTER TABLE notification_task
    DROP CONSTRAINT pk_notificationtask;

-- changeSet firiv:3

ALTER TABLE notification_task
    ADD CONSTRAINT pk_notificationtask
    PRIMARY KEY (chat_id, text, time)