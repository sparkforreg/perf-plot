package org.perf.maven;

public class Latency {
    private final String projectId;
    private final long startNs;
    private final long thread;
    private long endNs;

    public Latency(String projectId, long thread, long startNs) {
        this.projectId = projectId;
        this.startNs = startNs;
        this.thread = thread;
    }

    public void setEndNs(long endNs) {
        this.endNs = endNs;
    }

    public String getProjectId() {
        return projectId;
    }

    public long getStartNs() {
        return startNs;
    }

    public long getEndNs() {
        return endNs;
    }

    public long getThread() {
        return thread;
    }
}
