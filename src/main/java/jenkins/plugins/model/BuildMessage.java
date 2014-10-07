package jenkins.plugins.model;

public class BuildMessage {
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
}
