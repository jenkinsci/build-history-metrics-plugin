package jenkins.plugins.model;

import hudson.Util;

import java.util.List;
import java.util.logging.Logger;

public class MTTRMetric implements AggregateBuildMetric {
    private static final Logger LOGGER = Logger.getLogger(MTTRMetric.class.getName());


    private int buildCount;
    private long totalFailedTime;
    private String name;

    public MTTRMetric(String name, List<BuildMessage> builds) {
        this.name = name;
        initialize(builds);
    }

    @Override
    public int getOccurences() {
        return buildCount;
    }

    private void initialize(List<BuildMessage> builds) {
        long failedBuildDate = 0;
        for (BuildMessage build : builds) {
            String result = build.getResult();
            if (result == null) continue;

            if (!BuildMessage.BUILD_SUCCESS.equals(result)) {
                if (failedBuildDate != 0) continue;

                failedBuildDate = build.getStartTime();
                continue;
            }

            if (failedBuildDate == 0) continue;

            long failedLastTime = build.getStartTime() - failedBuildDate;
            totalFailedTime += failedLastTime;
            buildCount++;

            failedBuildDate = 0;
        }

        LOGGER.info(String.format("%s buildCount : %d%n", name, buildCount));
        LOGGER.info(String.format("%s totalFailedTime : %s%n", name,
                Util.getPastTimeString(totalFailedTime)));
        LOGGER.info(String.format("%s average failed  time : %s%n", name,
                Util.getPastTimeString(calculateMetric())));
    }

    @Override
    public long calculateMetric() {
        if (buildCount == 0) return 0L;
        return totalFailedTime / buildCount;
    }

    @Override
    public String getName() {
        return name;
    }
}
