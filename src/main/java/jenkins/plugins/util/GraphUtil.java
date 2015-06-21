package jenkins.plugins.util;

import jenkins.plugins.model.BuildMessage;
import jenkins.plugins.model.StandardDeviationMetric;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;

public class GraphUtil {
    public static JFreeChart generateStdDevGraph(String title, List<BuildMessage> buildMessages) {
        String chartTitle = title;
        String xAxisLabel = "X";
        String yAxisLabel = "Y";

        XYDataset dataset = createDataset(buildMessages);

        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
                xAxisLabel, yAxisLabel, dataset, PlotOrientation.HORIZONTAL, false, false, false);

        return chart;
    }

    private static XYDataset createDataset(List<BuildMessage> buildMessages) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Object 1");

        //Need to optimise this, it's O(n^2)
        for(int i=0; i<buildMessages.size(); i++){

            List<BuildMessage> sublist = buildMessages.subList(i, buildMessages.size() - 1);
            System.out.println(sublist.get(0));
            StandardDeviationMetric metric = new StandardDeviationMetric("Graph", sublist);
            series1.add(metric.calculateMetric(), i);
        }

        dataset.addSeries(series1);


        return dataset;
    }

}
