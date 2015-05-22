import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridEmulator {

    public static void main(String[] args) throws InterruptedException {
        int amountOfTasks = 1000;
        int amountOfResources = 10;

        int minTaskDuration = 100;
        int maxTaskDuration = 500;

        List<Task> tasks = generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration);
        List<Resource> resources = generateResources(amountOfResources);

        resources.forEach(Resource::start);

        for (LoadBalancer.Rule rule : LoadBalancer.Rule.values()) {
            System.out.println("==============================");
            System.out.println("LB algorithm:" + rule);
            for (Task task : tasks) {
                LoadBalancer.getTargetResource(rule, resources).executeTask(task);
                Thread.sleep((maxTaskDuration + minTaskDuration) / (amountOfResources * 2));
            }

            while (resources.stream().filter(Resource::isBusy).count() != 0 ||
                    tasks.stream().filter(Task::isCompleted).count() == 0) {
                Thread.sleep(100);
            }


            long minWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
            long maxWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
            long averageWaitTime = 0;
            for (Task task : tasks) averageWaitTime += task.getWaitTime();
            averageWaitTime /= tasks.size();
            System.out.println("Min wait time: " + minWaitTime);
            System.out.println("Max wait time: " + maxWaitTime);
            System.out.println("Average wait time: " + averageWaitTime);
            System.out.println("==============================");
        }

        resources.forEach(Resource::shouldBeTerminated);
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
