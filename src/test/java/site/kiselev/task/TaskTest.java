package site.kiselev.task;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static site.kiselev.task.Task.REMINDER_NOT_SET;
import static site.kiselev.task.Task.ROOT_ID;

/**
 * Tests for Task class
 */
public class TaskTest {
    private Task task;

    @Before
    public void setUp() throws Exception {
        Task root = new Task(ROOT_ID);

        task = new Task(1);
        root.addSubTask(task);

        task.addSubTask(new Task(11));
        task.addSubTask(new Task(12).done());
        task.addSubTask(new Task(13));

        task.setSubj("task 1").setDescription("description 1");


    }

    @Test
    public void getId() throws Exception {
        assertTrue(task.getId() == 1);
    }

    @Test
    public void getSubTasks() throws Exception {
        assertEquals(3, task.getSubTasks().size());
    }

    @Test
    public void addSubTask() throws Exception {
        task.addSubTask(new Task(14));
        assertEquals(4, task.getSubTasks().size());
    }

    @Test
    public void addSubTasksByList() throws Exception {
        List<Task> list = new ArrayList<>();
        list.add(new Task(15));
        list.add(new Task(16));
        list.add(new Task(17));
        task.addSubTasks(list);
        assertEquals(6, task.getSubTasks().size());
    }

    @Test
    public void addSubTasksActiveToDone() throws Exception {
        // К задаче с выполненым статусом добавляем задачу с статусом активно.
        Task task = new Task(1).done();
        try {
            task.addSubTask(new Task(2));
            fail();
        } catch (IllegalStateException ignored) {}

        task.addSubTask(new Task(2).done());
        task.addSubTask(new Task(3).done().archive());

        List<Task> list = new ArrayList<>();
        list.add(new Task(4).done());
        list.add(new Task(5));

        try {
            task.addSubTasks(list);
            fail();
        } catch (IllegalStateException ignored) {}

    }

    @Test
    public void getName() throws Exception {
        assertEquals("task 1", task.getSubj());
    }

    @Test
    public void setName() throws Exception {
        task.setSubj("test");
        assertEquals("test", task.getSubj());
    }

    @Test
    public void getDescription() throws Exception {
        assertEquals("description 1", task.getDescription());
    }

    @Test
    public void setDescription() throws Exception {
        task.setDescription("test");
        assertEquals("test", task.getDescription());
    }

    @Test
    public void getState() throws Exception {
        assertEquals(State.ACTIVE, task.getState());
    }

    @Test
    public void doneAndArchive() throws Exception {
        try {
            task.archive();
            fail();
        } catch (IllegalStateException ignored) {}

        task.done();
        assertEquals(State.DONE, task.getState());

        task.archive();
        assertEquals(State.ARCHIVED, task.getState());

        try {
            task.undone();
            fail();
        } catch (IllegalStateException ignored) {}

        task.unarchive();
        assertEquals(State.DONE, task.getState());

        task.undone();
        assertEquals(State.ACTIVE, task.getState());
    }

    @Test
    public void doneAndArchiveSub() throws Exception {
        // Проверка, если у подзадачи статус - архивированная, а текущая - выполненая
        // и переводим в статуст активный - это позволительно.
        Task task = new Task(1).done();
        task.addSubTask(new Task(2).done().archive());
        task.undone();
    }

    @Test
    public void getReminder() throws Exception {
        assertTrue(task.getReminder() == REMINDER_NOT_SET);
    }

    @Test
    public void setReminder() throws Exception {
        task.setReminder(12345678);
        assertTrue(task.getReminder() == 12345678);
    }

    @Test
    public void isRemind() throws Exception {
        assertFalse(task.isItTime(12345679));
        task.setReminder(12345678);
        assertTrue(task.isItTime(12345679));
        task.clearReminder();
        assertFalse(task.isItTime(12345679));
    }

    @Test
    public void isReminderSet() throws Exception {
        assertFalse(task.isReminderSet());
        task.setReminder(12345678);
        assertTrue(task.isReminderSet());
        task.clearReminder();
        assertFalse(task.isReminderSet());
    }

    @Test
    public void clearReminder() throws Exception {
        try {
            task.clearReminder();
            fail();
        } catch (IllegalStateException ignored) {}
        task.setReminder(12345678);
        task.clearReminder();
        assertFalse(task.isReminderSet());
    }

    @Test
    public void moveTest() throws Exception {
        Task task1 = new Task(1).addSubTask(new Task(2));
        Task task2 = new Task(3);
        task1.getSubTasks().get(0).move(task1, task2);
        assertEquals(0, task1.getSubTasks().size());
        assertEquals(1, task2.getSubTasks().size());
        assertEquals(2, task2.getSubTasks().get(0).getId());
    }

    @Test
    public void findByID() throws Exception {
        Task found = task.findByID(12);
        assertEquals(12, found.getId());
    }

    @Test
    public void findByName() throws Exception {
        Task task = new Task(1).setSubj("task");

        for (int i = 2; i < 10; i++) {
            Task task1 = new Task(i).setSubj("t" + i);
            task.addSubTask(task1);
            for (int j = 0; j < 10; j++) {
                task1.addSubTask(new Task(10 * i + j).setSubj("task" + i + j));
            }
        }


        List<Task> found = task.findByName("task");
        assertEquals(81, found.size());
    }

    @Test
    public void fromJSON() throws Exception {
        String json = "{\"id\":1,\"subTasks\":[{\"id\":11,\"subTasks\":[],\"subj\":\"\",\"state\":" +
                "\"active\",\"description\":\"\",\"reminder\":0},{\"id\":12,\"subTasks\":[],\"subj\":\"\"" +
                ",\"state\":\"done\",\"description\":\"\",\"reminder\":0},{\"id\":13,\"subTasks\":[],\"subj\"" +
                ":\"\",\"state\":\"active\",\"description\":\"\",\"reminder\":0}],\"subj\":\"task 1\",\"state\""+
                ":\"active\",\"description\":\"description 1\",\"reminder\":0}";
        Task test = Task.fromJSON(json);
        assertEquals(task, test);
    }

    @Test
    public void toJSON() throws Exception {
        task.setSaver(t -> {});
        String json = task.toJSON();
        String test = "{\"id\":1,\"subTasks\":[{\"id\":11,\"subTasks\":[],\"subj\":\"\",\"state\":" +
                "\"active\",\"description\":\"\",\"reminder\":0},{\"id\":12,\"subTasks\":[],\"subj\":\"\"" +
                ",\"state\":\"done\",\"description\":\"\",\"reminder\":0},{\"id\":13,\"subTasks\":[],\"subj\"" +
                ":\"\",\"state\":\"active\",\"description\":\"\",\"reminder\":0}],\"subj\":\"task 1\",\"state\""+
                ":\"active\",\"description\":\"description 1\",\"reminder\":0}";
        assertEquals(test, json);
    }

    @Test
    public void getSubTasksTest() throws Exception {
        Set<State> state = new HashSet<>();
        state.add(State.ACTIVE);
        List<Task> subTasks = task.getSubTasks(state);
        assertEquals(2, subTasks.size());
    }

    @Test
    public void getParentsTest() throws Exception {
        Task task1 = this.task.getSubTasks().get(2);
        List<Task> parents = task1.getParents();
        assertEquals(2, parents.size());
    }

    @Test
    public void getRemindersTest() throws Exception {
        task.findByID(11).setReminder(100);
        task.findByID(12).setReminder(14694505460970L);
        task.findByID(13).setReminder(200);
        Map<Long, Task> reminders = task.getReminders();
        assertEquals(2, reminders.size());
    }

    @Test
    public void notifyTest() throws Exception {
        Task root = new Task(ROOT_ID).addSubTask(new Task(1));
        final boolean[] test = new boolean[1];
        root.setNotifier((a, b) -> test[0] = true);

        Task task = root.findByID(1);

        test[0] = false;
        task.setSubj("test");
        assertTrue(test[0]);

        test[0] = false;
        task.setDescription("test");
        assertTrue(test[0]);

        test[0] = false;
        task.setReminder(1);
        assertTrue(test[0]);

        test[0] = false;
        task.done();
        assertTrue(test[0]);
    }

}