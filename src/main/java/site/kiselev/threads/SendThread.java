package site.kiselev.threads;

import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * SendThread class
 */
public class SendThread {

    private final Logger logger = LoggerFactory.getLogger(SendThread.class);

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

    public SendThread() {
        this(new LinkedBlockingQueue<>());
    }

    public SendThread(BlockingQueue<SendMessage> outputQueue) {
        logger.debug("Creating new SendThread");
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
        assert sendFunction != null;
        return runnable;
    }

    public void exit() {
        this.isExit = true;
    }

    public SendThread setSendFunction(Function<SendMessage, Boolean> sendFunction) {
        this.sendFunction = sendFunction;
        return this;
    }
}
