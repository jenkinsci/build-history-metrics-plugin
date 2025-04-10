package jenkins.plugins.util;


import com.google.common.io.Files;
import hudson.model.*;
import hudson.util.RunList;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.JFreeChart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoreUtilTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    private File temporaryFolder;


    private File createTempFileWithLines(int lines) throws IOException {
        File existingFile = File.createTempFile("junit", null, temporaryFolder);
        BufferedWriter out = new BufferedWriter(new FileWriter(existingFile));
        for(int i=0; i<lines; i++) {
            out.write("blah\n");
        }
        out.close();
        return existingFile;
    }

    @Test
    void StoreBuildMessages_ShouldAppendTheData_whenTheFileExists()
            throws Exception {
        //Arrange
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(5678);
        File existingFile = createTempFileWithLines(2);

        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.FAILURE);
        Mockito.when(build.getNumber()).thenReturn(123);
        Mockito.when(build.getTimestamp()).thenReturn(timestamp);
        Mockito.when(build.getDuration()).thenReturn(5000L);

        //Act
        StoreUtil.storeBuildMessages(existingFile, build);

        //Assert
        String storedString = getLineFromFile(existingFile, 3);
        int linesInFile = getLinesInFile(existingFile);

        assertEquals(3, linesInFile, "The file should have 3 lines");
        assertEquals("123,5678,5000,FAILURE", storedString, "The data should be in the form BUILDNUMBER,STARTTIMEINMILLIS,DURATIONINMILLIS,RESULT");
    }

    @Test
    void StoreBuildMessages_ShouldIncludeAllJobsFromParent_whenTheFileDoesNotExist()
            throws Exception {
        //Arrange
        File deletedFile = File.createTempFile("junit", null, temporaryFolder);
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
        ArrayList<AbstractBuild> list = new ArrayList<>();
        list.add(build);
        list.add(build2);

        Mockito.when(job.getBuilds()).thenReturn(RunList.fromRuns(list));
        Mockito.when(build.getParent()).thenReturn(job);

        //Act
        assertFalse(deletedFile.exists(),"The file should not exist before the test runs");
        StoreUtil.storeBuildMessages(deletedFile, build);

        //Assert
        String firstBuild = getLineFromFile(deletedFile, 1);
        String secondBuild = getLineFromFile(deletedFile, 2);

        int linesInFile = getLinesInFile(deletedFile);

        assertEquals(2, linesInFile, "The file should have 2 lines");
        assertEquals("34,12,56,FAILURE", firstBuild, "The data for the first build is not correct");
        assertEquals("89,67,10,SUCCESS", secondBuild, "The data for the second build is not correct");
    }

    @Test
    void testStoreMTTRInfo() throws Exception {
        //Arrange
        File rootFolder = newFolder(temporaryFolder, "junit");

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

        //Act
        StoreUtil.storeBuildMetric(MTTRMetric.class, build, info, info2);

        //Assert

        File propertiesFile = new File(rootFolder.getAbsolutePath() + File.separator + "mttr.properties");
        assertTrue(propertiesFile.exists(),
                "The mttr.properties file is missing" );

        List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
        assertEquals(2,lines.size(),"Should have only 2 lines");
        assertEquals("last7=76543210",lines.get(0),"The first  MTTR metric is wrong");
        assertEquals("last30=3210",lines.get(1),"The second  MTTR metric is wrong");
    }

    @Test
    void testStorStdDevInfo() throws Exception {
        //Arrange
        File rootFolder = newFolder(temporaryFolder, "junit");

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

        //Act
        StoreUtil.storeBuildMetric(StandardDeviationMetric.class, build, info, info2);

        //Assert

        File propertiesFile = new File(rootFolder.getAbsolutePath() + File.separator + "stddev.properties");
        assertTrue(propertiesFile.exists(),
                "The stddev.properties file is missing" );

        List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
        assertEquals(2,lines.size(),"Should have only 2 lines");
        assertEquals("last7=76543210",lines.get(0),"The first  stddev metric is wrong");
        assertEquals("last30=3210",lines.get(1),"The second  stddev metric is wrong");
    }

    @Test
    void testStoreMTTFInfo() throws Exception {
        //Arrange
        File rootFolder = newFolder(temporaryFolder, "junit");

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

        //Act
        StoreUtil.storeBuildMetric(MTTFMetric.class, build, info, info2);

        //Assert
        File propertiesFile = new File(rootFolder.getAbsolutePath() + File.separator + "mttf.properties");
        assertTrue(propertiesFile.exists(),
                "The mttf.properties file is missing: "+ propertiesFile);

        List<String> lines = Files.readLines(propertiesFile, Charset.defaultCharset());
        assertEquals(2,lines.size(),"Should have only 2 lines");
        assertEquals("last7=76543210",lines.get(0),"The first MTTF metric is wrong");
        assertEquals("last30=3210",lines.get(1),"The second  MTTF metric is wrong");
    }

    @Test
    void testStoreGraph() throws Exception {
        //Arrange
        File rootFolder = newFolder(temporaryFolder, "junit");

        Job job = Mockito.mock(Job.class);
        Mockito.when(job.getRootDir()).thenReturn(rootFolder);

        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getParent()).thenReturn(job);

        JFreeChart chart = Mockito.mock(JFreeChart.class);
        Mockito.when(chart.createBufferedImage(500,500)).thenReturn(new BufferedImage(500,500, BufferedImage.TYPE_INT_RGB));
        //Act
        StoreUtil.storeGraph(MTTFMetric.class, build, chart);

        //Assert
        File graphFile = new File(rootFolder.getAbsolutePath() + File.separator + "mttf.jpg");
        assertTrue(graphFile.exists(),
                "The mttf.jpg file is missing: " + graphFile);
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
        } while (--i>0);
        return (s);
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