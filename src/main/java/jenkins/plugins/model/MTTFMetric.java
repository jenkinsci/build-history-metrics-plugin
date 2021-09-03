package jenkins.plugins.model;

import com.google.common.collect.Ordering;
import hudson.model.Result;
import java.util.List;

/**
 * The Mean Time to Fail Metric.
 *
 * @author mcgin
 */
public class MTTFMetric implements AggregateBuildMetric {
  /** The name of this metric. */
  private final String metricName;
  /** The pre calculated metric value. */
  private long metricValue;
  /** The total number of Builds that are in a failure chain. */
  private int occurences;

  /**
   * MTTFMetric constructor.
   *
   * @param pMetricName The name of the metric.
   * @param builds A {@link List} of {@link BuildMessage}s to calculate the
   *     Metrics from.
   */
  public MTTFMetric(final String pMetricName, final List<BuildMessage> builds) {
    this.metricValue = 0;
    this.metricName = pMetricName;
    initialize(Ordering.natural().sortedCopy(builds));
  }

  /**
   * Calculate the metric for the {@link List} of {@link Build}s.
   *
   * @param builds A {@link List} of {@link Build}s to calculate the Metrics
   *     from.
   */
  private void initialize(final List<BuildMessage> builds) {
    long successBuildDate = 0;
    long totalSuccessTime = 0;
    for (BuildMessage build : builds) {
      String result = build.getResult();
      if (result == null) {
        continue;
      }

      if (!result.equals(Result.FAILURE.toString())) {
        if (successBuildDate != 0) {
          continue;
        }

        successBuildDate = build.getStartTime();
        continue;
      }

      if (successBuildDate == 0) {
        continue;
      }

      long successLastTime = build.getStartTime() - successBuildDate;
      totalSuccessTime += successLastTime;
      occurences++;

      successBuildDate = 0;
    }
    if (occurences > 0) {
      metricValue = totalSuccessTime / occurences;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final int getOccurences() {
    return occurences;
  }

  /** {@inheritDoc} */
  @Override
  public final long calculateMetric() {
    return metricValue;
  }

  /** {@inheritDoc} */
  @Override
  public final String getName() {
    return metricName;
  }
}
