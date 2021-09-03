package jenkins.plugins.mttr;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.common.io.Files;
import hudson.model.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jenkins.plugins.util.StoreUtil;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class WorkflowJobTest {
  @Rule public JenkinsRule jenkins = new JenkinsRule();

  private WorkflowJob project;
  private View view;
  private String rootDirectory;

  @Before
  public void setUp() throws Exception {
    project = jenkins.createProject(WorkflowJob.class, "wfj_test");

    project.setDefinition(
        new CpsFlowDefinition(
            "node { \n"
                + "    if (env.BUILD_NUMBER.toInteger().mod(2) == 0) {\n"
                + "        echo \"Fail\"\n"
                + "        currentBuild.result = 'FAILURE'\n"
                + "    } else {\n"
                + "        echo 'Pass'\n"
                + "    }\n"
                + "}",
            true));
    // Standard View with no 'extra' columns
    view = new ListView("wfj_viw");
    jenkins.getInstance().addView(view);
    rootDirectory = project.getRootDir() + File.separator;
  }

  @Test
  public void test_ShouldRenderTheMetricsInATable() throws Exception {
    for (int i = 0; i < 4; i++) {
      project.scheduleBuild2(0).get();
      Thread.sleep(1);
    }

    HtmlPage page = jenkins.createWebClient().goTo(project.getUrl());
    // Do we have the the Element?
    WebAssert.assertElementPresent(page, "aggregate-build-metrics");
    // Is it a table
    HtmlElement metricsTable =
        (HtmlElement) page.getElementById("aggregate-build-metrics");
    assertEquals(
        "Metrics should be in a table", metricsTable.getNodeName(), "table");

    verifyMetricRow(
        metricsTable, MetricsAction.MTTF_LAST_7_DAYS, "Last 7 Days");
    verifyMetricRow(
        metricsTable, MetricsAction.MTTF_LAST_30_DAYS, "Last 30 Days");
    verifyMetricRow(metricsTable, MetricsAction.MTTF_ALL_BUILDS, "All Time");

    verifyMetricRow(
        metricsTable, MetricsAction.MTTR_LAST_7_DAYS, "Last 7 Days");
    verifyMetricRow(
        metricsTable, MetricsAction.MTTR_LAST_30_DAYS, "Last 30 Days");
    verifyMetricRow(metricsTable, MetricsAction.MTTR_ALL_BUILDS, "All Time");

    verifyMetricRow(
        metricsTable, MetricsAction.STDDEV_LAST_7_DAYS, "Last 7 Days");
    verifyMetricRow(
        metricsTable, MetricsAction.STDDEV_LAST_30_DAYS, "Last 30 Days");
    verifyMetricRow(metricsTable, MetricsAction.STDDEV_ALL_BUILDS, "All Time");
  }

  private HtmlElement verifyMetricRow(
      HtmlElement metricsElement,
      String metricElementIdentifier,
      String expectedLabel) {

    HtmlTable metricsTable = (HtmlTable) metricsElement;
    HtmlElement row = metricsTable.getRowById(metricElementIdentifier);

    assertEquals(
        metricElementIdentifier + "Metric should be in a row",
        row.getNodeName(),
        "tr");
    String label =
        row.getOneHtmlElementByAttribute("td", "class", "metric-label")
            .getTextContent();
    assertEquals(
        metricElementIdentifier + "Metric labeled incorrectly",
        expectedLabel,
        label);
    String value =
        row.getOneHtmlElementByAttribute("td", "class", "metric-value")
            .getTextContent();
    assertNotEquals("Metric value should be set", 0, value.length());
    assertNotEquals("Metric value should not be zero", "0", value);
    return metricsTable;
  }

  @Test
  public void test_CanShowListView() throws Exception {
    HtmlPage page = jenkins.createWebClient().goTo(view.getUrl());

    assertTrue(true);
  }

  @Test
  public void test_ShouldStoreBuildMessageFileCorrectly() throws Exception {
    project.scheduleBuild2(0).get();

    File buildsMessageFile =
        new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
    assertTrue(buildsMessageFile.exists());

    List<String> lines =
        Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);

    String[] line1 = lines.get(0).split(",");
    assertEquals("Build number", "1", line1[0]);
    assertEquals("Build Status", "SUCCESS", line1[3]);

    project.scheduleBuild2(1).get();

    lines = Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);
    line1 = lines.get(0).split(",");
    String[] line2 = lines.get(1).split(",");
    assertEquals(2, lines.size());
    assertEquals("Build number", "1", line1[0]);
    assertEquals("Build Status", "SUCCESS", line1[3]);
    assertEquals("Build Number", "2", line2[0]);
    assertEquals("Build Status", "FAILURE", line2[3]);
  }

  @Test
  public void test_ShouldStorePropertiesFilesCorrectly() throws Exception {
    project.scheduleBuild2(0).get();

    File mttrPropertyFile =
        new File(rootDirectory + StoreUtil.MTTR_PROPERTY_FILE);
    assertTrue(mttrPropertyFile.exists());

    File mttfPropertyFile =
        new File(rootDirectory + StoreUtil.MTTF_PROPERTY_FILE);
    assertTrue(mttfPropertyFile.exists());

    File stdDevPropertyFile =
        new File(rootDirectory + StoreUtil.STDDEV_PROPERTY_FILE);
    assertTrue(stdDevPropertyFile.exists());

    List<String> mttrLines =
        Files.readLines(mttrPropertyFile, StandardCharsets.UTF_8);
    List<String> mttfLines =
        Files.readLines(mttfPropertyFile, StandardCharsets.UTF_8);
    List<String> stdDevLines =
        Files.readLines(stdDevPropertyFile, StandardCharsets.UTF_8);

    assertEquals(
        String.format("%s=0", MetricsAction.MTTR_LAST_7_DAYS),
        mttrLines.get(0));
    assertEquals(
        String.format("%s=0", MetricsAction.MTTR_LAST_30_DAYS),
        mttrLines.get(1));
    assertEquals(
        String.format("%s=0", MetricsAction.MTTR_ALL_BUILDS), mttrLines.get(2));

    assertEquals(
        String.format("%s=0", MetricsAction.MTTF_LAST_7_DAYS),
        mttfLines.get(0));
    assertEquals(
        String.format("%s=0", MetricsAction.MTTF_LAST_30_DAYS),
        mttfLines.get(1));
    assertEquals(
        String.format("%s=0", MetricsAction.MTTF_ALL_BUILDS), mttfLines.get(2));

    assertEquals(
        String.format("%s=0", MetricsAction.STDDEV_LAST_7_DAYS),
        stdDevLines.get(0));
    assertEquals(
        String.format("%s=0", MetricsAction.STDDEV_LAST_30_DAYS),
        stdDevLines.get(1));
    assertEquals(
        String.format("%s=0", MetricsAction.STDDEV_ALL_BUILDS),
        stdDevLines.get(2));
  }

  @Test
  public void test_ShouldStoreAllBuildMessagesWhenBuildMessageFileDoesNotExist()
      throws Exception {
    project.scheduleBuild2(0).get();

    File buildsMessageFile =
        new File(rootDirectory + MetricsAction.ALL_BUILDS_FILE_NAME);
    buildsMessageFile.delete();

    project.scheduleBuild2(0).get();

    assertTrue(buildsMessageFile.exists());
    List<String> lines =
        Files.readLines(buildsMessageFile, StandardCharsets.UTF_8);
    assertEquals(2, lines.size());
  }
}
