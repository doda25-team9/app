package frontend.metrics.types;

import java.util.concurrent.atomic.AtomicInteger;

public class Gauge {
    private final AtomicInteger value = new AtomicInteger(0);
    private final String label;
    private final String metricName;
    private final String help;

    public Gauge(String label, String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
        this.label = label;
    }

    public void increment() {
        value.incrementAndGet();
    }
    public void decrement() {
        value.decrementAndGet();
    }
    public int getValue() {
        return value.get();
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" gauge").append("\n");

        sb.append(metricName).append(" ").append(getValue()).append("\n");
        return sb.toString();
    }
}
