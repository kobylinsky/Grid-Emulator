import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Resource extends Thread {

    private static int nextId;
    private final Queue<Task> queue;
    private int id;
    private boolean shouldBeTerminated;

    public Resource() {
        id = nextId++;
        queue = new LinkedBlockingQueue<>();
        shouldBeTerminated = false;
    }

    public void executeTask(Task task) {
        synchronized (queue) {
            queue.add(task);
            task.addedToTheQueue();
            System.out.println(task + " will be executed by " + this);
        }
    }

    @Override
    public void run() {
        while (!shouldBeTerminated) {
            Optional<Task> task;
            synchronized (queue) {
                task = Optional.ofNullable(queue.peek());
            }
            if (task.isPresent()) {
                task.get().execute();
                synchronized (queue) {
                    queue.remove(task.get());
                }
            }
        }
    }

    public boolean isBusy() {
        return queue.size() != 0;
    }


    @Override
    public String toString() {
        return "Resource[" + id + "]";
    }
}
