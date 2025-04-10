package jenkins.plugins.mttr;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import com.google.common.io.Files;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.Shell;
import jenkins.plugins.util.StoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class IntegrationTest {

    private FreeStyleProject project;
    private String rootDirectory;
    private JenkinsRule jenkins;


    @BeforeEach
    void setUp(JenkinsRule rule) throws Exception {
        jenkins = rule;
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
    void ShouldRenderTheMetricsInATable() throws Exception {
        project.getBuildersList().add(new Shell("return `expr $BUILD_NUMBER % 2`"));
        project.getBuildersList().add(getTestBuilder());

        for(int i=0; i<4; i++) {
            project.scheduleBuild2(0).get();
            Thread.sleep(1);
        }

        HtmlPage page = jenkins.createWebClient().goTo("job/test/");
        HtmlElement metricsTable = page.getHtmlElementById("aggregate-build-metrics");
        assertEquals("table", metricsTable.getNodeName(), "Metrics should be in a table");

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
        assertEquals("tr", row.getNodeName(), metricElementIdentifier + "Metric should be in a row");
        String label = row.getOneHtmlElementByAttribute("td", "class", "jenkins-table__cell metric-label").getTextContent();
        assertEquals(expectedLabel, label, metricElementIdentifier + "Metric labeled incorrectly");
        String value = row.getOneHtmlElementByAttribute("td", "class", "jenkins-table__cell metric-value").getTextContent();
        assertNotEquals(0, value.length(), "Metric value should be set");
        assertNotEquals("0", value, "Metric value should not be zero");
        return metricsTable;
    }

    @Test
    void should_store_build_message_file_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        assertTrue(buildsMessageFile.exists());

        List<String> lines = Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);

        String[] line1 = lines.get(0).split(",");
        assertThat("Build number", line1[0], is("1"));
        assertThat("Build Status", line1[3], is("SUCCESS"));

        project.scheduleBuild2(1).get();

        lines = Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);
        line1 = lines.get(0).split(",");
        String[] line2 = lines.get(1).split(",");
        assertThat(lines.size(), is(2));
        assertThat("Build number", line1[0], is("1"));
        assertThat("Build Status", line1[3], is("SUCCESS"));
        assertThat("Build Number", line2[0], is("2"));
        assertThat("Build Status", line2[3], is("SUCCESS"));
    }

    @Test
    void should_store_properties_files_correctly() throws Exception {
        project.scheduleBuild2(0).get();

        File mttrPropertyFile = new File(rootDirectory + StoreUtil.MTTR_PROPERTY_FILE);
        assertTrue(mttrPropertyFile.exists());

        File mttfPropertyFile = new File(rootDirectory + StoreUtil.MTTF_PROPERTY_FILE);
        assertTrue(mttfPropertyFile.exists());

        File stdDevPropertyFile = new File(rootDirectory + StoreUtil.STDDEV_PROPERTY_FILE);
        assertTrue(stdDevPropertyFile.exists());

        List<String> mttrLines = Files.readLines(mttrPropertyFile, StandardCharsets.UTF_8);
        List<String> mttfLines = Files.readLines(mttfPropertyFile, StandardCharsets.UTF_8);
        List<String> stdDevLines = Files.readLines(stdDevPropertyFile, StandardCharsets.UTF_8);

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
    void should_store_all_build_messages_when_build_message_file_does_not_exist() throws Exception {
        project.scheduleBuild2(0).get();

        File buildsMessageFile = new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
        buildsMessageFile.delete();

        project.scheduleBuild2(0).get();

        assertTrue(buildsMessageFile.exists());
        List<String> lines = Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);
        assertThat(lines.size(), is(2));
    }
}
