package jenkins.plugins.mttr;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import com.google.common.io.Files;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.Shell;
import jenkins.plugins.util.StoreUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static jenkins.plugins.util.StoreUtil.UTF_8;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {

    private FreeStyleProject project;
    private String rootDirectory;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        project = jenkins.getInstance().createProject(FreeStyleProject.class, "test");
        rootDirectory = project.getRootDir() + File.separator;
    }
    private TestBuilder getTestBuilder() {
        return new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("abc.txt").write("hello","UTF-8");
                return true;
            }
        };
    }
    @Test
    public void ShouldRenderTheMetricsInATable() throws Exception {
        project.getBuildersList().add(new Shell("return `expr $BUILD_NUMBER % 2`"));
        project.getBuildersList().add(getTestBuilder());

        for(int i=0; i<4; i++) {
            project.scheduleBuild2(0).get();
            Thread.sleep(1);
        }

        HtmlPage page = jenkins.createWebClient().goTo("job/test/");
        HtmlElement metricsTable = page.getHtmlElementById("aggregate-build-metrics");
        assertEquals("Metrics should be in a table", metricsTable.getNodeName(), "table");

        verifyMetricRow(metricsTable, MetricsAction.MTTF_LAST_7_DAYS, "Last 7 Days");
        verifyMetricRow(metricsTable, MetricsAction.MTTF_LAST_30_DAYS, "Last 30 Days");
        verifyMetricRow(metricsTable, MetricsAction.MTTF_ALL_BUILDS, "All Time");

        verifyMetricRow(metricsTable, MetricsAction.MTTR_LAST_7_DAYS, "Last 7 Days");
        verifyMetricRow(metricsTable, MetricsAction.MTTR_LAST_30_DAYS, "Last 30 Days");
        verifyMetricRow(metricsTable, MetricsAction.MTTR_ALL_BUILDS, "All Time");

        verifyMetricRow(metricsTable, MetricsAction.STDDEV_LAST_7_DAYS, "Last 7 Days");
        verifyMetricRow(metricsTable, MetricsAction.STDDEV_LAST_30_DAYS, "Last 30 Days");
        verifyMetricRow(metricsTable, MetricsAction.STDDEV_ALL_BUILDS, "All Time");
    }

    private HtmlElement verifyMetricRow(HtmlElement metricsTable, String metricElementIdentifier, String expectedLabel) {
        HtmlElement row = metricsTable.getOneHtmlElementByAttribute("tr", "id", metricElementIdentifier);
        assertEquals(metricElementIdentifier + "Metric should be in a row", row.getNodeName(), "tr");
        String label = row.getOneHtmlElementByAttribute("td", "class", "jenkins-table__cell metric-label").getTextContent();
        assertEquals(metricElementIdentifier + "Metric labeled incorrectly", expectedLabel, label);
        String value = row.getOneHtmlElementByAttribute("td", "class", "jenkins-table__cell metric-value").getTextContent();
        assertNotEquals("Metric value should be set", 0, value.length());
        assertNotEquals("Metric value should not be zero", "0", value);
        return metricsTable;
    }


    @Test
    public void should_store_build_message_file_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        assertTrue(buildsMessageFile.exists());

        List<String> lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));

        String[] line1 = lines.get(0).split(",");
        assertThat("Build number", line1[0], is("1"));
        assertThat("Build Status", line1[3], is("SUCCESS"));

        project.scheduleBuild2(1).get();

        lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));
        line1 = lines.get(0).split(",");
        String[] line2 = lines.get(1).split(",");
        assertThat(lines.size(), is(2));
        assertThat("Build number", line1[0], is("1"));
        assertThat("Build Status", line1[3], is("SUCCESS"));
        assertThat("Build Number", line2[0], is("2"));
        assertThat("Build Status", line2[3], is("SUCCESS"));
    }

    @Test
    public void should_store_properties_files_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File mttrPropertyFile = new File(rootDirectory + StoreUtil.MTTR_PROPERTY_FILE);
        assertTrue(mttrPropertyFile.exists());

        File mttfPropertyFile = new File(rootDirectory + StoreUtil.MTTF_PROPERTY_FILE);
        assertTrue(mttfPropertyFile.exists());

        File stdDevPropertyFile = new File(rootDirectory + StoreUtil.STDDEV_PROPERTY_FILE);
        assertTrue(stdDevPropertyFile.exists());

        List<String> mttrLines = Files.readLines(mttrPropertyFile, Charset.forName(UTF_8));
        List<String> mttfLines = Files.readLines(mttfPropertyFile, Charset.forName(UTF_8));
        List<String> stdDevLines = Files.readLines(stdDevPropertyFile, Charset.forName(UTF_8));

        assertThat(mttrLines.get(0), is(String.format("%s=0", MetricsAction.MTTR_LAST_7_DAYS)));
        assertThat(mttrLines.get(1), is(String.format("%s=0", MetricsAction.MTTR_LAST_30_DAYS)));
        assertThat(mttrLines.get(2), is(String.format("%s=0", MetricsAction.MTTR_ALL_BUILDS)));

        assertThat(mttfLines.get(0), is(String.format("%s=0", MetricsAction.MTTF_LAST_7_DAYS)));
        assertThat(mttfLines.get(1), is(String.format("%s=0", MetricsAction.MTTF_LAST_30_DAYS)));
        assertThat(mttfLines.get(2), is(String.format("%s=0", MetricsAction.MTTF_ALL_BUILDS)));

        assertThat(stdDevLines.get(0), is(String.format("%s=0", MetricsAction.STDDEV_LAST_7_DAYS)));
        assertThat(stdDevLines.get(1), is(String.format("%s=0", MetricsAction.STDDEV_LAST_30_DAYS)));
        assertThat(stdDevLines.get(2), is(String.format("%s=0", MetricsAction.STDDEV_ALL_BUILDS)));
    }

    @Test
    public void should_store_all_build_messages_when_build_message_file_does_not_exist() throws Exception {
        project.scheduleBuild2(0).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        buildsMessageFile.delete();

        project.scheduleBuild2(0).get();

        assertTrue(buildsMessageFile.exists());
        List<String> lines = Files.readLines(buildsMessageFile, Charset.forName(UTF_8));
        assertThat(lines.size(), is(2));
    }
}
