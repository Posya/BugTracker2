package site.kiselev.threads;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.usersession.UserSession;
import site.kiselev.usersession.UserSessionFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * ReceiveThread
 */
public class ReceiveThread {
    private final Logger logger = LoggerFactory.getLogger(ReceiveThread.class);
    private Supplier<List<Update>> receiveFunction;

    private boolean isExit = false;

    private Runnable runnable = () -> {
        while (!isExit) {
            List<Update> updates = receiveFunction.get();
            updates.forEach(this::processUpdate);
        }
    };
    private UserSessionFactory userSessionFactory;

    public ReceiveThread(UserSessionFactory userSessionFactory) {
        logger.debug("Creating new ReceiveThread");
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

    public ReceiveThread setReceiveFunction(Supplier<List<Update>> receiveFunction) {
        this.receiveFunction = receiveFunction;
        return this;
    }
}
