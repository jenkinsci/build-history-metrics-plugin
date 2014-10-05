package jenkins.plugins.mttr;

import hudson.model.Job;

import java.io.IOException;

public interface ResultColumn {
    public String getResult(Job job) throws IOException;
}
