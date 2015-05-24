import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadBalancer {

    private static final Map<Resource, Long> resourcesDurationCache = new HashMap<>();
    private static int roundRobinLastAssignedResourceId = -1;
    private static long resourceCacheUpdateTime;

    public static Resource getTargetResource(Rule rule, Task task, List<Resource> resources) {
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
            case ADAPTIVE:
                return getResourceAdaptively(task, resources);
            default:
                throw new NotImplementedException();
        }
    }

    private static Resource getResourceByRandom(List<Resource> resources) {
        return resources.get((int) (Math.random() * resources.size()));
    }

    private static Resource getResourceByRoundRobin(List<Resource> resources) {
        return resources.get(++roundRobinLastAssignedResourceId < resources.size() ?
                roundRobinLastAssignedResourceId : (roundRobinLastAssignedResourceId = 0));
    }

    private static Resource getResourceByMinExecution(List<Resource> resources) {
        Resource resourceWithMinQueueDuration = resources.get(0);
        for (Resource resource : resources) {
            if (resource == resourceWithMinQueueDuration) continue;
            if (resource.getQueueDuration() < resourceWithMinQueueDuration.getQueueDuration()) {
                resourceWithMinQueueDuration = resource;
            }
        }
        return resourceWithMinQueueDuration;
    }

    private static Resource getResourceByMinQueueSize(List<Resource> resources) {
        Resource resourceWithMinQueue = resources.get(0);
        for (Resource resource : resources) {
            if (resource == resourceWithMinQueue) continue;
            if (resource.getQueueSize() < resourceWithMinQueue.getQueueSize()) {
                resourceWithMinQueue = resource;
            }
        }
        return resourceWithMinQueue;
    }

    private static Resource getResourceByKMeans(List<Resource> resources) {
        Collections.shuffle(resources);
        Resource resourceWithMinQueue = resources.get(0);
        for (int i = 0; i < resources.size() / 2; i++) {
            Resource resource = resources.get(i);
            if (resource == resourceWithMinQueue) continue;
            if (resource.getQueueDuration() < resourceWithMinQueue.getQueueDuration()) {
                resourceWithMinQueue = resource;
            }
        }
        return resourceWithMinQueue;
    }

    private static Resource getResourceAdaptively(Task task, List<Resource> resources) {
        if (System.currentTimeMillis() - resourceCacheUpdateTime > task.getExecutionTime() ||
                resourceCacheUpdateTime == 0) {
            resourcesDurationCache.clear();
            for (Resource resource : resources) {
                resourcesDurationCache.put(resource, resource.getQueueDuration());
            }
            resourceCacheUpdateTime = System.currentTimeMillis();
            //System.out.println("Updated cache:" + resourcesDurationCache);
        }

        Resource targetResource = resourcesDurationCache.keySet().stream().findFirst().get();
        long targetResourceFutureQueueDuration = (long) (resourcesDurationCache.get(targetResource) +
                task.getExecutionTime() / targetResource.getProcessingRate());

        for (Resource resource : resourcesDurationCache.keySet()) {
            if (resource == targetResource) continue;
            long resourceFutureQueueDuration = (long) (resourcesDurationCache.get(resource) +
                    task.getExecutionTime() / resource.getProcessingRate());
            if (resourceFutureQueueDuration < targetResourceFutureQueueDuration) {
                targetResource = resource;
                targetResourceFutureQueueDuration = resourceFutureQueueDuration;
            }
        }
        resourcesDurationCache.put(targetResource, targetResourceFutureQueueDuration);
        return targetResource;
    }

    enum Rule {
        RANDOM, ROUND_ROBIN, MIN_EXECUTION, MIN_QUEUE, K_MEANS, ADAPTIVE
    }

}
