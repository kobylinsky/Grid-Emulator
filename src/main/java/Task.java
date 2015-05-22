import java.util.Optional;

public class Task {

    private static int nextId;
    private int id;
    private long executionTime;

    private Optional<Long> startTime = Optional.empty();
    private Optional<Long> endTime = Optional.empty();

    public Task(int executionTime) {
        id = nextId++;
        this.executionTime = executionTime;
    }

    public void addedToTheQueue() {
        startTime = Optional.of(System.nanoTime());
    }

    public long getWaitTime() {
        return endTime.get() - startTime.get() - executionTime;
    }

    public boolean isCompleted() {
        return endTime.isPresent();
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void execute() {
        if (!startTime.isPresent()) throw new IllegalStateException("Task should be added to the queue first!");
        try {
            Thread.sleep(getExecutionTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = Optional.of(System.nanoTime());
        //System.out.println(this + " completed");
    }

    @Override
    public String toString() {
        return "Task[" + id + "]:{" + executionTime + "}";
    }
}
