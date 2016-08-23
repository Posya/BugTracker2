package site.kiselev;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.datastore.Datastore;
import site.kiselev.datastore.RedisDatastore;
import site.kiselev.threads.ReceiveThread;
import site.kiselev.threads.SendThread;
import site.kiselev.usersession.UserSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Console
 */
public class Console {
    private final Logger logger = LoggerFactory.getLogger(Console.class);
    private Gson gson = new Gson();

    private ExecutorService sendExecutor;
    private SendThread sendThread;
    private ExecutorService receiveExecutor;
    private ReceiveThread receiveThread;


    private Scanner scanner = new Scanner(System.in);

    @SuppressWarnings("FieldCanBeLocal")
    private Function<SendMessage, Boolean> sendFunction = sm -> {
        System.out.println(sm);
        return true;
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final Supplier<List<Update>> receiveFunction = () -> {
        List<Update> updates = new ArrayList<>();
        System.out.print(">>> ");
        String input = scanner.nextLine();
        String json = String.format("{\"message\":{\"from\":{\"id\":%d},\"text\":\"%s\"}}",
                25216033,
                input);
        Update update = gson.fromJson(json, Update.class);
        updates.add(update);
        return updates;
    };

    public Console() {
        logger.debug("Creating new Console");

        sendThread = new SendThread().setSendFunction(sendFunction);
        sendExecutor = Executors.newSingleThreadExecutor();
        sendExecutor.submit(sendThread.getRunnable());

        Datastore datastore = new RedisDatastore();
        UserSessionFactory userSessionFactory = new UserSessionFactory(datastore, sendThread.getOutputQueue());

        receiveThread = new ReceiveThread(userSessionFactory).setReceiveFunction(receiveFunction);
        receiveExecutor = Executors.newSingleThreadExecutor();
        receiveExecutor.submit(receiveThread.getRunnable());
    }

    public void exit() {
        logger.debug("attempt to shutdown sendThread");
        sendThread.exit();
        try {
            logger.debug("attempt to shutdown sendExecutor");
            sendExecutor.shutdown();
            sendExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("sendExecutor tasks interrupted");
        } finally {
            if (!sendExecutor.isTerminated()) {
                logger.error("cancel non-finished sendExecutor task");
            }
            sendExecutor.shutdownNow();
            logger.debug("sendExecutor shutdown finished");
        }

        logger.debug("attempt to shutdown receiveThread");
        receiveThread.exit();

        try {
            logger.debug("attempt to shutdown receiveExecutor");
            receiveExecutor.shutdown();
            receiveExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("receiveExecutor tasks interrupted");
        } finally {
            if (!receiveExecutor.isTerminated()) {
                logger.error("cancel non-finished receiveExecutor task");
            }
            receiveExecutor.shutdownNow();
            logger.debug("receiveExecutor shutdown finished");
        }

    }

}
