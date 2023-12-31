package org.perf.maven;

import org.apache.maven.execution.ProjectExecutionEvent;
import org.apache.maven.execution.ProjectExecutionListener;
import org.codehaus.plexus.component.annotations.Component;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Component(role = ProjectExecutionListener.class, hint = "latency")
@Singleton
public class SubProjectListener implements ProjectExecutionListener {

    public Map<String, Latency> latencyPerProject = new HashMap<>();

    @Override
    public void beforeProjectExecution(ProjectExecutionEvent projectExecutionEvent) {
        String id = projectExecutionEvent.getProject().getId();
        latencyPerProject.put(id, new Latency(id, Thread.currentThread().getId(), System.nanoTime()));

    }

    @Override
    public void beforeProjectLifecycleExecution(ProjectExecutionEvent projectExecutionEvent) {

    }

    @Override
    public void afterProjectExecutionSuccess(ProjectExecutionEvent projectExecutionEvent) {
        String id = projectExecutionEvent.getProject().getId();
        latencyPerProject.get(id).setEndNs(System.nanoTime());
    }

    @Override
    public void afterProjectExecutionFailure(ProjectExecutionEvent projectExecutionEvent) {
        String id = projectExecutionEvent.getProject().getId();
        latencyPerProject.get(id).setEndNs(System.nanoTime());
    }

    public Map<String, Latency> getLatencyPerProject() {
        return latencyPerProject;
    }
}
