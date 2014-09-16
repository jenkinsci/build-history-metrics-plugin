package jenkins.plugins.model;

public class BuildMessage {
    private long startTime;
    private String result;

    public static final String BUILD_SUCCESS = "SUCCESS";
    public static final String BUILD_FAILED = "FAILED";

    public BuildMessage(long startTime, String result) {
        this.startTime = startTime;
        this.result = result;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getResult() {
        return result;
    }
}
