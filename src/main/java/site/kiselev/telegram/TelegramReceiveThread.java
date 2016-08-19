package site.kiselev.telegram;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.usersession.UserSession;
import site.kiselev.usersession.UserSessionFactory;

import java.util.function.Supplier;

/**
 * TelegramReceiveThread
 */
public class TelegramReceiveThread {
    private final Logger logger = LoggerFactory.getLogger(TelegramReceiveThread.class);
    private Supplier<GetUpdatesResponse> receiveFunction;

    private boolean isExit = false;

    private Runnable runnable = () -> {
        while (!isExit) {
            GetUpdatesResponse updatesResponse = receiveFunction.get();
            updatesResponse.updates().forEach(this::processUpdate);
        }
    };
    private UserSessionFactory userSessionFactory;

    public TelegramReceiveThread(UserSessionFactory userSessionFactory) {
        this.userSessionFactory = userSessionFactory;
    }

    private void processUpdate(Update update) {
        logger.trace("Processing update: {}", update);
        Message message = update.message();
        User user = message.from();
        try {
            UserSession us = userSessionFactory.getUserSession(user.id());
            us.putMessage(message.text());
        } catch (InterruptedException e) {
            logger.error("Can't put message {} to queue for user {}: {}", message.text(), user.id(), e);
        } catch (UserSessionFactory.UserSessionFactoryException e) {
            logger.error("Can't get UserSession for user {}: {}", user.id(), e);
        }
    }

    public void exit() {
        isExit = true;
    }

    public Runnable getRunnable() {
        assert receiveFunction != null;
        return runnable;
    }

    public void setReceiveFunction(Supplier<GetUpdatesResponse> receiveFunction) {
        this.receiveFunction = receiveFunction;
    }
}
