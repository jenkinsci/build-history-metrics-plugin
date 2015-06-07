package jenkins.plugins.mttr;

import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import jenkins.plugins.util.ReadUtil;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class BuildMetricAllTimeResultColumn extends ListViewColumn  implements ResultColumn {

    @DataBoundConstructor
    public BuildMetricAllTimeResultColumn() {
    }

    @Override
    public String getResult(Job job) throws IOException {
        return ReadUtil.getColumnResult(job, MetricsAction.MTTR_ALL_BUILDS);
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        public DescriptorImpl() {
        }

        public String getDisplayName() {
            return Messages.allBuildsColumnTitle();
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}
