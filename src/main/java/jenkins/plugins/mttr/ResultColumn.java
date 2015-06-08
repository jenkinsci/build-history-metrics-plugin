package jenkins.plugins.mttr;

import hudson.model.Job;

import java.io.IOException;

public interface ResultColumn {
    String getResult(Job job) throws IOException;
    String getGraph(Job job) throws IOException;
}
