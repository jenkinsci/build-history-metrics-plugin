package jenkins.plugins.model;

/**
 * AggregateBuildMetric Interface.
 *
 * @author mcgin
 */
public interface AggregateBuildMetric {
  /**
   * The number of qualifying {@link Build}s for the metric.
   *
   * @return Number of {@link Build}s.
   */
  int getOccurences();
  /**
   * Calculate the value of the metric.
   *
   * @return The metric value.
   */
  long calculateMetric();
  /**
   * The name of this metric.
   *
   * @return The metric name.
   */
  String getName();
}
