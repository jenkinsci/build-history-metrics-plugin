package jenkins.plugins.model;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.List;

public class StandardDeviationMetric implements AggregateBuildMetric {

    private final String name;
    private final long metric;
    private final int occurences;

    public StandardDeviationMetric(String name, List<BuildMessage> messages) {
        this.name = name;
        this.occurences = messages.size();

        SummaryStatistics statistics = new SummaryStatistics();
        for(BuildMessage m:messages) {
            statistics.addValue(m.getDuration());
        }
        metric = (long) statistics.getStandardDeviation();
    }

    private StandardDeviationMetric(String name, long metric, int occurences) {
        this.name = name;
        this.metric = metric;
        this.occurences = occurences;
    }

    @Override
    public int getOccurences() {
        return occurences;
    }

    @Override
    public long calculateMetric() {
        return metric;
    }

    @Override
    public String getName() {
        return name;
    }

    public static Builder newBuilderFor(String name) {
       return new Builder(name); 
    }
    
    public static class Builder {
        private final SummaryStatistics statistics = new SummaryStatistics();
        private final String name;

        private Builder(String name) {
            this.name = name;
        }

        public void addMessage(BuildMessage message) {
            statistics.addValue(message.getDuration());
        }

        public StandardDeviationMetric build() {
            return new StandardDeviationMetric(name, 
                    (long) statistics.getStandardDeviation(),
                    Math.toIntExact(statistics.getN()));
        }
    }
}
