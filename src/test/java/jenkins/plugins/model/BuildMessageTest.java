package jenkins.plugins.model;

import static org.junit.Assert.*;

import hudson.model.Result;
import java.util.Date;
import org.junit.Test;

/** Created by aidan on 12/10/14. */
public class BuildMessageTest {

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
  private static final BuildMessage SIXTH_BUILD_II =
      new BuildMessage(6, TODAY + 6000, 7000, Result.UNSTABLE.toString());

  private static final BuildMessage NULL_BUILD = null;

  @Test
  public void test_FirstBuildShouldBeTheSameAsFirstBuildWhenCompared() {
    assertTrue(FIRST_BUILD.compareTo(FIRST_BUILD) == 0);
  }

  @Test
  public void test_SecondBuildShouldBeGreaterThanFirstBuildWhenCompared() {
    assertTrue(FIRST_BUILD.compareTo(SECOND_BUILD) < 0);
    assertTrue(SECOND_BUILD.compareTo(FIRST_BUILD) > 0);
  }

  @Test
  public void test_BuildMessageIsEqualToIself() {
    assertTrue(THIRD_BUILD.equals(THIRD_BUILD));
  }

  @Test
  public void test_BuildMessageIsNotEqualToNull() {
    assertFalse(FOURTH_BUILD.equals(NULL_BUILD));
  }

  @Test
  public void test_BuildMessageIsNotEqualToDifferentType() {
    assertFalse(FIFTH_BUILD.equals(this));
  }

  @Test
  public void test_BuildMessageIsNotEqualToDifferentBuildMessage() {
    assertFalse(SIXTH_BUILD.equals(FIRST_BUILD));
  }

  @Test
  public void test_BuildMessageIsEqualToForSameBuildNumber() {
    assertTrue(SIXTH_BUILD.equals(SIXTH_BUILD_II));
    assertEquals(SIXTH_BUILD.hashCode(), SIXTH_BUILD_II.hashCode());
  }
}
