package site.kiselev.telegram;

import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * TelegramSendThread class
 */
public class TelegramSendThread {

    private final Logger logger = LoggerFactory.getLogger(TelegramSendThread.class);

    private BlockingQueue<SendMessage> outputQueue;
    /**
     * Function to send message through Telegram API
     * Returns true on success
     */
    private Function<SendMessage, Boolean> sendFunction;

    private boolean isExit = false;

    private Runnable runnable = () -> {
        while (!isExit) try {
            send(outputQueue.take());
        } catch (InterruptedException e) {
            logger.error("Can't take from queue :{}", e);
        }
    };

    public TelegramSendThread() {
        this(new LinkedBlockingQueue<>());
    }

    public TelegramSendThread(BlockingQueue<SendMessage> outputQueue) {
        logger.debug("Creating new BugTrackerBot");
        this.outputQueue = outputQueue;
    }

    private void send(SendMessage message) throws InterruptedException {
        while (!isExit && !sendFunction.apply(message)) {
            Thread.sleep(10);
            //TODO Возможно нужно ограничить число повторов
        }
    }

    public BlockingQueue<SendMessage> getOutputQueue() {
        return outputQueue;
    }

    public Runnable getRunnable() {
        assert runnable != null;
        return runnable;
    }

    public void exit() {
        this.isExit = true;
    }

    public TelegramSendThread setSendFunction(Function<SendMessage, Boolean> sendFunction) {
        this.sendFunction = sendFunction;
        return this;
    }
}
