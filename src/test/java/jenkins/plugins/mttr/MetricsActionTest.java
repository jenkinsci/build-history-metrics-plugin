package jenkins.plugins.mttr;

import com.google.common.io.Files;
import hudson.model.AbstractProject;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import static org.junit.Assert.*;

public class MetricsActionTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String EXPECTED_MTTR_7 = "5200";
    private static final String EXPECTED_MTTR_30 = "5300";
    private static final String EXPECTED_MTTR_ALL = "5400";
    private static final String FIVE_SECONDS = "5 seconds";
    private static final String EXPECTED_MTTF_7 = "1200";
    private static final String EXPECTED_MTTF_30 = "1300";
    private static final String EXPECTED_MTTF_ALL = "1400";
    private static final String ONE_SECOND = "1 second";
    private static final String ZERO_SECONDS = "0 seconds";


    @Test
    public void GetMetricMap_Should_ReturnAMapWithTheMetricsPopulated() throws IOException {
        AbstractProject project = CreateMockProject();
        CreateAMockMTTRPropertiesFileIn(project.getRootDir());
        CreateAMockMTTFPropertiesFileIn(project.getRootDir());

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals("MTTR_LAST_7_DAYS is incorrect",
                FIVE_SECONDS, map.get(MetricsAction.MTTR_LAST_7_DAYS));
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                FIVE_SECONDS, map.get(MetricsAction.MTTR_LAST_30_DAYS));
        assertEquals("MTTR_ALL_BUILDS is incorrect",
                FIVE_SECONDS, map.get(MetricsAction.MTTR_ALL_BUILDS));

        assertEquals("MTTF_LAST_7_DAYS is incorrect",
                ONE_SECOND, map.get(MetricsAction.MTTF_LAST_7_DAYS));
        assertEquals("MTTF_LAST_30_DAYS is incorrect",
                ONE_SECOND, map.get(MetricsAction.MTTF_LAST_30_DAYS));
        assertEquals("MTTF_ALL_BUILDS is incorrect",
                ONE_SECOND, map.get(MetricsAction.MTTF_ALL_BUILDS));
    }
    @Test
    public void GetMetricMap_Should_ReturnAMapWithTheZeroValueMetrics_When_PropertiesFilesDoNotExist() throws IOException {
        AbstractProject project = CreateMockProject();

        MetricsAction action = new MetricsAction(project);
        Map<String,String> map = action.getMetricMap();

        assertEquals("MTTR_LAST_7_DAYS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTR_LAST_7_DAYS));
        assertEquals("MTTR_LAST_30_DAYS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTR_LAST_30_DAYS));
        assertEquals("MTTR_ALL_BUILDS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTR_ALL_BUILDS));

        assertEquals("MTTF_LAST_7_DAYS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTF_LAST_7_DAYS));
        assertEquals("MTTF_LAST_30_DAYS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTF_LAST_30_DAYS));
        assertEquals("MTTF_ALL_BUILDS is incorrect",
                ZERO_SECONDS, map.get(MetricsAction.MTTF_ALL_BUILDS));
    }

    private AbstractProject CreateMockProject() throws IOException {
        AbstractProject job = Mockito.mock(AbstractProject.class);
        File rootFolder = temporaryFolder.newFolder();
        Mockito.when(job.getRootDir()).thenReturn(rootFolder);
        return job;
    }

    private void CreateAMockMTTRPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTR_LAST_7_DAYS+"\t"+EXPECTED_MTTR_7+"\n";
        s += MetricsAction.MTTR_LAST_30_DAYS+"\t"+EXPECTED_MTTR_30+"\n";
        s += MetricsAction.MTTR_ALL_BUILDS+"\t"+EXPECTED_MTTR_ALL+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + "mttr.properties"));
    }

    private void CreateAMockMTTFPropertiesFileIn(File rootFolder) throws IOException {
        String s = MetricsAction.MTTF_LAST_7_DAYS+"\t"+EXPECTED_MTTF_7+"\n";
        s += MetricsAction.MTTF_LAST_30_DAYS+"\t"+EXPECTED_MTTF_30+"\n";
        s += MetricsAction.MTTF_ALL_BUILDS+"\t"+EXPECTED_MTTF_ALL+"\n";

        Files.write(s.getBytes(), new File(rootFolder.getAbsolutePath() + File.separator + "mttf.properties"));
    }
}
