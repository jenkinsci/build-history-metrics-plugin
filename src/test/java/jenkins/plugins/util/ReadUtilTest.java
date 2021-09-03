/** @author nick */
package jenkins.plugins.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.model.AbstractProject;
import java.io.File;
import java.io.IOException;
import jenkins.plugins.mttr.MetricsAction;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class ReadUtilTest {

  public ReadUtilTest() {}

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AbstractProject CreateMockProject() throws IOException {
    AbstractProject job = Mockito.mock(AbstractProject.class);
    File rootFolder = temporaryFolder.newFolder();
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);
    return job;
  }

  @Test
  public void test_GetColumnResultEmptyFiles() throws IOException {
    AbstractProject project = CreateMockProject();

    assertEquals("N/A", ReadUtil.getColumnResult(project, MetricsAction.MTTR_LAST_30_DAYS));
  }

  @Test
  public void test_GetBuildMessages_EmptyFiles() throws IOException {
    File deletedFile = temporaryFolder.newFile();
    deletedFile.delete();

    assertTrue(ReadUtil.getBuildMessageFrom(deletedFile).isEmpty());
  }
}
