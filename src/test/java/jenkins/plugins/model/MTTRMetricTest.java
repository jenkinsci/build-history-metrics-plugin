package jenkins.plugins.model;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import hudson.model.Result;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MTTRMetricTest {

  private static final long TODAY = new Date().getTime();
  private static final BuildMessage FIRST_BUILD =
      new BuildMessage(1, TODAY, 1500, Result.SUCCESS.toString());
  private static final BuildMessage SECOND_BUILD =
      new BuildMessage(2, TODAY + 1000, 2500, Result.FAILURE.toString());
  private static final BuildMessage THIRD_BUILD =
      new BuildMessage(3, TODAY + 2000, 3500, Result.FAILURE.toString());
  private static final BuildMessage FOURTH_BUILD =
      new BuildMessage(4, TODAY + 3000, 4500, Result.SUCCESS.toString());
  private static final BuildMessage FIFTH_BUILD =
      new BuildMessage(5, TODAY + 4000, 5500, Result.FAILURE.toString());
  private static final BuildMessage SIXTH_BUILD =
      new BuildMessage(6, TODAY + 5000, 6500, Result.SUCCESS.toString());

  @Before
  public void setUp() throws Exception {}

  @Test
  public void test_ShouldReturn0SecondWhenFirstSuccessfulBuild() {
    runAndVerifyResult(Lists.newArrayList(FIRST_BUILD), 0L, 0);
  }

  @Test
  public void test_ShouldReturn0SecondWhenFirstFailedBuild() {
    runAndVerifyResult(Lists.newArrayList(SECOND_BUILD), 0L, 0);
  }

  @Test
  public void test_ShouldReturn0SecondWhen2FailedBuilds() {
    List<BuildMessage> builds = Lists.newArrayList(SECOND_BUILD, THIRD_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_ShouldReturn0SecondsWhen2SuccessfulBuild() {
    List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, FOURTH_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_ShouldReturn0SecondsWhen1successfulAnd1FailedBuild() {
    List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, THIRD_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhen1FailedAnd1SuccessfulBuild() {
    List<BuildMessage> builds = Lists.newArrayList(THIRD_BUILD, FOURTH_BUILD);
    runAndVerifyResult(builds, 1000L, 1);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhen2FailedAnd1SuccessfulBuild() {
    List<BuildMessage> builds = Lists.newArrayList(SECOND_BUILD, THIRD_BUILD, FOURTH_BUILD);
    runAndVerifyResult(builds, 2000L, 1);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhenHaveAllBuild() {
    List<BuildMessage> builds =
        Lists.newArrayList(
            FIRST_BUILD, SECOND_BUILD, THIRD_BUILD, FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD);
    runAndVerifyResult(builds, 1500L, 2);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhenHaveAllBuildsRegardlessOfOrderTheyAreAdded() {
    List<BuildMessage> builds =
        Lists.newArrayList(
            SIXTH_BUILD, FIFTH_BUILD, FOURTH_BUILD, THIRD_BUILD, SECOND_BUILD, FIRST_BUILD);
    runAndVerifyResult(builds, 1500L, 2);
  }

  private void runAndVerifyResult(List<BuildMessage> builds, long expectTime, int expectCount) {
    MTTRMetric mttrMetric = new MTTRMetric("test", builds);
    assertEquals("Metric Name", "test", mttrMetric.getName());
    assertEquals("MTTR Metric", expectTime, mttrMetric.calculateMetric());
    assertEquals("Build Count", expectCount, mttrMetric.getOccurences());
  }
}
