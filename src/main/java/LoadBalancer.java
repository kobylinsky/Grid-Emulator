import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
        Resource resourceWithMinQueueDuration = resources.get(0);
        for (Resource resource : resources) {
            if (resource.getQueueDuration() < resourceWithMinQueueDuration.getQueueDuration()) {
                resourceWithMinQueueDuration = resource;
            }
        }
        return resourceWithMinQueueDuration;
    }

    private static Resource getResourceByMinQueueSize(List<Resource> resources) {
        Resource resourceWithMinQueue = resources.get(0);
        for (Resource resource : resources) {
            if (resource.getQueueDuration() < resourceWithMinQueue.getQueueDuration()) {
                resourceWithMinQueue = resource;
            }
        }
        return resourceWithMinQueue;
    }

    private static Resource getResourceByKMeans(List<Resource> resources) {
        Resource resourceWithMinQueue = resources.get(0);
        for (int i = 0; i < resources.size() / 2; i++) {
            Resource resource = resources.get(i);
            if (resource.getQueueDuration() < resourceWithMinQueue.getQueueDuration()) {
                resourceWithMinQueue = resource;
            }
        }
        return resourceWithMinQueue;
    }

    enum Rule {
        RANDOM, ROUND_ROBIN, MIN_EXECUTION, MIN_QUEUE, K_MEANS
    }

}
