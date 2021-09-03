package jenkins.plugins.mttr;

import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.io.IOException;
import jenkins.plugins.util.ReadUtil;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuildMetric30DaysResultColumn extends ListViewColumn implements ResultColumn {

  @DataBoundConstructor
  public BuildMetric30DaysResultColumn() {
    // NOP
  }

  @Override
  public String getResult(Job job) throws IOException {
    return ReadUtil.getColumnResult(job, MetricsAction.MTTR_LAST_30_DAYS);
  }

  @Override
  public String getGraph(Job job) throws IOException {
    return null;
  }

  @Extension
  public static class DescriptorImpl extends ListViewColumnDescriptor {

    @Override
    public String getDisplayName() {
      return Messages.last30DaysBuildsColumnTitle();
    }

    @Override
    public boolean shownByDefault() {
      return false;
    }
  }
}
