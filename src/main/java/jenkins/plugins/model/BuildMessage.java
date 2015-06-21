package jenkins.plugins.model;

import java.util.Comparator;

public class BuildMessage implements Comparator<BuildMessage>, Comparable<BuildMessage> {
    private long startTime;
    private String result;
    private long duration;
    private long buildNumber;

    public BuildMessage(long buildNumber, long startTime, long duration, String result) {
        this.startTime = startTime;
        this.result = result;
        this.duration = duration;
        this.buildNumber = buildNumber;
    }

    public long getBuildNumber()  { return buildNumber; }

    public long getStartTime() {
        return startTime;
    }

    public String getResult() {
        return result;
    }

    public long getDuration() { return duration; }

    @Override
    public int compare(BuildMessage message1, BuildMessage message2) {
        return (int) (message1.getBuildNumber() - message2.getBuildNumber());
    }

    @Override
    public int compareTo(BuildMessage message) {
        return compare(this, message);
    }

    @Override
    public String toString() {
        return buildNumber + "\t" + result;
    }
}
