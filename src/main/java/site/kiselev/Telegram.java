package site.kiselev;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.threads.SendThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Telegram class
 */
public class Telegram {
    private final Logger logger = LoggerFactory.getLogger(Telegram.class);
    private TelegramBot bot;
    private ExecutorService sendExecutor;
    private SendThread sendThread;

    private Function<SendMessage, Boolean> sendFunction = sm -> {
        SendResponse sendResponse = bot.execute(sm);
        logger.trace("SendResponse: {}", sendResponse);
        if (sendResponse.message() == null) {
            logger.error("Can't send message: {}", sm);
            return false;
        }
        return true;
    };

    //TODO Сделать receiveFunction

    public Telegram() {
        logger.debug("Creating new Telegram");
        bot = TelegramBotAdapter.build(ApplicationConfig.TELEGRAM_BOT_API_TOKEN);
        sendThread = new SendThread().setSendFunction(sendFunction);

        sendExecutor = Executors.newSingleThreadExecutor();
        sendExecutor.submit(sendThread.getRunnable());
    }

    public void exit() {
        logger.debug("attempt to shutdown sendThread");
        sendThread.exit();
        try {
            logger.debug("attempt to shutdown sendExecutor");
            sendExecutor.shutdown();
            sendExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("tasks interrupted");
        } finally {
            if (!sendExecutor.isTerminated()) {
                logger.error("cancel non-finished tasks");
            }
            sendExecutor.shutdownNow();
            logger.debug("shutdown finished");
        }
    }
}
