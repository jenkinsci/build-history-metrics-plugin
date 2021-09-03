package jenkins.plugins.model;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import hudson.model.Result;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class StandardDeviationMetricTest {
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
  private static final BuildMessage SEVENTH_BUILD =
      new BuildMessage(7, TODAY + 7000, 7500, Result.ABORTED.toString());
  private static final BuildMessage EIGHTH_BUILD =
      new BuildMessage(8, TODAY + 8000, 8500, Result.UNSTABLE.toString());

  @Test
  public void test_ShouldReturn0SecondsWhenFirstSuccessBuild() {
    runAndVerifyResult(Lists.newArrayList(FIRST_BUILD), 0, 1);
  }

  @Test
  public void test_ShouldReturn0SecondsWhenFirstFailureBuild() {
    runAndVerifyResult(Lists.newArrayList(SECOND_BUILD), 0, 1);
  }

  @Test
  public void test_ShouldReturn0SecondsWhenFirstAbortedBuild() {
    runAndVerifyResult(Lists.newArrayList(SEVENTH_BUILD), 0, 1);
  }

  @Test
  public void test_ShouldReturn0SecondsWhenFirstUnstableBuild() {
    runAndVerifyResult(Lists.newArrayList(EIGHTH_BUILD), 0, 1);
  }

  @Test
  public void test_ShouldReturnCorrectStddevWith8builds() {
    runAndVerifyResult(
        Lists.newArrayList(
            FIRST_BUILD,
            SECOND_BUILD,
            THIRD_BUILD,
            FOURTH_BUILD,
            FIFTH_BUILD,
            SIXTH_BUILD,
            SEVENTH_BUILD,
            EIGHTH_BUILD),
        2449,
        8);
  }

  @Test
  public void
      test_ShouldReturnStddevWhenHaveAllBuildsRegardlessOfOrderTheyAreAdded() {
    List<BuildMessage> builds =
        Lists.newArrayList(
            EIGHTH_BUILD,
            SEVENTH_BUILD,
            SIXTH_BUILD,
            FIFTH_BUILD,
            FOURTH_BUILD,
            THIRD_BUILD,
            SECOND_BUILD,
            FIRST_BUILD);
    runAndVerifyResult(builds, 2449, 8);
  }

  @Test
  public void test_ShouldReturnCorrectStddevWith2Builds() {
    runAndVerifyResult(Lists.newArrayList(FIRST_BUILD, SECOND_BUILD), 707L, 2);
  }

  private void runAndVerifyResult(
      List<BuildMessage> builds, long expectTime, int expectCount) {
    AggregateBuildMetric stddevMetric =
        new StandardDeviationMetric("test", builds);
    assertEquals("Metric Name", "test", stddevMetric.getName());
    assertEquals(
        "StandardDeviationMetric", expectTime, stddevMetric.calculateMetric());
    assertEquals("Build Count", expectCount, stddevMetric.getOccurences());
  }
}
