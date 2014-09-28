package jenkins.plugins.model;

import com.google.common.collect.Lists;
import hudson.model.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MTTFMetricTest {
    private static final long TODAY = new Date().getTime();
    private static final BuildMessage FIRST_BUILD = new BuildMessage(TODAY, Result.SUCCESS.toString());
    private static final BuildMessage SECOND_BUILD = new BuildMessage(TODAY + 1000, Result.FAILURE.toString());
    private static final BuildMessage THIRD_BUILD = new BuildMessage(TODAY + 2000, Result.FAILURE.toString());
    private static final BuildMessage FOURTH_BUILD = new BuildMessage(TODAY + 3000, Result.SUCCESS.toString());
    private static final BuildMessage FIFTH_BUILD = new BuildMessage(TODAY + 4000, Result.FAILURE.toString());
    private static final BuildMessage SIXTH_BUILD = new BuildMessage(TODAY + 5000, Result.SUCCESS.toString());

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void should_return_0_second_when_first_success_build() {
        runAndVerifyResult(Lists.newArrayList(FIRST_BUILD), 0L, 0);
    }
    @Test
    public void should_return_0_second_when_2_failed_builds() {
        List<BuildMessage> builds = Lists.newArrayList(SECOND_BUILD, THIRD_BUILD);
        runAndVerifyResult(builds, 0, 0);
    }
    @Test
    public void should_return_0_seconds_when_2_success_builds() {
        List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, FOURTH_BUILD);
        runAndVerifyResult(builds, 0, 0);
    }
    @Test
    public void should_0_seconds_when_1_failed_and_1_success_builds() {
        List<BuildMessage> builds = Lists.newArrayList(THIRD_BUILD, FOURTH_BUILD);
        runAndVerifyResult(builds, 0, 0);
    }
    @Test
    public void should_correct_metric_when_1_success_and_1_failed_builds() {
        List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, SECOND_BUILD);
        runAndVerifyResult(builds, 1000L, 1);
    }
    @Test
    public void should_correct_metric_when_2_success_and_1_failed_builds() {
        List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, FOURTH_BUILD, FIFTH_BUILD);
        runAndVerifyResult(builds, 4000L, 1);
    }
    @Test
    public void should_return_failed_info_when_have_all_builds() {
        List<BuildMessage> builds = Lists.newArrayList(FIRST_BUILD, SECOND_BUILD, THIRD_BUILD,
                FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD);
        runAndVerifyResult(builds, 1000L, 2);
    }
    @Test
    public void should_return_failed_info_when_starting_with_a_failed_build() {
        List<BuildMessage> builds = Lists.newArrayList(SECOND_BUILD, THIRD_BUILD,
                FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD);
        runAndVerifyResult(builds, 1000L, 1);
    }
    private void runAndVerifyResult(List<BuildMessage> builds, long expectTime, int expectCount) {
        AggregateBuildMetric mttfMetric = new MTTFMetric("test", builds);
        assertEquals("Metric Name", "test", mttfMetric.getName());
        assertEquals("MTTF Metric", expectTime, mttfMetric.calculateMetric());
        assertEquals("Build Count", expectCount, mttfMetric.getOccurences());
    }
}
