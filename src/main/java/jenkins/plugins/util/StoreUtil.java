package jenkins.plugins.util;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.plugins.model.JobFailedTimeInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;

public class StoreUtil {

    private static final Logger LOGGER = Logger.getLogger(StoreUtil.class.getName());
    public static final String BANGKOU_PROPERTIES = "mttr.properties";
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

    public static void storeJobFailedInfo(Run run, JobFailedTimeInfo... jobFailedTimeInfos) {

        StringBuilder filePath = new StringBuilder();
        try {
            filePath.append(run.getParent().getRootDir().getAbsolutePath())
                    .append(File.separator).append(BANGKOU_PROPERTIES);

            if (Strings.isNullOrEmpty(filePath.toString())) {
                LOGGER.warning("file path is empty");
                return;
            }

            File file = new File(filePath.toString());

            StringBuilder fileContent = new StringBuilder();
            for (JobFailedTimeInfo jobFailedTimeInfo : jobFailedTimeInfos) {
                fileContent.append(jobFailedTimeInfo.getName()).append("=")
                        .append(jobFailedTimeInfo.calcAvgFailedTime()).append("\n");
            }
            Files.write(fileContent.toString(), file, Charset.forName(UTF_8));
        } catch (IOException e) {
            LOGGER.warning(String.format("store property error:%s", e.getMessage()));
        }
    }

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
                .append(build.getResult()).append("\n");
    }
}
