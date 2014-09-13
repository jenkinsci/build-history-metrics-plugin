package jenkins.plugins.util;


import hudson.model.*;
import hudson.util.RunList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

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
    public void testStoreJobFailedInfo() throws Exception {

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