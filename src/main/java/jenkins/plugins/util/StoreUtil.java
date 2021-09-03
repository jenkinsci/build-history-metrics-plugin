package jenkins.plugins.util;

import com.google.common.io.Files;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jenkins.plugins.annotations.ExcludeFromCoverageFakeGenerated;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.JFreeChart;

/**
 * Utilities to Save info files and graphs.
 *
 * @author mcgin
 */
public final class StoreUtil {
  /** Logger for logging message. */
  private static final Logger LOGGER =
      Logger.getLogger(StoreUtil.class.getName());
  /** WIDTH for the generated Image. */
  public static final int IMG_PX_WIDTH = 500;
  /** HEIGHT for the generated Image. */
  public static final int IMG_PX_HEIGHT = 500;
  /** The extension for the image. */
  public static final String IMG_TYPE = "png";
  /** Name of the Mean Time to Repair properties file. */
  public static final String MTTR_PROPERTY_FILE = "mttr.properties";
  /** Name of the Mean Time to Fail properties file. */
  public static final String MTTF_PROPERTY_FILE = "mttf.properties";
  /** Name of the Standard Deviation properties file. */
  public static final String STDDEV_PROPERTY_FILE = "stddev.properties";
  /** Name of the Mean Time to Repair graph file. */
  public static final String MTTR_GRAPH_FILE = "mttr." + IMG_TYPE;
  /** Name of the Mean Time to Fail graph file. */
  public static final String MTTF_GRAPH_FILE = "mttf." + IMG_TYPE;
  /** Name of the Standard Deviation graph file. */
  public static final String STDDEV_GRAPH_FILE = "stddev." + IMG_TYPE;

  /**
   * StoreUtil Constructor, private as Utilities classes should not be
   * constructed.
   */
  @ExcludeFromCoverageFakeGenerated
  private StoreUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Store {@link Run} info in to a {@link File}.
   *
   * @param storeFile The {@link File} to store messages in.
   * @param build The {@link Run} to store in the file.
   */
  public static void storeBuildMessages(final File storeFile, final Run build) {
    try {
      if (storeFile.exists()) {
        appendBuildMessageToFile(build, storeFile);
      } else {
        appendAJobsBuildMessageHistoryToFile(build.getParent(), storeFile);
      }
    } catch (IOException e) {
      LOGGER.warning(
          String.format("store build messages error : %s", e.getMessage()));
    }
  }

  /**
   * Store Build metrics for the {@link Run}.
   *
   * @param metricType The metric type class for the metric we are storing.
   * @param run The {@link Run} that is being stored.
   * @param buildMetrics Metrics to store to the file.
   */
  public static void storeBuildMetric(
      final Class metricType,
      final Run run,
      final AggregateBuildMetric... buildMetrics) {
    try {
      StringBuilder fileContent = new StringBuilder();

      for (AggregateBuildMetric buildMetric : buildMetrics) {
        fileContent
            .append(buildMetric.getName())
            .append("=")
            .append(buildMetric.calculateMetric())
            .append("\n");
      }

      String propertyFilename = getPropertyFilename(metricType);
      File propertiesFile =
          new File(
              run.getParent().getRootDir().getAbsolutePath()
                  + File.separator
                  + propertyFilename);
      Files.write(
          fileContent.toString(), propertiesFile, StandardCharsets.UTF_8);

    } catch (IOException e) {
      LOGGER.warning(String.format("store property error:%s", e.getMessage()));
    }
  }

  /**
   * Store a graph for the {@link Run}.
   *
   * @param metricType The metric type class for the metric we are storing.
   * @param run The {@link Run} that is being stored.
   * @param chart The {@link JFreeChart} to save.
   */
  public static void storeGraph(
      final Class metricType, final Run run, final JFreeChart chart) {
    try {
      String graphFileName = getGraphFilename(metricType);
      File graphFile =
          new File(
              run.getParent().getRootDir().getAbsolutePath()
                  + File.separator
                  + graphFileName);

      ImageIO.write(
          chart.createBufferedImage(IMG_PX_WIDTH, IMG_PX_HEIGHT),
          IMG_TYPE,
          graphFile);
    } catch (IOException e) {
      LOGGER.warning(String.format("store property error:%s", e.getMessage()));
    }
  }

  /**
   * Return the name of the {@link Class}'s property file.
   *
   * @param metricType The metric type class who's name we want.
   * @return The Name for the Properties File for this {@link Class}.
   */
  public static String getPropertyFilename(final Class metricType) {
    if (metricType == MTTFMetric.class) {
      return MTTF_PROPERTY_FILE;
    } else if (metricType == MTTRMetric.class) {
      return MTTR_PROPERTY_FILE;
    } else if (metricType == StandardDeviationMetric.class) {
      return STDDEV_PROPERTY_FILE;
    } else {
      throw new IllegalArgumentException(
          "No property file mapping for metric - " + metricType);
    }
  }

  /**
   * Return the name of the {@link Class}'s graph file.
   *
   * @param metricType The metric type class who's name we want.
   * @return The Name for the Properties File for this {@link Class}.
   */
  public static String getGraphFilename(final Class metricType) {
    if (metricType == MTTFMetric.class) {
      return MTTF_GRAPH_FILE;
    } else if (metricType == MTTRMetric.class) {
      return MTTR_GRAPH_FILE;
    } else if (metricType == StandardDeviationMetric.class) {
      return STDDEV_GRAPH_FILE;
    } else {
      throw new IllegalArgumentException(
          "No property file mapping for metric - " + metricType);
    }
  }

  /**
   * Append the {@link Job} to a {@link File}.
   *
   * @param job The {@link Job} that we are appending to the message file.
   * @param storeFile The {@link File} to append the messages to.
   * @throws IOException file writes can cause exceptions.
   */
  // @SuppressWarnings("unchecked") // Required because of RunList<Run>
  private static void appendAJobsBuildMessageHistoryToFile(
      final Job job, final File storeFile) throws IOException {
    StringBuilder fileContent = new StringBuilder();
    RunList<Run> builds = job.getBuilds();
    builds.forEach(build -> constructBuildInfoStringForRun(fileContent, build));
    Files.write(fileContent.toString(), storeFile, StandardCharsets.UTF_8);
  }

  /**
   * Append the {@link Run}'s info to a {@link File}.
   *
   * @param build The {@link Run} to generate info from.
   * @param storeFile The {@link File} to append the info into.
   * @throws IOException maybe generated by file writes.
   */
  private static void appendBuildMessageToFile(
      final Run build, final File storeFile) throws IOException {
    StringBuilder fileContent = new StringBuilder();
    constructBuildInfoStringForRun(fileContent, build);
    Files.append(fileContent.toString(), storeFile, StandardCharsets.UTF_8);
  }

  /**
   * Add the {@link Run}'s info to a {@link StringBuilder}.
   *
   * @param fileContent the {@link StringBuilder} to add the {@link Run}'s info
   *     to.
   * @param build The {@link Run} to generate info from.
   */
  private static void constructBuildInfoStringForRun(
      final StringBuilder fileContent, final Run build) {
    fileContent
        .append(build.getNumber())
        .append(",")
        .append(build.getTimestamp().getTimeInMillis())
        .append(",")
        .append(build.getDuration())
        .append(",")
        .append(build.getResult())
        .append("\n");
  }
}
