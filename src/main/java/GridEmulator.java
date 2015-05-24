import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridEmulator {

    private static final Map<LoadBalancer.Rule, long[]> RESULTS = new HashMap<>();

    public static void main(String[] args) throws Exception {
        LoadBalancer.Rule[] rules = new LoadBalancer.Rule[]{
                LoadBalancer.Rule.RANDOM,
                LoadBalancer.Rule.ROUND_ROBIN,
                LoadBalancer.Rule.MIN_EXECUTION,
                LoadBalancer.Rule.MIN_QUEUE,
                LoadBalancer.Rule.K_MEANS};

        int experiments = 10;

        int amountOfTasks = 1000;
        int amountOfResources = 10;

        double resourcesFluctuationRate = 0.5d;
        int minTaskDuration = 800;
        int maxTaskDuration = 1200;
        int cacheUpdateInterval = 1000;

        int maxPingTime = 15;
        int maxDataAccessTime = 10;

        int queueAppendingInterval = (maxTaskDuration + minTaskDuration) / (amountOfResources * 2);

        String fileName = String.format("./%s.csv", String.format("%d_%dres_%dtasks_%ddur",
                System.currentTimeMillis(), amountOfResources, amountOfTasks, (minTaskDuration + maxTaskDuration) / 2));
        String reportHeader = "Алгоритм, Загальний час (мс), Середній час очікування (мс), Максимальний час очікування (мс), Середній час простою (мс), Максимальний час простою (мс)";

        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
        bufferedWriter.write(reportHeader);
        bufferedWriter.newLine();

        for (int i = 0; i < experiments; i++) {
            List<Resource> resources = Utils.generateResources(amountOfResources, resourcesFluctuationRate, maxPingTime);
            List<Task> tasks = Utils.generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration, resources, maxDataAccessTime);

            resources.forEach(Resource::start);
            for (final LoadBalancer.Rule rule : rules) {
                tasks.forEach(Task::reset);
                resources.forEach(Resource::reset);

                long[] results = executeAllTasks(rule, tasks, resources, cacheUpdateInterval,
                        queueAppendingInterval);
                if (RESULTS.get(rule) == null) RESULTS.put(rule, results);
                else RESULTS.put(rule, Utils.sum(RESULTS.get(rule), results));
                System.out.println(String.format("Experiment %d/%d for rule %s completed.", i, experiments, rule));
            }

            resources.forEach(Resource::shouldBeTerminated);
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

    private static long[] executeAllTasks(LoadBalancer.Rule rule, List<Task> tasks, List<Resource> resources,
                                          long cacheUpdateInterval, long queueAppendingInterval) throws InterruptedException {
        long updateInterval = 5000;
        long lastOutput = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            LoadBalancer.getTargetResource(rule, task, resources, cacheUpdateInterval).executeTask(task);
            Thread.sleep(queueAppendingInterval);
            if (System.currentTimeMillis() - lastOutput >= updateInterval) {
                System.out.println(String.format("Tasks queued: %d/%d", i, tasks.size()));
                lastOutput = System.currentTimeMillis();
            }

        }
        long completedTasks;

        while ((completedTasks = tasks.stream().filter(Task::isCompleted).count()) != tasks.size()) {
            if (System.currentTimeMillis() - lastOutput >= updateInterval) {
                System.out.println(String.format("Completed tasks: %d/%d", completedTasks, tasks.size()));
                lastOutput = System.currentTimeMillis();
            }
            Thread.sleep(25);
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
