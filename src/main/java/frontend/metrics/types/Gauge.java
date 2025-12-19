package frontend.metrics.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Gauge {
    private final String metricName;
    private final String help;
    private final Map<String, Map<String, AtomicInteger>> labelValues = new ConcurrentHashMap<>();

    public Gauge(String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
    }

    public void increment(String labelName, String labelValue) {
        labelValues.putIfAbsent(labelName, new ConcurrentHashMap<>());
        Map<String, AtomicInteger> values = labelValues.get(labelName);
        values.putIfAbsent(labelValue, new AtomicInteger(0));
        AtomicInteger value = values.get(labelValue);
        value.incrementAndGet();
    }
    public void decrement(String labelName, String labelValue) {
        labelValues.putIfAbsent(labelName, new ConcurrentHashMap<>());
        Map<String, AtomicInteger> values = labelValues.get(labelName);
        values.putIfAbsent(labelValue, new AtomicInteger(0));
        AtomicInteger value = values.get(labelValue);
        value.decrementAndGet();
    }
    public int getValue(String labelName, String labelValue) {
        return labelValues.getOrDefault(labelName, Map.of())
                .getOrDefault(labelValue, new AtomicInteger(0))
                .get();
    }

    public void set(String labelName, String labelValue, int newValue) {
        labelValues.putIfAbsent(labelName, new ConcurrentHashMap<>());
        Map<String, AtomicInteger> values = labelValues.get(labelName);
        values.putIfAbsent(labelValue, new AtomicInteger(0));
        values.get(labelValue).set(newValue);
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" gauge").append("\n");

        for (var labelEntry : labelValues.entrySet()) {
            String labelName = labelEntry.getKey();
            for (var valueEntry : labelEntry.getValue().entrySet()) {
                String labelValue = valueEntry.getKey();
                int value = valueEntry.getValue().get();
                sb.append(metricName)
                  .append("{").append(labelName).append("=\"").append(labelValue).append("\"} ")
                  .append(value).append("\n");
            }
        }
        return sb.toString();
    }
}
