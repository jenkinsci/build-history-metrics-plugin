package jenkins.plugins.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import hudson.Util;
import hudson.model.Job;
import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.MTTRMetric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

//TODO Wrap some tests around this class
public class ReadUtil {

    private static final Logger LOGGER = Logger.getLogger(ReadUtil.class.getName());

    public static Properties getJobProperties(Class metricType, Job job) {
        try {
            File rootDir = job.getRootDir();
            String filename = StoreUtil.getPropertyFilename(metricType);
            File file = new File(rootDir.getAbsolutePath() + File.separator + filename);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException e) {
            LOGGER.warning(String.format("get property file error : %s", e.getMessage()));
            return new Properties();
        }
    }

    public static String getColumnResult(Job job, String resultKey) {
        Properties properties = ReadUtil.getJobProperties(MTTRMetric.class, job);
        if (properties == null) {
            LOGGER.info("property file can't find");
            return "N/A";
        }

        long result = Long.valueOf(properties.get(resultKey).toString());
        return Util.getPastTimeString(result);
    }

    public static List<BuildMessage> getBuildMessageFrom(File storeFile) {
        try {
            List<String> fileLines = Files.readLines(storeFile, Charset.forName(StoreUtil.UTF_8));
            List<BuildMessage> buildMessages = Lists.newArrayList();
            for (String line : fileLines) {
                String[] build = line.split(",");
                buildMessages.add(new BuildMessage(Long.valueOf(build[0]),
                        Long.valueOf(build[1]), Long.valueOf(build[2]), build[3]));
            }
            Collections.sort(buildMessages);
            return buildMessages;
        } catch (IOException e) {
            LOGGER.warning(String.format("get build message from file error:%s", e.getMessage()));
            return null;
        }
    }

}
