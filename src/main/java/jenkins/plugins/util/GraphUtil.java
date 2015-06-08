package jenkins.plugins.util;

import jenkins.plugins.model.BuildMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

public class GraphUtil {
    public static JFreeChart generateGraph(String title, List<BuildMessage> buildMessages) {
        String chartTitle = title;
        String xAxisLabel = "X";
        String yAxisLabel = "Y";

        XYDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
                xAxisLabel, yAxisLabel, dataset, PlotOrientation.HORIZONTAL, false, false, false);

        return chart;
    }

    private static XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Object 1");

        series1.add(1.0, 2.0);
        series1.add(2.0, 3.0);
        series1.add(3.0, 2.5);
        series1.add(3.5, 2.8);
        series1.add(4.2, 6.0);

        dataset.addSeries(series1);

        return dataset;
    }
}
