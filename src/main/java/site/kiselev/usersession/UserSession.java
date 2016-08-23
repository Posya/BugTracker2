package site.kiselev.usersession;

import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardHide;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.datastore.Datastore;
import site.kiselev.task.Task;

import java.util.concurrent.*;

/**
 * UserSession
 */
public class UserSession {

    private final Integer userID;
    private final Datastore datastore;

    private final BlockingQueue<SendMessage> outputQueue;
    private final BlockingQueue<String> inputQueue;
    private final ExecutorService executor;

    private boolean isExit = false;

    private final Logger logger = LoggerFactory.getLogger(UserSession.class);

    @SuppressWarnings("FieldCanBeLocal")
    private Runnable runnable = () -> {
        try {
            process();
        } catch (InterruptedException e) {
            logger.error("Interrupted: {}", e);
        }
    };

    UserSession(Datastore datastore, BlockingQueue<SendMessage> outputQueue, Integer userID) {
        this.datastore = datastore;
        this.outputQueue = outputQueue;
        this.userID = userID;
        inputQueue = new LinkedBlockingQueue<>();
        executor = Executors.newSingleThreadExecutor();
        executor.submit(runnable);
    }

    /**
     * User session logic
     *
     * @throws InterruptedException on interrupt
     */
    private void process() throws InterruptedException {
        logger.trace("loop started");
        String json = datastore.get(new String[]{Long.toString(userID), "tasks"});
        Task rootTask = Task.fromJSON(json);
        if (rootTask == null) rootTask = new Task(Task.ROOT_ID);

        Task task = rootTask;

        while (!isExit) {

            String cmd = read();
            switch (cmd) {
                case "список":
                    cmdList(task);
                    break;
                case "создать":
                    cmdNew();
                    break;
                default:
                    cmdUnknown(cmd);
            }
        }

        logger.trace("loop stopped");
    }

    private void cmdUnknown(String cmd) throws InterruptedException {
        logger.trace("Command: cmdUnknown: {}", cmd);
        write("Простите, не знаю что делать с командой " + cmd);
    }

    private void cmdNew() throws InterruptedException {
        logger.trace("Command: cmdNew");

    }

    private void cmdList(Task task) throws InterruptedException {
        logger.trace("Command: cmdList: {}", task);
        String out = String.format("Задача %d%n%s", task.getId(), task.getSubj());
        for (Task t : task.getSubTasks()) {
            out += String.format("  %s (%d)%n", t.getSubj(), t.getId());
        }
        writeWithKeyboard(out, new String[][]{{"корень"}, {"создать", "подробнее"}});
    }

    public void putMessage(String text) throws InterruptedException {
        inputQueue.put(text);
    }

    void exit() {
        isExit = true;
        try {
            logger.debug("attempt to shutdown executor for userID {}", userID);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                logger.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            logger.debug("shutdown finished");
        }
    }

    private String read() throws InterruptedException {
        String s = inputQueue.take();
        logger.trace("  read() = {}", s);
        return s;
    }

    private void write(String text) throws InterruptedException {
        writeWithKeyboard(text, new ReplyKeyboardHide());
    }

    private void writeWithKeyboard(String text, String[][] keyboard) throws InterruptedException {
        writeWithKeyboard(text, new ReplyKeyboardMarkup(keyboard, true, true, false));
    }

    private void writeWithKeyboard(String text, Keyboard keyboard) throws InterruptedException {
        logger.trace("  write() = {}", text);
        SendMessage sm = new SendMessage(userID, text);
        sm.parseMode(ParseMode.Markdown);
        sm.replyMarkup(keyboard);
        outputQueue.put(sm);
    }
}
