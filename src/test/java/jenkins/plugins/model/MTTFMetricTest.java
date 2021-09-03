package jenkins.plugins.model;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import hudson.model.Result;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MTTFMetricTest {
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
  public void test_ShouldReturn0SecondWhenSecondFailedBuild() {
    List<BuildMessage> builds = Lists.newArrayList(SECOND_BUILD, THIRD_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_ShouldReturn0SecondsWhen2SuccessfulBuild() {
    List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, FOURTH_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_Should0SecondsWhen1failedAnd1SuccessfulBuild() {
    List<BuildMessage> builds = Lists.newArrayList(THIRD_BUILD, FOURTH_BUILD);
    runAndVerifyResult(builds, 0, 0);
  }

  @Test
  public void test_ShouldCorrectMetricWhen1SuccessAnd1FailedBuild() {
    List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, SECOND_BUILD);
    runAndVerifyResult(builds, 1000L, 1);
  }

  @Test
  public void test_ShouldCorrectMetricWhen2SuccessfulAnd1FailedBuild() {
    List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, FOURTH_BUILD, FIFTH_BUILD);
    runAndVerifyResult(builds, 4000L, 1);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhenHaveAllBuilds() {
    List<BuildMessage> builds =
        Lists.newArrayList(
            FIRST_BUILD, SECOND_BUILD, THIRD_BUILD, FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD);
    runAndVerifyResult(builds, 1000L, 2);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhenHaveAllBuildsRegardlessOfOrderTheyAreAdded() {
    List<BuildMessage> builds =
        Lists.newArrayList(
            SIXTH_BUILD, FIFTH_BUILD, FOURTH_BUILD, THIRD_BUILD, SECOND_BUILD, FIRST_BUILD);
    runAndVerifyResult(builds, 1000L, 2);
  }

  @Test
  public void test_ShouldReturnFailedInfoWhenStartingWithAFailedBuild() {
    List<BuildMessage> builds =
        Lists.newArrayList(SECOND_BUILD, THIRD_BUILD, FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD);
    runAndVerifyResult(builds, 1000L, 1);
  }

  private void runAndVerifyResult(List<BuildMessage> builds, long expectTime, int expectCount) {
    AggregateBuildMetric mttfMetric = new MTTFMetric("test", builds);
    assertEquals("Metric Name", "test", mttfMetric.getName());
    assertEquals("MTTF Metric", expectTime, mttfMetric.calculateMetric());
    assertEquals("Build Count", expectCount, mttfMetric.getOccurences());
  }
}
