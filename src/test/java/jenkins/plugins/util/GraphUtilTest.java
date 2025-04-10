package jenkins.plugins.util;

import hudson.model.Result;
import jenkins.plugins.model.BuildMessage;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphUtilTest {

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
    void should_return_graph_data() {
        JFreeChart chart = GraphUtil.generateStdDevGraph("test",
                Arrays.asList(FIRST_BUILD, SECOND_BUILD, THIRD_BUILD, FOURTH_BUILD, FIFTH_BUILD, SIXTH_BUILD, SEVENTH_BUILD, EIGHTH_BUILD));
        XYDataset dataset = chart.getXYPlot().getDataset();
        assertEquals(8, dataset.getItemCount(0));
        assertEquals(0d, dataset.getX(0, 0));
        assertEquals(707d, dataset.getX(0, 1));
        assertEquals(1000d, dataset.getX(0, 2));
        assertEquals(1290d, dataset.getX(0, 3));
        assertEquals(1581d, dataset.getX(0, 4));
        assertEquals(1870d, dataset.getX(0, 5));
        assertEquals(2160d, dataset.getX(0, 6));
        assertEquals(2449d, dataset.getX(0, 7));
    }

}
