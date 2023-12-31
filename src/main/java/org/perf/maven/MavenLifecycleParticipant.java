package org.perf.maven;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.codehaus.plexus.logging.Logger;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "latency")
public class MavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final long NS_IN_SEC = 1_000_000_000L;
    @Inject
    private SubProjectListener listener;

    @Requirement
    private Logger logger;

    @Override
    public void afterSessionEnd(MavenSession session) {
        logger.info("Maven latency plugin");
        var latencyPerProject = listener.getLatencyPerProject();
        if (latencyPerProject.size() == 0) {
            logger.info("No subprojects detected");
            return;
        }
        var latencies = new ArrayList<>(latencyPerProject.values());
        latencies.sort(Comparator.comparing(Latency::getStartNs));
        long startNs = latencies.get(0).getStartNs();
        long endNs = getEndTsNs(latencies);
        long durationNs = endNs - startNs;
        double multiplier = (double) 200 / TimeUnit.NANOSECONDS.toSeconds(durationNs == 0 ? 1 : durationNs);

        List<LatencyPoints> pointsPerLine = new ArrayList<>();
        int maxPoints = 0;
        for (var latency : latencies) {
            double startSec = (double) (latency.getStartNs() - startNs) / NS_IN_SEC;
            double endSec = (double) (latency.getEndNs() - latency.getStartNs()) / NS_IN_SEC;
            int start = (int) Math.ceil(startSec * multiplier);
            int end = (int) Math.ceil(endSec * multiplier);
            int points = start + end;
            if (points > maxPoints) {
                maxPoints = points;
            }
            pointsPerLine.add(new LatencyPoints(start, end , latency));
        }

        for (LatencyPoints latency: pointsPerLine) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            printRepeat(latency.start(), '.', sb);
            printRepeat(latency.end(), '|', sb);
            printRepeat(maxPoints - (latency.start() + latency.end()), '.', sb);
            sb.append(']');
            logger.info(toString(startNs, latency.latency()) + "\n" + sb.toString());
        }

        logger.info("Took: " + Duration.ofNanos(durationNs).toString());
    }

    private void printRepeat(int num, char symbol, StringBuilder sb) {
        for (int i = 0; i < num; ++i) {
            sb.append(symbol);
        }
    }

    private long getEndTsNs(List<Latency> latencies) {
        return latencies.stream().map(Latency::getEndNs).max(Comparator.naturalOrder()).get();
    }

    private String toString(long startBuildNs, Latency latency) {
        return "Latency {" +
                "id=" + latency.getProjectId() +
                ", thread=" + latency.getThread() +
                ", start=" + Duration.ofNanos(latency.getStartNs() - startBuildNs) +
                ", latency" + Duration.ofNanos(latency.getEndNs() - latency.getStartNs()) +
                "}";
    }
}
