package jenkins.plugins.mttr;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.util.HttpResponses;
import jenkins.plugins.model.*;
import jenkins.plugins.util.GraphUtil;
import jenkins.plugins.util.ReadUtil;
import jenkins.plugins.util.StoreUtil;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MetricsAction implements Action {

    private static final Logger LOGGER = Logger.getLogger(MetricsAction.class.getName());

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

    private AbstractProject project;

    public MetricsAction(AbstractProject project) {
        this.project = project;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public Map<String, String> getMetricMap() throws IOException {
        Map<String, String> result = new HashMap<String, String>();

        Properties properties = new Properties();
        properties.putAll(ReadUtil.getJobProperties(MTTRMetric.class, project));
        properties.putAll(ReadUtil.getJobProperties(MTTFMetric.class, project));
        properties.putAll(ReadUtil.getJobProperties(StandardDeviationMetric.class, project));

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

        return Util.getPastTimeString(Long.parseLong(duration));
    }

    @Extension
    public static final class ProjectActionFactory extends TransientProjectActionFactory {

        @Override
        public Collection<? extends Action> createFor(AbstractProject target) {
            return Collections.singleton(new MetricsAction(target));
        }
    }

    @Extension
    public static class RunListenerImpl extends RunListener<Run> {

        public RunListenerImpl() {}

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

            JFreeChart chart = GraphUtil.generateGraph();

            BufferedImage img = chart.createBufferedImage(500,500);

            File outputfile = new File(run.getParent().getRootDir().getAbsolutePath() + File.separator + "graph.jpg");


            try {
                ImageIO.write(img, "jpg", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


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
