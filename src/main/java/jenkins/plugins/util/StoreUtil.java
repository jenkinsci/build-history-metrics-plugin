package jenkins.plugins.util;

import com.google.common.io.Files;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;

public class StoreUtil {

    private static final Logger LOGGER = Logger.getLogger(StoreUtil.class.getName());
    public static final String MTTR_PROPERTY_FILE = "mttr.properties";
    public static final String MTTF_PROPERTY_FILE = "mttf.properties";
    public static final String STDDEV_PROPERTY_FILE = "stddev.properties";
    public static final String MTTR_GRAPH_FILE = "mttr.jpg";
    public static final String MTTF_GRAPH_FILE = "mttf.jpg";
    public static final String STDDEV_GRAPH_FILE = "stddev.jpg";
    public static final String UTF_8 = "UTF-8";

    public static void storeBuildMessages(File storeFile, Run build) {
        try {
            if (storeFile.exists()) {
                appendBuildMessageToFile(build, storeFile);
            } else {
                appendAJobsBuildMessageHistoryToFile(build.getParent(), storeFile);
            }
        } catch (IOException e) {
            LOGGER.warning(String.format("store build messages error : %s", e.getMessage()));
        }
    }

    public static void storeBuildMetric(Class metricType, Run run, AggregateBuildMetric... buildMetrics) {
        try {
            StringBuilder fileContent = new StringBuilder();

            for (AggregateBuildMetric buildMetric : buildMetrics) {
                fileContent.append(buildMetric.getName()).append("=")
                        .append(buildMetric.calculateMetric()).append("\n");
            }

            String propertyFilename = getPropertyFilename(metricType);
            File propertiesFile = new File(run.getParent().getRootDir().getAbsolutePath() + File.separator + propertyFilename);
            Files.write(fileContent.toString(), propertiesFile, Charset.forName(UTF_8));

        } catch (IOException e) {
            LOGGER.warning(String.format("store property error:%s", e.getMessage()));
        }
    }

    public static void storeGraph(Class metricType, Run run, JFreeChart chart) {
        try {
            String graphFileName = getGraphFilename(metricType);
            File graphFile = new File(run.getParent().getRootDir().getAbsolutePath() + File.separator + graphFileName);

            ImageIO.write(chart.createBufferedImage(500,500), "jpg", graphFile);
        } catch (IOException e) {
            LOGGER.warning(String.format("store property error:%s", e.getMessage()));
        }
    }

    public static String getPropertyFilename(Class metricType) {
        if(metricType==MTTFMetric.class) {
            return MTTF_PROPERTY_FILE;
        } else if(metricType==MTTRMetric.class) {
            return MTTR_PROPERTY_FILE;
        } else if(metricType==StandardDeviationMetric.class) {
            return STDDEV_PROPERTY_FILE;
        } else {
            throw new IllegalArgumentException("No property file mapping for metric - " + metricType);
        }
    }

    public static String getGraphFilename(Class metricType) {
        if(metricType==MTTFMetric.class) {
            return MTTF_GRAPH_FILE;
        } else if(metricType==MTTRMetric.class) {
            return MTTR_GRAPH_FILE;
        } else if(metricType==StandardDeviationMetric.class) {
            return STDDEV_GRAPH_FILE;
        } else {
            throw new IllegalArgumentException("No property file mapping for metric - " + metricType);
        }
    }

    @SuppressWarnings("unchecked") //Required because of RunList<Run>
    private static void appendAJobsBuildMessageHistoryToFile(Job job, File storeFile) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        RunList<Run> builds = job.getBuilds();
        for (Iterator<Run> i = builds.iterator(); i.hasNext(); ) {
            Run build = i.next();
            constructBuildInfoStringForRun(fileContent, build);
        }
        Files.write(fileContent.toString(), storeFile, Charset.forName(UTF_8));
    }

    private static void appendBuildMessageToFile(Run build, File storeFile) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        constructBuildInfoStringForRun(fileContent, build);
        Files.append(fileContent.toString(), storeFile, Charset.forName(UTF_8));
    }


    private static void constructBuildInfoStringForRun(StringBuilder fileContent, Run build) {
        fileContent.append(build.getNumber()).append(",")
                .append(build.getTimestamp().getTimeInMillis()).append(",")
                .append(build.getDuration()).append(",")
                .append(build.getResult()).append("\n");
    }
}
