import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridEmulator {

    public static void main(String[] args) throws Exception {
        int amountOfTasks = 20;
        int amountOfResources = 5;

        int minTaskDuration = 400;
        int maxTaskDuration = 600;

        int minPingTime = 10;
        int maxPingTime = 50;

        List<Task> tasks = generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration);
        List<Resource> resources = generateResources(amountOfResources, minPingTime, maxPingTime);

        String fileName = String.format("./%s.csv", System.currentTimeMillis());
        String reportHeader = "Алгоритм, Середній час очікування (мс), Максимальний час очікування (мс), " +
                "Середній час простою (мс), Максимальний час простою (мс)";

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "windows-1251"));
        bufferedWriter.write(reportHeader);
        bufferedWriter.newLine();

        resources.forEach(Resource::start);
        for (LoadBalancer.Rule rule : LoadBalancer.Rule.values()) {
            resources.forEach(Resource::resetIdle);

            for (Task task : tasks) {
                LoadBalancer.getTargetResource(rule, resources).executeTask(task);
                Thread.sleep((maxTaskDuration + minTaskDuration) / (amountOfResources * 2));
            }
            while (resources.stream().filter(Resource::isBusy).count() != 0 ||
                    tasks.stream().filter(Task::isCompleted).count() == 0) {
                Thread.sleep(10);
            }

            long maxWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime() / 1000000;
            long avgWaitTime = 0;
            for (Task task : tasks) avgWaitTime += task.getWaitTime();
            avgWaitTime /= amountOfTasks * 1000000;

            long maxIdleTime = resources.stream().max((r1, r2) -> Long.compare(r1.getIdle(), r2.getIdle())).get().getIdle() / 1000000;
            long avgIdleTime = 0;
            for (Resource resource : resources) avgIdleTime += resource.getIdle();
            avgIdleTime /= amountOfResources * 1000000;

            final String stats = rule.toString() + ", " + avgWaitTime + ", " + maxWaitTime + ", " + avgIdleTime + ", " + maxIdleTime;
            System.out.println(stats);
            bufferedWriter.write(stats);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
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

    private static List<Resource> generateResources(int amount, int minPingTIme, int maxPingTime) {
        ArrayList<Resource> resources = new ArrayList<>(amount);
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            Resource resource = new Resource();
            resource.setPingTime(random.nextInt(maxPingTime - minPingTIme) + minPingTIme);
            resources.add(resource);
        }
        return resources;
    }
}
