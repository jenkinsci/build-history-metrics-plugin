package jenkins.plugins.mttr;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.plugins.model.*;
import jenkins.plugins.util.GraphUtil;
import jenkins.plugins.util.ReadUtil;
import jenkins.plugins.util.StoreUtil;
import org.jfree.chart.JFreeChart;
import java.io.File;
import java.io.IOException;
import java.util.*;
import jenkins.model.TransientActionFactory;

public class MetricsAction implements Action {
    public static final String MTTR_LAST_7_DAYS = "mttrLast7days";
    public static final String MTTR_LAST_30_DAYS = "mttrLast30days";
    public static final String MTTR_ALL_BUILDS = "mttrAllBuilds";

    public static final String MTTF_LAST_7_DAYS = "mttfLast7days";
    public static final String MTTF_LAST_30_DAYS = "mttfLast30days";
    public static final String MTTF_ALL_BUILDS = "mttfAllBuilds";

    public static final String STDDEV_LAST_7_DAYS = "stddevLast7days";
    public static final String STDDEV_LAST_30_DAYS = "stddevLast30days";
    public static final String STDDEV_ALL_BUILDS = "stddevAllBuilds";
    
    public static final String ALL_BUILDS_FILE_NAME = "all_builds.mr";

    private final Job job;

    public MetricsAction(Job job) {
        this.job = job;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

		@Override
    public String getUrlName() {
        return null;
    }

    public Map<String, String> getMetricMap() throws IOException {
        Map<String, String> result = new HashMap<>();

        Properties properties = new Properties();
        properties.putAll(ReadUtil.getJobProperties(MTTRMetric.class, job));
        properties.putAll(ReadUtil.getJobProperties(MTTFMetric.class, job));
        properties.putAll(ReadUtil.getJobProperties(StandardDeviationMetric.class, job));

        result.put(MetricsAction.MTTR_LAST_7_DAYS, getPropertyOrDefault(properties, MetricsAction.MTTR_LAST_7_DAYS, "0"));
        result.put(MetricsAction.MTTR_LAST_30_DAYS, getPropertyOrDefault(properties, MetricsAction.MTTR_LAST_30_DAYS, "0"));
        result.put(MetricsAction.MTTR_ALL_BUILDS, getPropertyOrDefault(properties, MetricsAction.MTTR_ALL_BUILDS, "0"));

        result.put(MetricsAction.MTTF_LAST_7_DAYS, getPropertyOrDefault(properties, MetricsAction.MTTF_LAST_7_DAYS, "0"));
        result.put(MetricsAction.MTTF_LAST_30_DAYS, getPropertyOrDefault(properties, MetricsAction.MTTF_LAST_30_DAYS, "0"));
        result.put(MetricsAction.MTTF_ALL_BUILDS, getPropertyOrDefault(properties, MetricsAction.MTTF_ALL_BUILDS, "0"));

        result.put(MetricsAction.STDDEV_LAST_7_DAYS, getPropertyOrDefault(properties, MetricsAction.STDDEV_LAST_7_DAYS, "0"));
        result.put(MetricsAction.STDDEV_LAST_30_DAYS, getPropertyOrDefault(properties, MetricsAction.STDDEV_LAST_30_DAYS, "0"));
        result.put(MetricsAction.STDDEV_ALL_BUILDS, getPropertyOrDefault(properties, MetricsAction.STDDEV_ALL_BUILDS, "0"));

        return result;
    }

    private String getPropertyOrDefault(Properties properties, String key, String defaultValue) {
        String duration = properties.containsKey(key)?
                properties.getProperty(key):defaultValue;

        return Util.getTimeSpanString(Long.parseLong(duration));
    }

		@Extension
		public static final class ActionFactory extends TransientActionFactory<Job> {
			@Override
			public Class<Job> type() {
				return Job.class;
			}

			@Override
			public Collection<? extends Action> createFor(Job target) {
				return Collections.singleton(new MetricsAction(target));
			}
		}

    @Extension
    public static class RunListenerImpl extends RunListener<Run> {

				@Override
				public void onCompleted(Run run, TaskListener listener) {
            File storeFile = new File(run.getParent().getRootDir().getAbsolutePath()
                    + File.separator + ALL_BUILDS_FILE_NAME);

            StoreUtil.storeBuildMessages(storeFile, run);

            List<BuildMessage> buildMessages = ReadUtil.getBuildMessageFrom(storeFile);

            AggregateBuildMetric mttrLast7DayInfo = new MTTRMetric(MTTR_LAST_7_DAYS, cutListByAgoDays(buildMessages, -7));
            AggregateBuildMetric mttrLast30DayInfo = new MTTRMetric(MTTR_LAST_30_DAYS, cutListByAgoDays(buildMessages, -30));
            AggregateBuildMetric mttrAllFailedInfo = new MTTRMetric(MTTR_ALL_BUILDS, buildMessages);

            StoreUtil.storeBuildMetric(MTTRMetric.class, run,
                    mttrLast7DayInfo, mttrLast30DayInfo, mttrAllFailedInfo);

            AggregateBuildMetric mttfLast7DayInfo = new MTTFMetric(MTTF_LAST_7_DAYS, cutListByAgoDays(buildMessages, -7));
            AggregateBuildMetric mttfLast30DayInfo = new MTTFMetric(MTTF_LAST_30_DAYS, cutListByAgoDays(buildMessages, -30));
            AggregateBuildMetric mttfAllBuilds = new MTTFMetric(MTTF_ALL_BUILDS, buildMessages);

            StoreUtil.storeBuildMetric(MTTFMetric.class, run,
                    mttfLast7DayInfo, mttfLast30DayInfo, mttfAllBuilds);

            AggregateBuildMetric stdDevLast7DayInfo = new StandardDeviationMetric(STDDEV_LAST_7_DAYS, cutListByAgoDays(buildMessages, -7));
            AggregateBuildMetric stdDevLast30DayInfo = new StandardDeviationMetric(STDDEV_LAST_30_DAYS, cutListByAgoDays(buildMessages, -30));
            AggregateBuildMetric stdDevAllFailedInfo = new StandardDeviationMetric(STDDEV_ALL_BUILDS, buildMessages);

            StoreUtil.storeBuildMetric(StandardDeviationMetric.class, run,
                    stdDevLast7DayInfo, stdDevLast30DayInfo, stdDevAllFailedInfo);

            JFreeChart stddevChart = GraphUtil.generateStdDevGraph("Standard Deviation of Build Time", buildMessages);

            StoreUtil.storeGraph(StandardDeviationMetric.class, run, stddevChart);
        }

        private List<BuildMessage> cutListByAgoDays(List<BuildMessage> builds, int daysAgo) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, daysAgo);

            List<BuildMessage> subList = Lists.newArrayList();
            for (BuildMessage build : builds) {
                if (build.getStartTime() > calendar.getTimeInMillis()) {
                    subList.add(build);
                }
            }
            return subList;
        }

    }
}
