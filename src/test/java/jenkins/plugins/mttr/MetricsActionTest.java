package jenkins.plugins.mttr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.io.Files;
import hudson.model.AbstractProject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import jenkins.plugins.util.StoreUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class MetricsActionTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
  public void test_MetricsActionDisplayName() throws IOException {
    AbstractProject project = CreateMockProject();
    MetricsAction action = new MetricsAction(project);

    assertNull(action.getDisplayName());
  }

  @Test
  public void test_MetricsActionUrlName() throws IOException {
    AbstractProject project = CreateMockProject();
    MetricsAction action = new MetricsAction(project);

    assertNull(action.getUrlName());
  }

  @Test
  public void test_GetMetricMapShouldReturnAMapWithTheMetricsPopulated()
      throws IOException {
    AbstractProject project = CreateMockProject();
    CreateAMockMTTRPropertiesFileIn(project.getRootDir());
    CreateAMockMTTFPropertiesFileIn(project.getRootDir());
    CreateAMockStdDevPropertiesFileIn(project.getRootDir());

    MetricsAction action = new MetricsAction(project);
    Map<String, String> map = action.getMetricMap();

    assertEquals(
        "MTTR_LAST_7_DAYS is incorrect",
        EXPECTED_MTTR_7_AS_STRING,
        map.get(MetricsAction.MTTR_LAST_7_DAYS));
    assertEquals(
        "MTTR_LAST_30_DAYS is incorrect",
        EXPECTED_MTTR_30_AS_STRING,
        map.get(MetricsAction.MTTR_LAST_30_DAYS));
    assertEquals(
        "MTTR_ALL_BUILDS is incorrect",
        EXPECTED_MTTR_ALL_AS_STRING,
        map.get(MetricsAction.MTTR_ALL_BUILDS));

    assertEquals(
        "MTTF_LAST_7_DAYS is incorrect",
        EXPECTED_MTTF_7_AS_STRING,
        map.get(MetricsAction.MTTF_LAST_7_DAYS));
    assertEquals(
        "MTTF_LAST_30_DAYS is incorrect",
        EXPECTED_MTTF_30_AS_STRING,
        map.get(MetricsAction.MTTF_LAST_30_DAYS));
    assertEquals(
        "MTTF_ALL_BUILDS is incorrect",
        EXPECTED_MTTF_ALL_AS_STRING,
        map.get(MetricsAction.MTTF_ALL_BUILDS));

    assertEquals(
        "STDDEV_LAST_7_DAYS is incorrect",
        EXPECTED_STDDEV_7_AS_STRING,
        map.get(MetricsAction.STDDEV_LAST_7_DAYS));
    assertEquals(
        "MTTF_LAST_30_DAYS is incorrect",
        EXPECTED_STDDEV_30_AS_STRING,
        map.get(MetricsAction.STDDEV_LAST_30_DAYS));
    assertEquals(
        "MTTF_ALL_BUILDS is incorrect",
        EXPECTED_STDDEV_ALL_AS_STRING,
        map.get(MetricsAction.STDDEV_ALL_BUILDS));
  }

  @Test
  public void
      test_GetMetricMapShouldReturnAMapWithTheZeroValueMetricsWhenPropertiesFilesDoNotExist()
          throws IOException {
    AbstractProject project = CreateMockProject();

    MetricsAction action = new MetricsAction(project);
    Map<String, String> map = action.getMetricMap();

    assertEquals(
        "MTTR_LAST_7_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTR_LAST_7_DAYS));
    assertEquals(
        "MTTR_LAST_30_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTR_LAST_30_DAYS));
    assertEquals(
        "MTTR_ALL_BUILDS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTR_ALL_BUILDS));

    assertEquals(
        "MTTF_LAST_7_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTF_LAST_7_DAYS));
    assertEquals(
        "MTTF_LAST_30_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTF_LAST_30_DAYS));
    assertEquals(
        "MTTF_ALL_BUILDS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.MTTF_ALL_BUILDS));

    assertEquals(
        "STDDEV_LAST_7_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.STDDEV_LAST_7_DAYS));
    assertEquals(
        "MTTF_LAST_30_DAYS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.STDDEV_LAST_30_DAYS));
    assertEquals(
        "MTTF_ALL_BUILDS is incorrect",
        ZERO_TIME_AS_STRING,
        map.get(MetricsAction.STDDEV_ALL_BUILDS));
  }

  @Test
  public void test_ResultColumnsShouldReturnExpectedValues()
      throws IOException {
    AbstractProject project = CreateMockProject();
    CreateAMockMTTRPropertiesFileIn(project.getRootDir());

    ResultColumn resultColumn = new BuildMetric30DaysResultColumn();
    assertEquals(
        "MTTR_LAST_30_DAYS is incorrect",
        EXPECTED_MTTR_30_AS_STRING,
        resultColumn.getResult(project));
    resultColumn = new BuildMetric7DaysResultColumn();
    assertEquals(
        "MTTR_LAST_30_DAYS is incorrect",
        EXPECTED_MTTR_7_AS_STRING,
        resultColumn.getResult(project));
    resultColumn = new BuildMetricAllTimeResultColumn();
    assertEquals(
        "MTTR_LAST_30_DAYS is incorrect",
        EXPECTED_MTTR_ALL_AS_STRING,
        resultColumn.getResult(project));
  }

  private AbstractProject CreateMockProject() throws IOException {
    AbstractProject job = Mockito.mock(AbstractProject.class);
    File rootFolder = temporaryFolder.newFolder();
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);
    return job;
  }

  private void CreateAMockMTTRPropertiesFileIn(File rootFolder)
      throws IOException {
    String s =
        MetricsAction.MTTR_LAST_7_DAYS + "\t" + EXPECTED_MTTR_7_MILLIS + "\n";
    s +=
        MetricsAction.MTTR_LAST_30_DAYS + "\t" + EXPECTED_MTTR_30_MILLIS + "\n";
    s += MetricsAction.MTTR_ALL_BUILDS + "\t" + EXPECTED_MTTR_ALL_MILLIS + "\n";

    Files.write(
        s.getBytes(),
        new File(
            rootFolder.getAbsolutePath()
                + File.separator
                + StoreUtil.MTTR_PROPERTY_FILE));
  }

  private void CreateAMockMTTFPropertiesFileIn(File rootFolder)
      throws IOException {
    String s =
        MetricsAction.MTTF_LAST_7_DAYS + "\t" + EXPECTED_MTTF_7_MILLIS + "\n";
    s +=
        MetricsAction.MTTF_LAST_30_DAYS + "\t" + EXPECTED_MTTF_30_MILLIS + "\n";
    s += MetricsAction.MTTF_ALL_BUILDS + "\t" + EXPECTED_MTTF_ALL_MILLIS + "\n";

    Files.write(
        s.getBytes(),
        new File(
            rootFolder.getAbsolutePath()
                + File.separator
                + StoreUtil.MTTF_PROPERTY_FILE));
  }

  private void CreateAMockStdDevPropertiesFileIn(File rootFolder)
      throws IOException {
    String s =
        MetricsAction.STDDEV_LAST_7_DAYS
            + "\t"
            + EXPECTED_STDDEV_7_MILLIS
            + "\n";
    s +=
        MetricsAction.STDDEV_LAST_30_DAYS
            + "\t"
            + EXPECTED_STDDEV_30_MILLIS
            + "\n";
    s +=
        MetricsAction.STDDEV_ALL_BUILDS
            + "\t"
            + EXPECTED_STDDEV_ALL_MILLIS
            + "\n";

    Files.write(
        s.getBytes(),
        new File(
            rootFolder.getAbsolutePath()
                + File.separator
                + StoreUtil.STDDEV_PROPERTY_FILE));
  }
}
