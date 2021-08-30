package jenkins.plugins.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import hudson.Util;
import hudson.model.Job;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import jenkins.plugins.annotations.ExcludeFromCoverage_FakeGenerated;
import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.MTTRMetric;

// TODO Wrap some tests around this class
public class ReadUtil {

  private static final Logger LOGGER = Logger.getLogger(ReadUtil.class.getName());

  @ExcludeFromCoverage_FakeGenerated
  private ReadUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static Properties getJobProperties(Class metricType, Job job) {
    try {
      File rootDir = job.getRootDir();
      String filename = StoreUtil.getPropertyFilename(metricType);
      File file = new File(rootDir.getAbsolutePath() + File.separator + filename);
      Properties properties = new Properties();
      try (FileInputStream fis = new FileInputStream(file)) {
        properties.load(fis);
      }

      return properties;
    } catch (IOException e) {
      LOGGER.warning(String.format("get property file error : %s", e.getMessage()));
      return new Properties();
    }
  }

  public static String getColumnResult(Job job, String resultKey) {
    Properties properties = ReadUtil.getJobProperties(MTTRMetric.class, job);
    long result = Long.parseLong(properties.get(resultKey).toString());
    return Util.getPastTimeString(result);
  }

  public static List<BuildMessage> getBuildMessageFrom(File storeFile) {
    List<BuildMessage> buildMessages = Lists.newArrayList();
    try {
      List<String> fileLines = Files.readLines(storeFile, Charset.forName(StoreUtil.UTF_8));
      for (String line : fileLines) {
        String[] build = line.split(",");
        buildMessages.add(
            new BuildMessage(
                Long.parseLong(build[0]),
                Long.parseLong(build[1]),
                Long.parseLong(build[2]),
                build[3]));
      }
      Collections.sort(buildMessages);
    } catch (IOException e) {
      LOGGER.warning(String.format("get build message from file error:%s", e.getMessage()));
    }
		return buildMessages;
    
  }
}
