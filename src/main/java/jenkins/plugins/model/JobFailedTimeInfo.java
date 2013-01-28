package jenkins.plugins.model;

import hudson.Util;

import java.util.List;
import java.util.logging.Logger;

public class JobFailedTimeInfo {
    private static final Logger LOGGER = Logger.getLogger(JobFailedTimeInfo.class.getName());

    public static final String BUILD_SUCCESS = "SUCCESS";
    private int buildCount;
    private long totalFailedTime;
    private String name;

    public JobFailedTimeInfo(String name) {
        this.name = name;
    }

    public int getBuildCount() {
        return buildCount;
    }

    public long getTotalFailedTime() {
        return totalFailedTime;
    }

    public void recordFailedTimeInfo(List<BuildMessage> builds) {
        long failedBuildDate = 0;
        for (BuildMessage build : builds) {
            String result = build.getResult();
            if (result == null) continue;

            if (!BUILD_SUCCESS.equals(result)) {
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
                Util.getPastTimeString(calcAvgFailedTime())));
    }

    public long calcAvgFailedTime() {
        if (buildCount == 0) return 0L;
        return totalFailedTime / buildCount;
    }

    public String getName() {
        return name;
    }
}
