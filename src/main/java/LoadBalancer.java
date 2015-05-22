import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.List;

public class LoadBalancer {

    private static int lastAssignedResourceId = -1;

    public static Resource getTargetResource(Rule rule, List<Resource> resources) {
        switch (rule) {
            case RANDOM:
                return getResourceByRandom(resources);
            case ROUND_ROBIN:
                return getResourceByRoundRobin(resources);
            case MIN_EXECUTION:
                return getResourceByMinExecution(resources);
            case MIN_QUEUE:
                return getResourceByMinQueueSize(resources);
            case K_MEANS:
                return getResourceByKMeans(resources);
            default:
                throw new NotImplementedException();
        }
    }

    private static Resource getResourceByRandom(List<Resource> resources) {
        return resources.get((int) (Math.random() * resources.size()));
    }

    private static Resource getResourceByRoundRobin(List<Resource> resources) {
        return resources.get(++lastAssignedResourceId < resources.size() ? lastAssignedResourceId : (lastAssignedResourceId = 0));
    }

    private static Resource getResourceByMinExecution(List<Resource> resources) {
        return resources.stream().min((r1, r2) -> Long.compare(r1.getQueueDuration(), r2.getQueueDuration())).get();
    }

    private static Resource getResourceByMinQueueSize(List<Resource> resources) {
        return resources.stream().min((r1, r2) -> Long.compare(r1.getQueueSize(), r2.getQueueSize())).get();
    }

    private static Resource getResourceByKMeans(List<Resource> resources) {
        Collections.shuffle(resources);
        return resources.stream().limit(3).min((r1, r2) -> Long.compare(r1.getQueueSize(), r2.getQueueSize())).get();
    }

    enum Rule {
        RANDOM, ROUND_ROBIN, MIN_EXECUTION, MIN_QUEUE, K_MEANS
    }

}
