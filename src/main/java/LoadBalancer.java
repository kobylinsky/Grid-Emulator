import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class LoadBalancer {

    public static Resource getTargetResource(Rule rule, Task task, List<Resource> resources) {
        switch (rule) {
            case RANDOM:
                return resources.get((int) (Math.random() * resources.size()));
            default:
                throw new NotImplementedException();
        }
    }

    enum Rule {
        RANDOM, ROUND_ROBIN, MIN_EXECUTION, MIN_QUEUE, K_MEANS
    }

}
