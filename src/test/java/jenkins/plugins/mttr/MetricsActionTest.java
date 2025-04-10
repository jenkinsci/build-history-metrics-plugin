package jenkins.plugins.mttr;

import com.google.common.io.Files;
import hudson.model.AbstractProject;
import jenkins.plugins.util.StoreUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsActionTest {

    @TempDir
    private File temporaryFolder;

    private static final String EXPECTED_MTTR_7_MILLIS = "5200";
    private static final String EXPECTED_MTTR_7_AS_STRING = "5.2 sec";

    private static final String EXPECTED_MTTR_30_MILLIS = "5300";
    private static final String EXPECTED_MTTR_30_AS_STRING = "5.3 sec";

    private static final String EXPECTED_MTTR_ALL_MILLIS = "5400";
    private static final String EXPECTED_MTTR_ALL_AS_STRING = "5.4 sec";

    private static final String EXPECTED_MTTF_7_MILLIS = "1200";
    private static final String EXPECTED_MTTF_7_AS_STRING = "1.2 sec";

    private static final String EXPECTED_MTTF_30_MILLIS = "1300";
    private static final String EXPECTED_MTTF_30_AS_STRING = "1.3 sec";

    private static final String EXPECTED_MTTF_ALL_MILLIS = "1400";
    private static final String EXPECTED_MTTF_ALL_AS_STRING = "1.4 sec";

    private static final String EXPECTED_STDDEV_7_MILLIS = "9200";
    private static final String EXPECTED_STDDEV_7_AS_STRING = "9.2 sec";

    private static final String EXPECTED_STDDEV_30_MILLIS = "9300";
    private static final String EXPECTED_STDDEV_30_AS_STRING = "9.3 sec";

    private static final String EXPECTED_STDDEV_ALL_MILLIS = "9400";
    private static final String EXPECTED_STDDEV_ALL_AS_STRING = "9.4 sec";

    private static final String ZERO_TIME_AS_STRING = "0 ms";


    @Test
    void GetMetricMap_Should_ReturnAMapWithTheMetricsPopulated() throws IOException {
        AbstractProject project = CreateMockProject();
        CreateAMockMTTRPropertiesFileIn(project.getRootDir());
        CreateAMockMTTFPropertiesFileIn(project.getRootDir());
        CreateAMockStdDevPropertiesFileIn(project.getRootDir());

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals(EXPECTED_MTTR_7_AS_STRING, map.get(MetricsAction.MTTR_LAST_7_DAYS), "MTTR_LAST_7_DAYS is incorrect");
        assertEquals(EXPECTED_MTTR_30_AS_STRING, map.get(MetricsAction.MTTR_LAST_30_DAYS), "MTTR_LAST_30_DAYS is incorrect");
        assertEquals(EXPECTED_MTTR_ALL_AS_STRING, map.get(MetricsAction.MTTR_ALL_BUILDS), "MTTR_ALL_BUILDS is incorrect");

        assertEquals(EXPECTED_MTTF_7_AS_STRING, map.get(MetricsAction.MTTF_LAST_7_DAYS), "MTTF_LAST_7_DAYS is incorrect");
        assertEquals(EXPECTED_MTTF_30_AS_STRING, map.get(MetricsAction.MTTF_LAST_30_DAYS), "MTTF_LAST_30_DAYS is incorrect");
        assertEquals(EXPECTED_MTTF_ALL_AS_STRING, map.get(MetricsAction.MTTF_ALL_BUILDS), "MTTF_ALL_BUILDS is incorrect");

        assertEquals(EXPECTED_STDDEV_7_AS_STRING, map.get(MetricsAction.STDDEV_LAST_7_DAYS), "STDDEV_LAST_7_DAYS is incorrect");
        assertEquals(EXPECTED_STDDEV_30_AS_STRING, map.get(MetricsAction.STDDEV_LAST_30_DAYS), "MTTF_LAST_30_DAYS is incorrect");
        assertEquals(EXPECTED_STDDEV_ALL_AS_STRING, map.get(MetricsAction.STDDEV_ALL_BUILDS), "MTTF_ALL_BUILDS is incorrect");
    }

    @Test
    void GetMetricMap_Should_ReturnAMapWithTheZeroValueMetrics_When_PropertiesFilesDoNotExist() throws IOException {
        AbstractProject project = CreateMockProject();

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_LAST_7_DAYS), "MTTR_LAST_7_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_LAST_30_DAYS), "MTTR_LAST_30_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_ALL_BUILDS), "MTTR_ALL_BUILDS is incorrect");

        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_LAST_7_DAYS), "MTTF_LAST_7_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_LAST_30_DAYS), "MTTF_LAST_30_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_ALL_BUILDS), "MTTF_ALL_BUILDS is incorrect");

        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.STDDEV_LAST_7_DAYS), "STDDEV_LAST_7_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.STDDEV_LAST_30_DAYS), "MTTF_LAST_30_DAYS is incorrect");
        assertEquals(ZERO_TIME_AS_STRING, map.get(MetricsAction.STDDEV_ALL_BUILDS), "MTTF_ALL_BUILDS is incorrect");
    }

    @Test
    void ResultColumnsShouldReturnExpectedValues() throws IOException {
        AbstractProject project = CreateMockProject();
        CreateAMockMTTRPropertiesFileIn(project.getRootDir());

        ResultColumn resultColumn = new BuildMetric30DaysResultColumn();
        assertEquals(EXPECTED_MTTR_30_AS_STRING, resultColumn.getResult(project), "MTTR_LAST_30_DAYS is incorrect");
        resultColumn = new BuildMetric7DaysResultColumn();
        assertEquals(EXPECTED_MTTR_7_AS_STRING, resultColumn.getResult(project), "MTTR_LAST_30_DAYS is incorrect");
        resultColumn = new BuildMetricAllTimeResultColumn();
        assertEquals(EXPECTED_MTTR_ALL_AS_STRING, resultColumn.getResult(project), "MTTR_LAST_30_DAYS is incorrect");
    }

    private AbstractProject CreateMockProject() throws IOException {
        AbstractProject job = Mockito.mock(AbstractProject.class);
        File rootFolder = newFolder(temporaryFolder, "junit");
        Mockito.when(job.getRootDir()).thenReturn(rootFolder);
        return job;
    }

    private void CreateAMockMTTRPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTR_LAST_7_DAYS+"\t"+EXPECTED_MTTR_7_MILLIS+"\n";
        s += MetricsAction.MTTR_LAST_30_DAYS+"\t"+EXPECTED_MTTR_30_MILLIS+"\n";
        s += MetricsAction.MTTR_ALL_BUILDS+"\t"+EXPECTED_MTTR_ALL_MILLIS+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + StoreUtil.MTTR_PROPERTY_FILE));
    }

    private void CreateAMockMTTFPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTF_LAST_7_DAYS+"\t"+EXPECTED_MTTF_7_MILLIS+"\n";
        s += MetricsAction.MTTF_LAST_30_DAYS+"\t"+EXPECTED_MTTF_30_MILLIS+"\n";
        s += MetricsAction.MTTF_ALL_BUILDS+"\t"+EXPECTED_MTTF_ALL_MILLIS+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + StoreUtil.MTTF_PROPERTY_FILE));
    }

    private void CreateAMockStdDevPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.STDDEV_LAST_7_DAYS+"\t"+EXPECTED_STDDEV_7_MILLIS+"\n";
        s += MetricsAction.STDDEV_LAST_30_DAYS+"\t"+EXPECTED_STDDEV_30_MILLIS+"\n";
        s += MetricsAction.STDDEV_ALL_BUILDS+"\t"+EXPECTED_STDDEV_ALL_MILLIS+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + StoreUtil.STDDEV_PROPERTY_FILE));
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
