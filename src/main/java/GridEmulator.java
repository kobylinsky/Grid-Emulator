import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class GridEmulator {

    public static void main(String[] args) throws Exception {
        LoadBalancer.Rule[] rules = new LoadBalancer.Rule[] {
                LoadBalancer.Rule.RANDOM,
                LoadBalancer.Rule.ROUND_ROBIN,
                LoadBalancer.Rule.MIN_EXECUTION,
                LoadBalancer.Rule.MIN_QUEUE,
                LoadBalancer.Rule.K_MEANS };

        int experiments = 20;

        int amountOfTasks = 1000;
        int amountOfResources = 10;

        double resourcesFluctuationRate = 0.5d;
        int minTaskDuration = 800;
        int maxTaskDuration = 1200;
        int cacheUpdateInterval = 1000;

        int maxPingTime = 15;
        int maxDataAccessTime = 10;

        int queueAppendingInterval = (maxTaskDuration + minTaskDuration) / (amountOfResources * 2);

        String reportHeader = "��������, ��������� ��� (��), ������� ��� ���������� (��), ������������ ��� ���������� (��), ������� ��� ������� (��), ������������ ��� ������� (��)";
        String fileName = String.format("./%s.csv",
                String.format("%d_%dres_%dtasks_%ddur", System.currentTimeMillis(), amountOfResources, amountOfTasks, (minTaskDuration + maxTaskDuration) / 2));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "Cp1251"));
        bufferedWriter.write(reportHeader);
        bufferedWriter.newLine();

        for (int i = 1; i <= experiments; i++) {
            List<Resource> resources = Utils.generateResources(amountOfResources, resourcesFluctuationRate, maxPingTime);
            System.out.println(resources);
            List<Task> tasks = Utils.generateTasks(amountOfTasks, minTaskDuration, maxTaskDuration, resources, maxDataAccessTime);
            resources.forEach(Resource::start);

            for (final LoadBalancer.Rule rule : rules) {
                tasks.forEach(Task::reset);
                resources.forEach(Resource::reset);

                long[] results = executeAllTasks(rule, tasks, resources, cacheUpdateInterval, queueAppendingInterval);

                bufferedWriter.write(rule.toString() + ", " + results[0] + ", " + results[1] + ", " + results[2] + ", " + results[3] + ", " + results[4]);
                bufferedWriter.newLine();
                System.out.println(String.format("Experiment %d/%d for rule %s completed.", i, experiments, rule));
            }
            resources.forEach(Resource::shouldBeTerminated);
        }

        bufferedWriter.close();
    }

    private static long[] executeAllTasks(LoadBalancer.Rule rule, List<Task> tasks, List<Resource> resources, long cacheUpdateInterval,
            long queueAppendingInterval) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            LoadBalancer.getTargetResource(rule, task, resources, cacheUpdateInterval).executeTask(task);
            Thread.sleep(queueAppendingInterval);
        }
        while ((tasks.stream().filter(Task::isCompleted).count()) != tasks.size()) {
            Thread.sleep(25);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        final long maxWaitTime = tasks.stream().max((t1, t2) -> Long.compare(t1.getWaitTime(), t2.getWaitTime())).get().getWaitTime();
        long avgWaitTime = 0;
        for (Task task : tasks)
            avgWaitTime += task.getWaitTime();
        avgWaitTime /= tasks.size();

        final long maxIdleTime = resources.stream().max((r1, r2) -> Long.compare(r1.getIdle(), r2.getIdle())).get().getIdle();
        long avgIdleTime = 0;
        for (Resource resource : resources)
            avgIdleTime += resource.getIdle();
        avgIdleTime /= resources.size();

        return new long[] { duration, avgWaitTime, maxWaitTime, avgIdleTime, maxIdleTime };
    }
}
