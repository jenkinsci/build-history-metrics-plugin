package jenkins.plugins.model;

/**
 * Created by aidan on 15/09/14.
 */
public interface AggregateBuildMetric {
    int getBuildCount();

    long calculateMetric();

    String getName();
}
