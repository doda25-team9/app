package frontend.metrics.types;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {
    private final AtomicLong count = new AtomicLong(0);
    private final List<String> labels;
    private final String metricName;
    private final String help;

    public Counter(List<String> labels, String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
        this.labels = labels;
    }

    public void increment() {
        count.incrementAndGet();
    }

    public long getCount() {
        return count.get();
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" counter").append("\n");

        sb.append(metricName).append(" ").append(getCount()).append("\n");
        return sb.toString();
    }
}
