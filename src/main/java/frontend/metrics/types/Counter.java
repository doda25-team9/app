package frontend.metrics.types;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {
    private final String metricName;
    private final String help;
    private final Map<String, Map<String, AtomicLong>> labelCounts = new ConcurrentHashMap<>();


    public Counter(String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
    }

    public void increment(String labelName, String labelValue) {
        labelCounts.putIfAbsent(labelName, new ConcurrentHashMap<>());
        Map<String, AtomicLong> counts = labelCounts.get(labelName);
        counts.putIfAbsent(labelValue, new AtomicLong(0));
        counts.get(labelValue).incrementAndGet();
    }

    public long getCount(String labelName, String labelValue) {
        return labelCounts.getOrDefault(labelName, Map.of())
                .getOrDefault(labelValue, new AtomicLong(0))
                .get();
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" counter").append("\n");

        for (var labelEntry : labelCounts.entrySet()) {
            String labelName = labelEntry.getKey();
            for (var valueEntry : labelEntry.getValue().entrySet()) {
                String labelValue = valueEntry.getKey();
                long count = valueEntry.getValue().get();
                sb.append(metricName)
                  .append("{").append(labelName).append("=\"").append(labelValue).append("\"} ")
                  .append(count).append("\n");
            }
        }
        return sb.toString();
    }
}
