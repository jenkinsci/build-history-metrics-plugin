package jenkins.plugins.model;

import java.util.List;

public class StandardDeviationMetric implements AggregateBuildMetric {

    private final String name;
    private final long metric;

    public StandardDeviationMetric(String name, List<BuildMessage> messages) {
        this.name = name;
        metric = 0L;

    }

    @Override
    public int getOccurences() {
        return 1;
    }

    @Override
    public long calculateMetric() {
        return metric;
    }

    @Override
    public String getName() {
        return name;
    }
}
