import java.util.Optional;

public class Task {

    private static int nextId;
    private int id;
    private long executionTime;

    private Optional<Long> startTime = Optional.empty();
    private Optional<Long> endTime = Optional.empty();
    private Optional<Long> waitTime = Optional.empty();

    public Task(int executionTime) {
        id = nextId++;
        this.executionTime = executionTime;
    }

    public void addedToTheQueue() {
        startTime = Optional.of(System.currentTimeMillis());
    }

    public long getWaitTime() {
        return waitTime.get();
    }

    public boolean isCompleted() {
        return endTime.isPresent();
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void execute(double processingSpeedFactor) {
        if (!startTime.isPresent()) throw new IllegalStateException("Task should be added to the queue first!");
        try {
            Thread.sleep((long) (executionTime / processingSpeedFactor));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = Optional.of(System.currentTimeMillis());
        waitTime = Optional.of(endTime.get() - startTime.get() - (long) (executionTime / processingSpeedFactor));
        //System.out.println(this + " completed");
    }

    @Override
    public String toString() {
        return "Task[" + id + "]:{" + executionTime + "}";
    }

    public void reset() {
        startTime = Optional.empty();
        endTime = Optional.empty();
        waitTime = Optional.empty();
    }
}
