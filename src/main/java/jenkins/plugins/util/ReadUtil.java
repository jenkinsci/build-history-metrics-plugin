package jenkins.plugins.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import hudson.Util;
import hudson.model.Job;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import jenkins.plugins.annotations.ExcludeFromCoverageFakeGenerated;
import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.MTTRMetric;

/**
 * Utilities to read metrics from properties.
 *
 * @author mcgin
 */
public final class ReadUtil {
  /** Logger for logging message. */
  private static final Logger LOGGER =
      Logger.getLogger(ReadUtil.class.getName());
  /** Index of Build Number in the Line from the property file. */
  private static final int BUILD_NUMBER_IDX = 0;
  /** Index of Start Time in the Line from the property file. */
  private static final int START_TIME_IDX = 1;
  /** Index of Duration in the Line from the property file. */
  private static final int DURATION_IDX = 2;
  /** Index of Result in the Line from the property file. */
  private static final int RESULT_IDX = 3;

  /**
   * ReadUtil Constructor, private as Utilities classes should not be
   * constructed.
   */
  @ExcludeFromCoverageFakeGenerated
  private ReadUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Get the {@link Properties} for the Metric Type for the {@link Job}.
   *
   * @param metricType The metric type class for the metric we are storing.
   * @param job The {@link Job} we want the metrics for.
   * @return The Properties from the file
   */
  public static Properties getJobProperties(
      final Class metricType, final Job job) {
    Properties properties = new Properties();
    File rootDir = job.getRootDir();
    String filename = StoreUtil.getPropertyFilename(metricType);
    File file = new File(rootDir.getAbsolutePath() + File.separator + filename);

    try (FileInputStream fis = new FileInputStream(file)) {
      properties.load(fis);
    } catch (IOException e) {
      LOGGER.warning(
          String.format("get property file error : %s", e.getMessage()));
      return new Properties();
    }

    return properties;
  }

  /**
   * The Column Results for the {@link Job} property.
   *
   * @param job The {@link Job} we want the metrics for.
   * @param resultKey The key in the properties that we want the result for.
   * @return The Value from this
   */
  public static String getColumnResult(final Job job, final String resultKey) {
    Properties properties = ReadUtil.getJobProperties(MTTRMetric.class, job);
    if (properties.isEmpty()) {
      LOGGER.info("property file can't find");
      return "N/A";
    }

    long result = Long.parseLong(properties.get(resultKey).toString());
    return Util.getTimeSpanString(result);
  }

  /**
   * Returns the {@link List} of {@link BuildMessage}s from a {@link File}.
   *
   * @param storeFile The {@link File} that the {@link BuildMessage}s are stored
   *     in.
   * @return The {@link List} of {@link BuildMessage}s.
   */
  public static List<BuildMessage> getBuildMessageFrom(final File storeFile) {
    List<BuildMessage> buildMessages = Lists.newArrayList();
    try {
      List<String> fileLines =
          Files.readLines(storeFile, StandardCharsets.UTF_8);
      fileLines.stream()
          .map(line -> line.split(","))
          .forEachOrdered(
              build ->
                  buildMessages.add(
                      new BuildMessage(
                          Long.parseLong(build[BUILD_NUMBER_IDX]),
                          Long.parseLong(build[START_TIME_IDX]),
                          Long.parseLong(build[DURATION_IDX]),
                          build[RESULT_IDX])));
      Collections.sort(buildMessages);
    } catch (IOException e) {
      LOGGER.warning(
          String.format(
              "get build message from file error:%s", e.getMessage()));
    }
    return buildMessages;
  }
}
