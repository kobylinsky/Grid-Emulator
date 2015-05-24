import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridEmulator {

    private static final Map<LoadBalancer.Rule, long[]> RESULTS = new HashMap<>();

    public static void main(String[] args) throws Exception {
        LoadBalancer.Rule[] rules = LoadBalancer.Rule.values();
        int experiments = 5;

        int amountOfTasks = 100;
        int amountOfResources = 5;

        int minTaskDuration = 400;
        int maxTaskDuration = 600;
        int cacheUpdateInterval = 500;

        int maxPingTime = 10;
        int maxDataAccessTime = 50;

        String fileName = String.format("./%s.csv", String.format("%d_%dres_%dtasks_%ddur",
                System.currentTimeMillis(), amountOfResources, amountOfTasks, (minTaskDuration + maxTaskDuration) / 2));
        String reportHeader = "Алгоритм, Загальний час (мс), Середній час очікування (мс), Максимальний час очікування (мс), Середній час простою (мс), Максимальний час простою (мс)";

        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        bufferedWriter.write(reportHeader);
        bufferedWriter.newLine();

        for (int i = 0; i < experiments; i++) {
            List<Resource> resources = Utils.generateResources(amountOfResources, 0.5d, maxPingTime);
            List<Task> tasks = Utils.generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration, resources, maxDataAccessTime);

            resources.forEach(Resource::start);
            for (final LoadBalancer.Rule rule : rules) {
                tasks.forEach(Task::reset);
                resources.forEach(Resource::reset);

                long[] results = getResults(rule, tasks, resources, minTaskDuration, maxTaskDuration, cacheUpdateInterval);
                if (RESULTS.get(rule) == null) RESULTS.put(rule, results);
                else RESULTS.put(rule, Utils.sum(RESULTS.get(rule), results));
            }

            resources.forEach(Resource::shouldBeTerminated);
            System.out.println(String.format("Experiment %d completed.", i));
        }

        for (LoadBalancer.Rule rule : RESULTS.keySet()) {
            long[] finalResults = Utils.divide(RESULTS.get(rule), experiments);
            //RESULTS.put(rule, finalResults);

            final String stats = rule.toString() + ", " + finalResults[0] + ", " + finalResults[1] + ", " +
                    finalResults[2] + ", " + finalResults[3] + ", " + finalResults[4];

            bufferedWriter.write(stats);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }

    private static long[] getResults(LoadBalancer.Rule rule, List<Task> tasks, List<Resource> resources, long minTaskDuration, long maxTaskDuration, long cacheUpdateInterval) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        for (Task task : tasks) {
            LoadBalancer.getTargetResource(rule, task, resources, cacheUpdateInterval).executeTask(task);
            Thread.sleep((maxTaskDuration + minTaskDuration) / (resources.size() * 2));
        }
        while (tasks.stream().filter(Task::isCompleted).count() != tasks.size()) {
            Thread.sleep(10);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        final long maxWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
        long avgWaitTime = 0;
        for (Task task : tasks) avgWaitTime += task.getWaitTime();
        avgWaitTime /= tasks.size();

        final long maxIdleTime = resources.stream().max((r1, r2) -> Long.compare(r1.getIdle(), r2.getIdle())).get().getIdle();
        long avgIdleTime = 0;
        for (Resource resource : resources) avgIdleTime += resource.getIdle();
        avgIdleTime /= resources.size();

        return new long[]{duration, avgWaitTime, maxWaitTime, avgIdleTime, maxIdleTime};
    }
}
