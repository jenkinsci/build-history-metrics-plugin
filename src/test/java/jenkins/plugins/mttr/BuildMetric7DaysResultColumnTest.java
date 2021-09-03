/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jenkins.plugins.mttr;

import static org.junit.Assert.*;

import com.google.common.io.Files;
import hudson.model.AbstractProject;
import java.io.File;
import java.io.IOException;
import jenkins.plugins.util.StoreUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/** @author nick */
public class BuildMetric7DaysResultColumnTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static final String EXPECTED_MTTR_7_MILLIS = "5200";
  private static final String EXPECTED_MTTR_7_AS_STRING = "5.2 sec";

  private static final String EXPECTED_MTTR_30_MILLIS = "5300";

  private static final String EXPECTED_MTTR_ALL_MILLIS = "5400";

  /**
   * Test of getResult method, of class BuildMetricAllTimeResultColumn.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_GetResult() throws Exception {
    AbstractProject project = CreateMockProject();
    CreateAMockMTTRPropertiesFileIn(project.getRootDir());

    ResultColumn resultColumn = new BuildMetric7DaysResultColumn();
    assertEquals(
        "MTTR_All_DAYS is incorrect",
        EXPECTED_MTTR_7_AS_STRING,
        resultColumn.getResult(project));
  }

  /** Test of getGraph method, of class BuildMetricAllTimeResultColumn. */
  @Test
  public void test_GetGraph() throws Exception {
    AbstractProject project = CreateMockProject();
    BuildMetric7DaysResultColumn instance = new BuildMetric7DaysResultColumn();

    assertNull(instance.getGraph(project));
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
}
