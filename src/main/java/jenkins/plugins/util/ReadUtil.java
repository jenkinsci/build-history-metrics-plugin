package jenkins.plugins.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import hudson.Util;
import hudson.model.Job;
import jenkins.plugins.model.BuildMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class ReadUtil {

    private static final Logger LOGGER = Logger.getLogger(ReadUtil.class.getName());

    public static Properties getJobProperties(Job job) {
        try {
            File rootDir = job.getRootDir();
            File file = new File(rootDir.getAbsolutePath() + File.separator + StoreUtil.BANGKOU_PROPERTIES);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException e) {
            LOGGER.warning(String.format("get property file error : %s", e.getMessage()));
            return null;
        }
    }

    public static String getColumnResult(Job job, String resultKey) {
        Properties properties = ReadUtil.getJobProperties(job);
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
                buildMessages.add(new BuildMessage(Long.valueOf(build[1]), build[2]));
            }
            return buildMessages;
        } catch (IOException e) {
            LOGGER.warning(String.format("get build message from file error:%s", e.getMessage()));
            return null;
        }
    }

}
