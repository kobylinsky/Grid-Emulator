import java.util.List;

public class LoadBalancer {

    public static Resource getTargetResource(Task task, List<Resource> resources) {
        return resources.get((int) (Math.random() * resources.size()));
    }
}
