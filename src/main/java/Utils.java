import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
    static List<Task> generateTasks(int amount, int minTaskDuration, int maxTaskDuration) {
        ArrayList<Task> tasks = new ArrayList<>(amount);
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            tasks.add(new Task(random.nextInt(maxTaskDuration - minTaskDuration) + minTaskDuration));
        }
        return tasks;
    }

    static List<Resource> generateResources(int amount, double fluctuationRate, int maxPingTime) {
        ArrayList<Resource> resources = new ArrayList<>(amount);
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            Resource resource = new Resource(1 - fluctuationRate + 2f * fluctuationRate * random.nextDouble(), random.nextInt(maxPingTime));
            resources.add(resource);
        }
        return resources;
    }


    public static long[] sum(long[] a, long[] b) {
        int aLen = a.length;
        long[] c = new long[aLen];
        for (int i = 0; i < aLen; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    public static long[] devide(long[] a, int d) {
        for (int i = 0; i < a.length; i++) {
            a[i] /= d;
        }
        return a;
    }

}
