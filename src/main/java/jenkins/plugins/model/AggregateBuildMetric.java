package jenkins.plugins.model;

public interface AggregateBuildMetric {
    int getOccurences();

    long calculateMetric();

    String getName();
}
