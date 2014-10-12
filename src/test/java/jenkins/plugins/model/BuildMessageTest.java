package jenkins.plugins.model;

import hudson.model.Result;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by aidan on 12/10/14.
 */
public class BuildMessageTest {

    private static final long TODAY = new Date().getTime();
    private static final BuildMessage FIRST_BUILD = new BuildMessage(1, TODAY, 1500, Result.SUCCESS.toString());
    private static final BuildMessage SECOND_BUILD = new BuildMessage(2, TODAY + 1000, 2500, Result.FAILURE.toString());
    private static final BuildMessage THIRD_BUILD = new BuildMessage(3, TODAY + 2000, 3500, Result.FAILURE.toString());
    private static final BuildMessage FOURTH_BUILD = new BuildMessage(4, TODAY + 3000, 4500, Result.SUCCESS.toString());
    private static final BuildMessage FIFTH_BUILD = new BuildMessage(5, TODAY + 4000, 5500, Result.FAILURE.toString());
    private static final BuildMessage SIXTH_BUILD = new BuildMessage(6, TODAY + 5000, 6500, Result.SUCCESS.toString());

    @Test
    public void FirstBuildShouldBeTheSameAsFirstBuildWhenCompared() {
        assertTrue(FIRST_BUILD.compareTo(FIRST_BUILD)==0);
    }
    @Test
    public void SecondBuildShouldBeGreaterThanFirstBuildWhenCompared() {
        assertTrue(FIRST_BUILD.compareTo(SECOND_BUILD)<0);
        assertTrue(SECOND_BUILD.compareTo(FIRST_BUILD)>0);
    }
}
