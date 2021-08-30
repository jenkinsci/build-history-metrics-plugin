package jenkins.plugins.model;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class StandardDeviationMetric implements AggregateBuildMetric {

  private final String name;
  private final long metric;
  private final int occurences;

  public StandardDeviationMetric(String name, List<BuildMessage> messages) {
    this.name = name;
    this.occurences = messages.size();

    SummaryStatistics statistics = new SummaryStatistics();
    for (BuildMessage m : messages) {
      statistics.addValue(m.getDuration());
    }
    metric = (long) statistics.getStandardDeviation();
  }

  @Override
  public int getOccurences() {
    return occurences;
  }

  @Override
  public long calculateMetric() {
    return metric;
  }

  @Override
  public String getName() {
    return name;
  }
}
