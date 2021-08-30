package jenkins.plugins.util;

import static org.junit.Assert.*;

import com.google.common.io.Files;
import hudson.model.*;
import hudson.util.RunList;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.JFreeChart;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;

public class StoreUtilTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  public File createTempFileWithLines(int lines) throws IOException {
    File existingFile = temporaryFolder.newFile();
    BufferedWriter out = new BufferedWriter(new FileWriter(existingFile));
    for (int i = 0; i < lines; i++) {
      out.write("blah\n");
    }
    out.close();
    return existingFile;
  }

  @Test
  public void StoreBuildMessages_ShouldAppendTheData_whenTheFileExists() throws Exception {
    // Arrange
    Calendar timestamp = Calendar.getInstance();
    timestamp.setTimeInMillis(5678);
    File existingFile = createTempFileWithLines(2);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getResult()).thenReturn(Result.FAILURE);
    Mockito.when(build.getNumber()).thenReturn(123);
    Mockito.when(build.getTimestamp()).thenReturn(timestamp);
    Mockito.when(build.getDuration()).thenReturn(5000L);

    // Act
    StoreUtil.storeBuildMessages(existingFile, build);

    // Assert
    String storedString = getLineFromFile(existingFile, 3);
    int linesInFile = getLinesInFile(existingFile);

    assertEquals("The file should have 3 lines", 3, linesInFile);
    assertEquals(
        "The data should be in the form BUILDNUMBER,STARTTIMEINMILLIS,DURATIONINMILLIS,RESULT",
        "123,5678,5000,FAILURE",
        storedString);
  }

  @Test
  public void StoreBuildMessages_ShouldIncludeAllJobsFromParent_whenTheFileDoesNotExist()
      throws Exception {
    // Arrange
    File deletedFile = temporaryFolder.newFile();
    deletedFile.delete();

    Calendar timestamp = Calendar.getInstance();
    timestamp.setTimeInMillis(12);
    Calendar timestamp2 = Calendar.getInstance();
    timestamp2.setTimeInMillis(67);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getResult()).thenReturn(Result.FAILURE);
    Mockito.when(build.getNumber()).thenReturn(34);
    Mockito.when(build.getDuration()).thenReturn(56L);
    Mockito.when(build.getTimestamp()).thenReturn(timestamp);

    AbstractBuild build2 = Mockito.mock(AbstractBuild.class);
    Mockito.when(build2.getResult()).thenReturn(Result.SUCCESS);
    Mockito.when(build2.getNumber()).thenReturn(89);
    Mockito.when(build2.getDuration()).thenReturn(10L);
    Mockito.when(build2.getTimestamp()).thenReturn(timestamp2);

    Job job = Mockito.mock(Job.class);
    ArrayList<AbstractBuild> list = new ArrayList<AbstractBuild>();
    list.add(build);
    list.add(build2);

    Mockito.when(job.getBuilds()).thenReturn(RunList.fromRuns(list));
    Mockito.when(build.getParent()).thenReturn(job);

    // Act
    assertFalse("The file should not exist before the test runs", deletedFile.exists());
    StoreUtil.storeBuildMessages(deletedFile, build);

    // Assert
    String firstBuild = getLineFromFile(deletedFile, 1);
    String secondBuild = getLineFromFile(deletedFile, 2);

    int linesInFile = getLinesInFile(deletedFile);

    assertEquals("The file should have 2 lines", 2, linesInFile);
    assertEquals("The data for the first build is not correct", "34,12,56,FAILURE", firstBuild);
    assertEquals("The data for the second build is not correct", "89,67,10,SUCCESS", secondBuild);
  }

  @Test
  public void testStoreMTTRInfo() throws Exception {
    // Arrange
    File rootFolder = temporaryFolder.newFolder();

    Job job = Mockito.mock(Job.class);
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getParent()).thenReturn(job);

    AggregateBuildMetric info = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info.calculateMetric()).thenReturn(76543210L);
    Mockito.when(info.getName()).thenReturn("last7");

    AggregateBuildMetric info2 = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info2.calculateMetric()).thenReturn(3210L);
    Mockito.when(info2.getName()).thenReturn("last30");

    // Act
    StoreUtil.storeBuildMetric(MTTRMetric.class, build, info, info2);

    // Assert

    File propertiesFile =
        new File(rootFolder.getAbsolutePath() + File.separator + "mttr.properties");
    assertTrue("The mttr.properties file is missing", propertiesFile.exists());

    List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
    assertEquals("Should have only 2 lines", 2, lines.size());
    assertEquals("The first  MTTR metric is wrong", "last7=76543210", lines.get(0));
    assertEquals("The second  MTTR metric is wrong", "last30=3210", lines.get(1));
  }

  @Test
  public void testStorStdDevInfo() throws Exception {
    // Arrange
    File rootFolder = temporaryFolder.newFolder();

    Job job = Mockito.mock(Job.class);
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getParent()).thenReturn(job);

    AggregateBuildMetric info = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info.calculateMetric()).thenReturn(76543210L);
    Mockito.when(info.getName()).thenReturn("last7");

    AggregateBuildMetric info2 = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info2.calculateMetric()).thenReturn(3210L);
    Mockito.when(info2.getName()).thenReturn("last30");

    // Act
    StoreUtil.storeBuildMetric(StandardDeviationMetric.class, build, info, info2);

    // Assert

    File propertiesFile =
        new File(rootFolder.getAbsolutePath() + File.separator + "stddev.properties");
    assertTrue("The stddev.properties file is missing", propertiesFile.exists());

    List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
    assertEquals("Should have only 2 lines", 2, lines.size());
    assertEquals("The first  stddev metric is wrong", "last7=76543210", lines.get(0));
    assertEquals("The second  stddev metric is wrong", "last30=3210", lines.get(1));
  }

  @Test
  public void testStoreMTTFInfo() throws Exception {
    // Arrange
    File rootFolder = temporaryFolder.newFolder();

    Job job = Mockito.mock(Job.class);
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getParent()).thenReturn(job);

    AggregateBuildMetric info = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info.calculateMetric()).thenReturn(76543210L);
    Mockito.when(info.getName()).thenReturn("last7");

    AggregateBuildMetric info2 = Mockito.mock(AggregateBuildMetric.class);
    Mockito.when(info2.calculateMetric()).thenReturn(3210L);
    Mockito.when(info2.getName()).thenReturn("last30");

    // Act
    StoreUtil.storeBuildMetric(MTTFMetric.class, build, info, info2);

    // Assert
    File propertiesFile =
        new File(rootFolder.getAbsolutePath() + File.separator + "mttf.properties");
    assertTrue(
        "The mttf.properties file is missing: " + propertiesFile.toString(),
        propertiesFile.exists());

    List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
    assertEquals("Should have only 2 lines", 2, lines.size());
    assertEquals("The first MTTF metric is wrong", "last7=76543210", lines.get(0));
    assertEquals("The second  MTTF metric is wrong", "last30=3210", lines.get(1));
  }

  @Test
  public void testStoreGraph() throws Exception {
    // Arrange
    File rootFolder = temporaryFolder.newFolder();

    Job job = Mockito.mock(Job.class);
    Mockito.when(job.getRootDir()).thenReturn(rootFolder);

    AbstractBuild build = Mockito.mock(AbstractBuild.class);
    Mockito.when(build.getParent()).thenReturn(job);

    JFreeChart chart = Mockito.mock(JFreeChart.class);
    Mockito.when(chart.createBufferedImage(500, 500))
        .thenReturn(new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB));
    // Act
    StoreUtil.storeGraph(MTTFMetric.class, build, chart);

    // Assert
    File graphFile = new File(rootFolder.getAbsolutePath() + File.separator + "mttf.jpg");
    assertTrue("The mttf.jpg file is missing: " + graphFile.toString(), graphFile.exists());
  }

  private int getLinesInFile(File file) throws IOException {
    LineNumberReader lnr = new LineNumberReader(new FileReader(file));
    lnr.skip(Long.MAX_VALUE);
    return (lnr.getLineNumber());
  }

  private String getLineFromFile(File file, int i) throws IOException {
    LineNumberReader lnr = new LineNumberReader(new FileReader(file));
    String s = null;
    do {
      s = lnr.readLine();
    } while (--i > 0);
    return (s);
  }
}
