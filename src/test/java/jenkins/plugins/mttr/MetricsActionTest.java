package jenkins.plugins.mttr;

import com.google.common.io.Files;
import hudson.model.FreeStyleProject;
import jenkins.plugins.util.StoreUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import static jenkins.plugins.util.StoreUtil.UTF_8;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MetricsActionTest {

    private FreeStyleProject project;
    private String rootDirectory;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        project = jenkins.getInstance().createProject(FreeStyleProject.class, "test");
        rootDirectory = project.getRootDir() + File.separator;
    }

    @Test
    public void should_store_build_message_file_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        assertTrue(buildsMessageFile.exists());

        List<String> lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));

        String[] line1 = lines.get(0).split(",");
        assertThat(line1[0], is("1"));
        assertThat(line1[2], is("SUCCESS"));

        project.scheduleBuild2(1).get();

        lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));
        String[] line2 = lines.get(1).split(",");
        assertThat(lines.size(), is(2));
        assertThat(line2[0], is("2"));
        assertThat(line2[2], is("SUCCESS"));
    }

    @Test
    public void should_store_mttr_properties_file_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File propertyFile = new File(rootDirectory + StoreUtil.MTTR_PROPERTY_FILE);
        assertTrue(propertyFile.exists());

        List<String> lines = Files.readLines(propertyFile, Charset.forName(UTF_8));

        assertThat(lines.get(0), is(String.format("%s=0", MetricsAction.MTTR_LAST_7_DAYS)));
        assertThat(lines.get(1), is(String.format("%s=0", MetricsAction.MTTR_LAST_30_DAYS)));
        assertThat(lines.get(2), is(String.format("%s=0", MetricsAction.MTTR_ALL_BUILDS)));
    }

    @Test
    public void should_store_mttf_properties_file_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File propertyFile = new File(rootDirectory + StoreUtil.MTTF_PROPERTY_FILE);
        assertTrue(propertyFile.exists());

        List<String> lines = Files.readLines(propertyFile, Charset.forName(UTF_8));

        assertThat(lines.get(0), is(String.format("%s=0", MetricsAction.MTTF_LAST_7_DAYS)));
        assertThat(lines.get(1), is(String.format("%s=0", MetricsAction.MTTF_LAST_30_DAYS)));
        assertThat(lines.get(2), is(String.format("%s=0", MetricsAction.MTTF_ALL_BUILDS)));
    }

    @Test
    public void should_store_all_build_message_when_store_file_not_exist() throws Exception {
        project.scheduleBuild2(0).get();
        project.scheduleBuild2(1).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        buildsMessageFile.delete();

        project.scheduleBuild2(3).get();

        assertTrue(buildsMessageFile.exists());
        List<String> lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));
        assertThat(lines.size(), is(3));
    }
}
