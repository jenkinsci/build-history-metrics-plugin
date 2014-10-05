package jenkins.plugins.mttr;

import com.google.common.io.Files;
import hudson.model.AbstractProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MetricsActionTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

    private static final String ZERO_TIME_AS_STRING = "0 ms";


    @Test
    public void GetMetricMap_Should_ReturnAMapWithTheMetricsPopulated() throws IOException {
        AbstractProject project = CreateMockProject();
        CreateAMockMTTRPropertiesFileIn(project.getRootDir());
        CreateAMockMTTFPropertiesFileIn(project.getRootDir());

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals("MTTR_LAST_7_DAYS is incorrect",
                EXPECTED_MTTR_7_AS_STRING, map.get(MetricsAction.MTTR_LAST_7_DAYS));
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                EXPECTED_MTTR_30_AS_STRING, map.get(MetricsAction.MTTR_LAST_30_DAYS));
        assertEquals("MTTR_ALL_BUILDS is incorrect",
                EXPECTED_MTTR_ALL_AS_STRING, map.get(MetricsAction.MTTR_ALL_BUILDS));

        assertEquals("MTTF_LAST_7_DAYS is incorrect",
                EXPECTED_MTTF_7_AS_STRING, map.get(MetricsAction.MTTF_LAST_7_DAYS));
        assertEquals("MTTF_LAST_30_DAYS is incorrect",
                EXPECTED_MTTF_30_AS_STRING, map.get(MetricsAction.MTTF_LAST_30_DAYS));
        assertEquals("MTTF_ALL_BUILDS is incorrect",
                EXPECTED_MTTF_ALL_AS_STRING, map.get(MetricsAction.MTTF_ALL_BUILDS));
    }
    @Test
    public void GetMetricMap_Should_ReturnAMapWithTheZeroValueMetrics_When_PropertiesFilesDoNotExist() throws IOException {
        AbstractProject project = CreateMockProject();

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals("MTTR_LAST_7_DAYS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_LAST_7_DAYS));
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_LAST_30_DAYS));
        assertEquals("MTTR_ALL_BUILDS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTR_ALL_BUILDS));

        assertEquals("MTTF_LAST_7_DAYS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_LAST_7_DAYS));
        assertEquals("MTTF_LAST_30_DAYS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_LAST_30_DAYS));
        assertEquals("MTTF_ALL_BUILDS is incorrect",
                ZERO_TIME_AS_STRING, map.get(MetricsAction.MTTF_ALL_BUILDS));
    }

    @Test
    public void ResultColumnsShouldReturnExpectedValues() throws IOException {
        AbstractProject project = CreateMockProject();
        CreateAMockMTTRPropertiesFileIn(project.getRootDir());
        CreateAMockMTTFPropertiesFileIn(project.getRootDir());

        ResultColumn resultColumn = new Last30DaysResultColumn();
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                EXPECTED_MTTR_30_AS_STRING, resultColumn.getResult(project));
        resultColumn = new Last7DaysResultColumn();
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                EXPECTED_MTTR_7_AS_STRING, resultColumn.getResult(project));
        resultColumn = new AllBuildsResultColumn();
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                EXPECTED_MTTR_ALL_AS_STRING, resultColumn.getResult(project));
    }

    private AbstractProject CreateMockProject() throws IOException {
        AbstractProject job = Mockito.mock(AbstractProject.class);
        File rootFolder = temporaryFolder.newFolder();
        Mockito.when(job.getRootDir()).thenReturn(rootFolder);
        return job;
    }

    private void CreateAMockMTTRPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTR_LAST_7_DAYS+"\t"+EXPECTED_MTTR_7_MILLIS+"\n";
        s += MetricsAction.MTTR_LAST_30_DAYS+"\t"+EXPECTED_MTTR_30_MILLIS+"\n";
        s += MetricsAction.MTTR_ALL_BUILDS+"\t"+EXPECTED_MTTR_ALL_MILLIS+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + "mttr.properties"));
    }

    private void CreateAMockMTTFPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTF_LAST_7_DAYS+"\t"+EXPECTED_MTTF_7_MILLIS+"\n";
        s += MetricsAction.MTTF_LAST_30_DAYS+"\t"+EXPECTED_MTTF_30_MILLIS+"\n";
        s += MetricsAction.MTTF_ALL_BUILDS+"\t"+EXPECTED_MTTF_ALL_MILLIS+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + "mttf.properties"));
    }
}
