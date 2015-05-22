import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridEmulator {

    public static void main(String[] args) throws InterruptedException {
        int amountOfTasks = 100;
        int amountOfResources = 5;

        int minTaskDuration = 1000;
        int maxTaskDuration = 2000;

        List<Task> tasks = generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration);
        List<Resource> resources = generateResources(amountOfResources);

        resources.forEach(Resource::start);

        for (Task task : tasks) {
            LoadBalancer.getTargetResource(task, resources).executeTask(task);
            Thread.sleep((maxTaskDuration - minTaskDuration) / amountOfResources);
        }

        while (resources.stream().filter(Resource::isBusy).count() != 0 ||
                tasks.stream().filter(Task::isCompleted).count() == 0) {
            Thread.sleep(100);
        }
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
