package jenkins.plugins.model;

import java.util.List;

/**
 * Created by aidan on 16/09/14.
 */
public class MTTFMetric implements AggregateBuildMetric {
    private String metricName;
    private long metricValue;
    private int occurences;

    public MTTFMetric(String metricName, List<BuildMessage> builds) {
        this.metricName = metricName;
        initialize(builds);
    }


    private void initialize(List<BuildMessage> builds) {
        long successBuildDate = 0;
        long totalSuccessTime = 0;
        for (BuildMessage build : builds) {
            String result = build.getResult();
            if (result == null) continue;

            if (!BuildMessage.BUILD_FAILED.equals(result)) {
                if (successBuildDate != 0) continue;

                successBuildDate = build.getStartTime();
                continue;
            }

            if (successBuildDate == 0) continue;

            long successLastTime = build.getStartTime() - successBuildDate;
            totalSuccessTime += successLastTime;
            occurences++;

            successBuildDate = 0;
        }
        metricValue = occurences>0?totalSuccessTime/occurences:0;
    }

    @Override
    public int getOccurences() {
        return occurences;
    }

    @Override
    public long calculateMetric() {
        return metricValue;
    }

    @Override
    public String getName() {
        return metricName;
    }
}
