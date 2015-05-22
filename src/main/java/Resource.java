import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Resource extends Thread {

    private static int nextId;
    private final Queue<Task> queue;
    private int id;
    private boolean shouldBeTerminated;

    private long idleStartTime;
    private long idle;

    public Resource() {
        id = nextId++;
        queue = new LinkedBlockingQueue<>();
        shouldBeTerminated = false;
    }

    public void executeTask(Task task) {
        synchronized (queue) {
            queue.add(task);
            task.addedToTheQueue();
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
                if (idleStartTime != 0) {
                    idle += (System.nanoTime() - idleStartTime);
                    idleStartTime = 0;
                }
                task.get().execute();
                synchronized (queue) {
                    queue.remove(task.get());
                    if (!isBusy()) {
                        idleStartTime = System.nanoTime();
                    }
                }
            }
        }
    }

    public boolean isBusy() {
        return queue.size() != 0;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public long getIdle() {
        return idle;
    }

    public void shouldBeTerminated() {
        shouldBeTerminated = true;
    }

    public long getQueueDuration() {
        long timeLeft = 0;
        for (Task task : queue) {
            timeLeft += task.getExecutionTime();
        }
        return timeLeft;
    }

    @Override
    public String toString() {
        return "Resource[" + id + "]";
    }

    public void resetIdle() {
        idle = 0;
        idleStartTime = 0;
    }
}
