import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridEmulator {

    public static void main(String[] args) throws InterruptedException {
        int amountOfTasks = 50;
        int amountOfResources = 5;

        int minTaskDuration = 100;
        int maxTaskDuration = 500;

        List<Task> tasks = generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration);
        List<Resource> resources = generateResources(amountOfResources);

        resources.forEach(Resource::start);

        for (Task task : tasks) {
            LoadBalancer.getTargetResource(LoadBalancer.Rule.MIN_EXECUTION, task, resources).executeTask(task);
            Thread.sleep((maxTaskDuration + minTaskDuration) / (amountOfResources * 2));
        }

        while (resources.stream().filter(Resource::isBusy).count() != 0 ||
                tasks.stream().filter(Task::isCompleted).count() == 0) {
            Thread.sleep(100);
        }

        resources.forEach(Resource::shouldBeTerminated);

        long minWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
        long maxWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
        long averageWaitTime = 0;
        for (Task task : tasks) averageWaitTime += task.getWaitTime();
        averageWaitTime /= tasks.size();
        System.out.println("Min wait time: " + minWaitTime);
        System.out.println("Max wait time: " + maxWaitTime);
        System.out.println("Average wait time: " + averageWaitTime);
    }


    private static List<Task> generateTasks(int amount, int minTaskDuration, int maxTaskDuration) {
        ArrayList<Task> tasks = new ArrayList<>(amount);
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            tasks.add(new Task(random.nextInt(maxTaskDuration - minTaskDuration) + minTaskDuration));
        }
        return tasks;
    }

    private static List<Resource> generateResources(int amount) {
        ArrayList<Resource> resources = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            resources.add(new Resource());
        }
        return resources;
    }
}
