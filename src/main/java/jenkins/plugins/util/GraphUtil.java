package jenkins.plugins.util;

import java.util.List;
import java.util.logging.Logger;
import jenkins.plugins.annotations.ExcludeFromCoverageFakeGenerated;
import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Utilities to generate graphs for metrics.
 *
 * @author mcgin
 */
public final class GraphUtil {
  /** Logger for logging message. */
  private static final Logger LOGGER =
      Logger.getLogger(GraphUtil.class.getName());

  /**
   * GraphUtil Constructor, private as Utilities classes should not be
   * constructed.
   */
  @ExcludeFromCoverageFakeGenerated
  private GraphUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Generates a Standard Deviation Graph.
   *
   * @param title The Title of the {@link JFreeChart}.
   * @param buildMessages A {@link List} of {@link BuildMessage}s.
   * @return The generated {@link JFreeChart}.
   */
  public static JFreeChart generateStdDevGraph(
      final String title, final List<BuildMessage> buildMessages) {
    String chartTitle = title;
    String xAxisLabel = "X";
    String yAxisLabel = "Y";

    XYDataset dataset = createDataset(buildMessages);

    return ChartFactory.createXYLineChart(
        chartTitle,
        xAxisLabel,
        yAxisLabel,
        dataset,
        PlotOrientation.HORIZONTAL,
        false,
        false,
        false);
  }

  /**
   * Create a Graph {@link XYDataset} for a {@link List} of {@link
   * BuildMessage}s.
   *
   * @param buildMessages A {@link List} of {@link BuildMessage}s.
   * @return The generated {@link XYDataset}.
   */
  private static XYDataset createDataset(
      final List<BuildMessage> buildMessages) {
    XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeries series1 = new XYSeries("Object 1");

    // Need to optimise this, it's O(n^2)
    for (int i = 0; i < buildMessages.size(); i++) {

      List<BuildMessage> sublist =
          buildMessages.subList(i, buildMessages.size());
      LOGGER.info(sublist.get(0).toString());
      StandardDeviationMetric metric =
          new StandardDeviationMetric("Graph", sublist);
      series1.add(metric.calculateMetric(), i);
    }

    dataset.addSeries(series1);

    return dataset;
  }
}
