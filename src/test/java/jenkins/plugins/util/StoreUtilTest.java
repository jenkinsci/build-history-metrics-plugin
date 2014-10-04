package jenkins.plugins.util;


import hudson.model.*;
import hudson.util.RunList;
import jenkins.plugins.model.AggregateBuildMetric;
import jenkins.plugins.model.MTTFMetric;
import jenkins.plugins.model.MTTRMetric;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class StoreUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public File createTempFileWithLines(int lines) throws IOException {
        File existingFile = temporaryFolder.newFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(existingFile));
        for(int i=0; i<lines; i++) {
            out.write("blah\n");
        }
        out.close();
        return existingFile;
    }


    @Test
    public void StoreBuildMessages_ShouldAppendTheData_whenTheFileExists()
            throws Exception {
        //Arrange
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(5678);
        File existingFile = createTempFileWithLines(2);

        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.FAILURE);
        Mockito.when(build.getNumber()).thenReturn(123);
        Mockito.when(build.getTimestamp()).thenReturn(timestamp);

        //Act
        StoreUtil.storeBuildMessages(existingFile, build);

        //Assert
        String storedString = getLineFromFile(existingFile, 3);
        int linesInFile = getLinesInFile(existingFile);

        assertEquals("The file should have 3 lines",
                3, linesInFile);
        assertEquals("The data should be in the form BUILDNUMBER,TIMEINMILLIS,RESULT",
                "123,5678,FAILURE", storedString);
    }

    @Test
    public void StoreBuildMessages_ShouldIncludeAllJobsFromParent_whenTheFileDoesNotExist()
            throws Exception {
        //Arrange
        File deletedFile = temporaryFolder.newFile();
        deletedFile.delete();

        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(12);
        Calendar timestamp2 = Calendar.getInstance();
        timestamp2.setTimeInMillis(67);

        AbstractBuild build = Mockito.mock(AbstractBuild.class);
        Mockito.when(build.getResult()).thenReturn(Result.FAILURE);
        Mockito.when(build.getNumber()).thenReturn(34);
        Mockito.when(build.getTimestamp()).thenReturn(timestamp);

        AbstractBuild build2 = Mockito.mock(AbstractBuild.class);
        Mockito.when(build2.getResult()).thenReturn(Result.SUCCESS);
        Mockito.when(build2.getNumber()).thenReturn(89);
        Mockito.when(build2.getTimestamp()).thenReturn(timestamp2);

        Job job = Mockito.mock(Job.class);
        ArrayList<AbstractBuild> list = new ArrayList<AbstractBuild>();
        list.add(build);
        list.add(build2);

        Mockito.when(job.getBuilds()).thenReturn(RunList.fromRuns(list));
        Mockito.when(build.getParent()).thenReturn(job);

        //Act
        assertFalse("The file should not exist before the test runs",deletedFile.exists());
        StoreUtil.storeBuildMessages(deletedFile, build);

        //Assert
        String firstBuild = getLineFromFile(deletedFile, 1);
        String secondBuild = getLineFromFile(deletedFile, 2);

        int linesInFile = getLinesInFile(deletedFile);

        assertEquals("The file should have 2 lines",
                2, linesInFile);
        assertEquals("The data for the first build is not correct",
                "34,12,FAILURE", firstBuild);
        assertEquals("The data for the second build is not correct",
                "89,67,SUCCESS", secondBuild);
    }

    @Test
    public void testStoreMTTRInfo() throws Exception {
        //Arrange
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

        //Act
        StoreUtil.storeBuildMetric(MTTRMetric.class, build, info, info2);

        //Assert
        Path propertiesFile = Paths.get(rootFolder.getAbsolutePath(), "mttr.properties");
        assertTrue("The mttr.properties file is missing",
                Files.exists(propertiesFile) );

        List<String> lines = Files.readAllLines(propertiesFile, Charset.defaultCharset());
        assertEquals("Should have only 2 lines",2,lines.size());
        assertEquals("The first  MTTR metric is wrong","last7=76543210",lines.get(0));
        assertEquals("The second  MTTR metric is wrong","last30=3210",lines.get(1));
    }

    @Test
    public void testStoreMTTFInfo() throws Exception {
        //Arrange
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

        //Act
        StoreUtil.storeBuildMetric(MTTFMetric.class, build, info, info2);

        //Assert
        Path propertiesFile = Paths.get(rootFolder.getAbsolutePath(), "mttf.properties");
        assertTrue("The mttf.properties file is missing: "+ propertiesFile.toAbsolutePath().toString(),
                Files.exists(propertiesFile) );

        List<String> lines = Files.readAllLines(propertiesFile, Charset.defaultCharset());
        assertEquals("Should have only 2 lines",2,lines.size());
        assertEquals("The first MTTF metric is wrong","last7=76543210",lines.get(0));
        assertEquals("The second  MTTF metric is wrong","last30=3210",lines.get(1));
    }

    private boolean fileExistsInFolder(final String filename, File folder) {
        return folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(filename);
            }
        }).length == 1;
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
}