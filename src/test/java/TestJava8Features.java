import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJava8Features {

    @Test
    public void testOptinals() {
        Task task = new Task(1000);
        task.addedToTheQueue();
        assertFalse(task.isCompleted());
        task.execute();
        assertTrue(task.isCompleted());
    }


    @Test
    public void testLambdas() throws InterruptedException {
        List<Task> tasks = new ArrayList<Task>();
        Task task = new Task(2000);
        tasks.add(task);

        List<Resource> resources = new ArrayList<>();
        Resource resource = new Resource();
        resources.add(resource);
        resource.start();
        resource.executeTask(task);

        assertEquals(1, resources.stream().filter(Resource::isBusy).count());
        assertEquals(0, tasks.stream().filter(Task::isCompleted).count());

        while (resources.stream().filter(Resource::isBusy).count() != 0) {
            Thread.sleep(100);
        }

        assertEquals(0, resources.stream().filter(Resource::isBusy).count());
        assertEquals(1, tasks.stream().filter(Task::isCompleted).count());

    }
}
