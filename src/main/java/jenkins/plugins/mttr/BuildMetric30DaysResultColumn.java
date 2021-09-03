package jenkins.plugins.mttr;

import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.io.IOException;
import jenkins.plugins.util.ReadUtil;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Class for Last 30 Days Builds Results Columns.
 *
 * @author mcgin
 */
public class BuildMetric30DaysResultColumn extends ListViewColumn
    implements ResultColumn {

  /** The Constructor for BuildMetric30DaysResultColumn. */
  @DataBoundConstructor
  public BuildMetric30DaysResultColumn() {
    // NOP
  }

  /** {@inheritDoc} */
  @Override
  public final String getResult(final Job job) throws IOException {
    return ReadUtil.getColumnResult(job, MetricsAction.MTTR_LAST_30_DAYS);
  }

  /** {@inheritDoc} */
  @Override
  public final String getGraph(final Job job) throws IOException {
    return null;
  }

  /** The Class that allows the Column to display in List Views. */
  @Extension
  public static class DescriptorImpl extends ListViewColumnDescriptor {
    /** {@inheritDoc} */
    @Override
    public final String getDisplayName() {
      return Messages.last30DaysBuildsColumnTitle();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean shownByDefault() {
      return false;
    }
  }
}
