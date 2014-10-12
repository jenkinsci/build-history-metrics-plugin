package jenkins.plugins.model;

import com.google.common.collect.Lists;
import hudson.model.Result;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by aidan on 06/10/14.
 */
public class StandardDeviationMetricTest {
    private static final long TODAY = new Date().getTime();
    private static final BuildMessage FIRST_BUILD = new BuildMessage(1, TODAY, 1500, Result.SUCCESS.toString());
    private static final BuildMessage SECOND_BUILD = new BuildMessage(2, TODAY + 1000, 2500, Result.FAILURE.toString());
    private static final BuildMessage THIRD_BUILD = new BuildMessage(3, TODAY + 2000, 3500, Result.FAILURE.toString());
    private static final BuildMessage FOURTH_BUILD = new BuildMessage(4, TODAY + 3000, 4500, Result.SUCCESS.toString());
    private static final BuildMessage FIFTH_BUILD = new BuildMessage(5, TODAY + 4000, 5500, Result.FAILURE.toString());
    private static final BuildMessage SIXTH_BUILD = new BuildMessage(6, TODAY + 5000, 6500, Result.SUCCESS.toString());
    private static final BuildMessage SEVENTH_BUILD = new BuildMessage(7, TODAY + 7000, 7500, Result.ABORTED.toString());
    private static final BuildMessage EIGHTH_BUILD = new BuildMessage(8, TODAY + 8000, 8500, Result.UNSTABLE.toString());


    @Test
    public void should_return_1_5_seconds_when_first_success_build() {
        runAndVerifyResult(Lists.newArrayList(FIRST_BUILD), 0, 1);
    }
    @Test
    public void should_return_2_5_seconds_when_first_failure_build() {
        runAndVerifyResult(Lists.newArrayList(SECOND_BUILD), 0, 1);
    }
    @Test
    public void should_return_7_5_seconds_when_first_aborted_build() {
        runAndVerifyResult(Lists.newArrayList(SEVENTH_BUILD), 0, 1);
    }
    @Test
    public void should_return_8_5_seconds_when_first_unstable_build() {
        runAndVerifyResult(Lists.newArrayList(EIGHTH_BUILD), 0, 1);
    }
    @Test
    public void should_return_correct_std_with_8_builds() {
        runAndVerifyResult(Lists.newArrayList(FIRST_BUILD, SECOND_BUILD,THIRD_BUILD, FOURTH_BUILD,
                FIFTH_BUILD,SIXTH_BUILD,SEVENTH_BUILD,EIGHTH_BUILD), 2449, 8);
    }

    @Test
    public void should_return_failed_info_when_have_all_builds_regardless_of_order_they_are_added() {
        List<BuildMessage> builds = Lists.newArrayList( EIGHTH_BUILD, SEVENTH_BUILD, SIXTH_BUILD,
                FIFTH_BUILD, FOURTH_BUILD, THIRD_BUILD, SECOND_BUILD, FIRST_BUILD);
        runAndVerifyResult(builds, 2449, 8);
    }

    @Test
    public void should_return_correct_std_with_2_builds() {
        runAndVerifyResult(Lists.newArrayList(FIRST_BUILD, SECOND_BUILD), 707L, 2);
    }

    private void runAndVerifyResult(List<BuildMessage> builds, long expectTime, int expectCount) {
        AggregateBuildMetric mttrMetric = new StandardDeviationMetric("test", builds);
        assertEquals("Metric Name", "test", mttrMetric.getName());
        assertEquals("StandardDeviationMetric", expectTime, mttrMetric.calculateMetric());
        assertEquals("Build Count", expectCount, mttrMetric.getOccurences());
    }
}
