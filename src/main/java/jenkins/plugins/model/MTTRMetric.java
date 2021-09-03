package jenkins.plugins.model;

import com.google.common.collect.Ordering;
import hudson.model.Result;
import java.util.List;

/**
 * The Mean Time to Repair Metric.
 *
 * @author mcgin
 */
public class MTTRMetric implements AggregateBuildMetric {
  /** The total number of Builds that are in a failure chain. */
  private int buildCount;
  /**
   * The sum of the time between the first and last build in a failure chain.
   */
  private long totalFailedTime;
  /** The name of this metric. */
  private final String name;

  /**
   * MTTRMetric constructor.
   *
   * @param pName The name of the metric.
   * @param builds A {@link List} of {@link BuildMessage}s to calculate the
   *     Metrics from.
   */
  public MTTRMetric(final String pName, final List<BuildMessage> builds) {
    this.name = pName;
    initialize(Ordering.natural().sortedCopy(builds));
  }

  /** {@inheritDoc} */
  @Override
  public final int getOccurences() {
    return buildCount;
  }

  /**
   * Calculate the metric for the {@link List} of {@link Build}s.
   *
   * @param builds A {@link List} of {@link Build}s to calculate the Metrics
   *     from.
   */
  private void initialize(final List<BuildMessage> builds) {
    long failedBuildDate = 0;
    for (BuildMessage build : builds) {
      String result = build.getResult();
      if (result == null) {
        continue;
      }

      if (!result.equals(Result.SUCCESS.toString())) {
        if (failedBuildDate != 0) {
          continue;
        }

        failedBuildDate = build.getStartTime();
        continue;
      }

      if (failedBuildDate == 0) {
        continue;
      }

      long failedLastTime = build.getStartTime() - failedBuildDate;
      totalFailedTime += failedLastTime;
      buildCount++;

      failedBuildDate = 0;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final long calculateMetric() {
    if (buildCount == 0) {
      return 0L;
    } else {
      return totalFailedTime / buildCount;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final String getName() {
    return name;
  }
}
