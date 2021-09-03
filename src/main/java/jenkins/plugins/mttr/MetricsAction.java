package jenkins.plugins.mttr;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import jenkins.model.TransientActionFactory;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import jenkins.plugins.model.StandardDeviationMetric;
import jenkins.plugins.util.GraphUtil;
import jenkins.plugins.util.ReadUtil;
import jenkins.plugins.util.StoreUtil;
import org.jfree.chart.JFreeChart;

/**
 * The main Build History Metrics class.
 *
 * @author mcgin
 */
public class MetricsAction implements Action {
  /** TOKEN to cut at Seven days. */
  public static final int LAST_7_DAYS = 7;
  /** TOKEN to cut at thirty days. */
  public static final int LAST_30_DAYS = 30;

  /** TOKEN Key for Last 7 days Mean Time to Repair. */
  public static final String MTTR_LAST_7_DAYS = "mttrLast7days";
  /** TOKEN Key for Last 30 days Mean Time to Repair. */
  public static final String MTTR_LAST_30_DAYS = "mttrLast30days";
  /** TOKEN Key for All builds Mean Time to Repair. */
  public static final String MTTR_ALL_BUILDS = "mttrAllBuilds";

  /** TOKEN Key for Last 7 days Mean Time to Fail. */
  public static final String MTTF_LAST_7_DAYS = "mttfLast7days";
  /** TOKEN Key for Last 30 days Mean Time to Fail. */
  public static final String MTTF_LAST_30_DAYS = "mttfLast30days";
  /** TOKEN Key for All builds Mean Time to Fail. */
  public static final String MTTF_ALL_BUILDS = "mttfAllBuilds";

  /** TOKEN Key for Last 7 days Standard Deviation. */
  public static final String STDDEV_LAST_7_DAYS = "stddevLast7days";
  /** TOKEN Key for Last 30 days Standard Deviation. */
  public static final String STDDEV_LAST_30_DAYS = "stddevLast30days";
  /** TOKEN Key for All builds Standard Deviation. */
  public static final String STDDEV_ALL_BUILDS = "stddevAllBuilds";

  /** TOKEN The name of the metrics data file. */
  public static final String ALL_BUILDS_FILE_NAME = "all_builds.mr";

  /** The {@link Job} that the Metrics are being calculated and stored for. */
  private final Job job;

  /**
   * MetricsAction constructor for the job.
   *
   * @param pJob the job to build the Metrics for.
   */
  public MetricsAction(final Job pJob) {
    this.job = pJob;
  }

  /** {@inheritDoc} */
  @Override
  public final String getIconFileName() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public final String getDisplayName() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public final String getUrlName() {
    return null;
  }

  /**
   * Generates the Metrics to display on this page.
   *
   * @return Map of Metrics
   * @throws IOException Reads metrics from a file so may through an exception
   */
  public final Map<String, String> getMetricMap() throws IOException {
    Map<String, String> result = new HashMap<>();

    Properties properties = new Properties();
    properties.putAll(ReadUtil.getJobProperties(MTTRMetric.class, job));
    properties.putAll(ReadUtil.getJobProperties(MTTFMetric.class, job));
    properties.putAll(
        ReadUtil.getJobProperties(StandardDeviationMetric.class, job));

    result.put(
        MetricsAction.MTTR_LAST_7_DAYS,
        getPropertyOrDefault(properties, MetricsAction.MTTR_LAST_7_DAYS, "0"));
    result.put(
        MetricsAction.MTTR_LAST_30_DAYS,
        getPropertyOrDefault(properties, MetricsAction.MTTR_LAST_30_DAYS, "0"));
    result.put(
        MetricsAction.MTTR_ALL_BUILDS,
        getPropertyOrDefault(properties, MetricsAction.MTTR_ALL_BUILDS, "0"));

    result.put(
        MetricsAction.MTTF_LAST_7_DAYS,
        getPropertyOrDefault(properties, MetricsAction.MTTF_LAST_7_DAYS, "0"));
    result.put(
        MetricsAction.MTTF_LAST_30_DAYS,
        getPropertyOrDefault(properties, MetricsAction.MTTF_LAST_30_DAYS, "0"));
    result.put(
        MetricsAction.MTTF_ALL_BUILDS,
        getPropertyOrDefault(properties, MetricsAction.MTTF_ALL_BUILDS, "0"));

    result.put(
        MetricsAction.STDDEV_LAST_7_DAYS,
        getPropertyOrDefault(
            properties, MetricsAction.STDDEV_LAST_7_DAYS, "0"));
    result.put(
        MetricsAction.STDDEV_LAST_30_DAYS,
        getPropertyOrDefault(
            properties, MetricsAction.STDDEV_LAST_30_DAYS, "0"));
    result.put(
        MetricsAction.STDDEV_ALL_BUILDS,
        getPropertyOrDefault(properties, MetricsAction.STDDEV_ALL_BUILDS, "0"));

    return result;
  }

  /**
   * Gets a property value from the property bag passed in.
   *
   * @param properties The properties bag.
   * @param key The key of the property to be returned.
   * @param defaultValue Value to return if property is not in the bag.
   * @return The value found or the default.
   */
  private String getPropertyOrDefault(
      final Properties properties,
      final String key,
      final String defaultValue) {
    String duration = defaultValue;
    if (properties.containsKey(key)) {
      duration = properties.getProperty(key);
    }

    return Util.getTimeSpanString(Long.parseLong(duration));
  }

  /** The Action Factory that links contains the Create Action for the class. */
  @Extension
  public static final class ActionFactory extends TransientActionFactory<Job> {
    /** {@inheritDoc} */
    @Override
    public Class<Job> type() {
      return Job.class;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends Action> createFor(final Job target) {
      return Collections.singleton(new MetricsAction(target));
    }
  }

  /** The RunListener is called to process the results of a run. */
  @Extension
  public static class RunListenerImpl extends RunListener<Run> {

    /** {@inheritDoc} */
    @Override
    public final void onCompleted(final Run run, final TaskListener listener) {
      File storeFile =
          new File(
              run.getParent().getRootDir().getAbsolutePath()
                  + File.separator
                  + ALL_BUILDS_FILE_NAME);

      StoreUtil.storeBuildMessages(storeFile, run);

      List<BuildMessage> buildMessages =
          ReadUtil.getBuildMessageFrom(storeFile);

      AggregateBuildMetric mttrLast7DayInfo =
          new MTTRMetric(
              MTTR_LAST_7_DAYS, cutListByAgoDays(buildMessages, LAST_7_DAYS));
      AggregateBuildMetric mttrLast30DayInfo =
          new MTTRMetric(
              MTTR_LAST_30_DAYS, cutListByAgoDays(buildMessages, LAST_30_DAYS));
      AggregateBuildMetric mttrAllFailedInfo =
          new MTTRMetric(MTTR_ALL_BUILDS, buildMessages);

      StoreUtil.storeBuildMetric(
          MTTRMetric.class,
          run,
          mttrLast7DayInfo,
          mttrLast30DayInfo,
          mttrAllFailedInfo);

      AggregateBuildMetric mttfLast7DayInfo =
          new MTTFMetric(
              MTTF_LAST_7_DAYS, cutListByAgoDays(buildMessages, LAST_7_DAYS));
      AggregateBuildMetric mttfLast30DayInfo =
          new MTTFMetric(
              MTTF_LAST_30_DAYS, cutListByAgoDays(buildMessages, LAST_30_DAYS));
      AggregateBuildMetric mttfAllBuilds =
          new MTTFMetric(MTTF_ALL_BUILDS, buildMessages);

      StoreUtil.storeBuildMetric(
          MTTFMetric.class,
          run,
          mttfLast7DayInfo,
          mttfLast30DayInfo,
          mttfAllBuilds);

      AggregateBuildMetric stdDevLast7DayInfo =
          new StandardDeviationMetric(
              STDDEV_LAST_7_DAYS, cutListByAgoDays(buildMessages, LAST_7_DAYS));
      AggregateBuildMetric stdDevLast30DayInfo =
          new StandardDeviationMetric(
              STDDEV_LAST_30_DAYS,
              cutListByAgoDays(buildMessages, LAST_30_DAYS));
      AggregateBuildMetric stdDevAllFailedInfo =
          new StandardDeviationMetric(STDDEV_ALL_BUILDS, buildMessages);

      StoreUtil.storeBuildMetric(
          StandardDeviationMetric.class,
          run,
          stdDevLast7DayInfo,
          stdDevLast30DayInfo,
          stdDevAllFailedInfo);

      JFreeChart stddevChart =
          GraphUtil.generateStdDevGraph(
              "Standard Deviation of Build Time", buildMessages);

      StoreUtil.storeGraph(StandardDeviationMetric.class, run, stddevChart);
    }

    /**
     * Gets the list of builds for the last <code>daysAgo</code> days.
     *
     * @param builds The list of all builds.
     * @param daysAgo The point to cut the list.
     * @return The List for the last <code>daysAgo</code> days builds.
     */
    private List<BuildMessage> cutListByAgoDays(
        final List<BuildMessage> builds, final int daysAgo) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DATE, -daysAgo);

      List<BuildMessage> subList = Lists.newArrayList();
      builds.stream()
          .filter(build -> (build.getStartTime() > calendar.getTimeInMillis()))
          .forEachOrdered(build -> subList.add(build));
      return subList;
    }
  }
}
