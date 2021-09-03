package jenkins.plugins.model;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * The Standard Deviation Fail Metric.
 *
 * @author mcgin
 */
public class StandardDeviationMetric implements AggregateBuildMetric {
  /** The name of this metric. */
  private final String name;
  /** The pre calculated metric value. */
  private final long metric;
  /** The total number of Builds that are in a failure chain. */
  private final int occurences;

  /**
   * StandardDeviationMetric constructor.
   *
   * @param pName The name of the metric.
   * @param messages A {@link List} of {@link BuildMessage}s to calculate the
   *     Metrics from.
   */
  public StandardDeviationMetric(
      final String pName, final List<BuildMessage> messages) {
    this.name = pName;
    this.occurences = messages.size();

    SummaryStatistics statistics = new SummaryStatistics();
    messages.forEach(m -> statistics.addValue(m.getDuration()));
    metric = (long) statistics.getStandardDeviation();
  }

  /** {@inheritDoc} */
  @Override
  public final int getOccurences() {
    return occurences;
  }

  /** {@inheritDoc} */
  @Override
  public final long calculateMetric() {
    return metric;
  }

  /** {@inheritDoc} */
  @Override
  public final String getName() {
    return name;
  }
}
