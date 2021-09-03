package jenkins.plugins.model;

import com.google.common.collect.Ordering;
import hudson.model.Result;
import java.util.List;

public class MTTRMetric implements AggregateBuildMetric {
  private int buildCount;
  private long totalFailedTime;
  private String name;

  public MTTRMetric(String name, List<BuildMessage> builds) {
    this.name = name;
    initialize(Ordering.natural().sortedCopy(builds));
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

      if (!result.equals(Result.SUCCESS.toString())) {
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
  }

  @Override
  public long calculateMetric() {
    return buildCount == 0 ? 0L : totalFailedTime / buildCount;
  }

  @Override
  public String getName() {
    return name;
  }
}
