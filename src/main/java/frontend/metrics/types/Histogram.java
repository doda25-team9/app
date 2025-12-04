package frontend.metrics.types;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Histogram {
    private final String metricName;
    private final List<Double> buckets;
    private final Map<String, Map<String, AtomicLong[]>> labelBucketCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, AtomicLong>> labelSums = new ConcurrentHashMap<>();
    private final String help;


    public Histogram(List<Double> buckets, String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
        this.buckets = buckets;
    }

    private AtomicLong[] createBucketArray() {
        AtomicLong[] bucketArray = new AtomicLong[buckets.size() + 1];
        for (int i = 0; i < bucketArray.length; i++) {
            bucketArray[i] = new AtomicLong(0);
        }
        return bucketArray;
    }

    public void record(String labelName, String labelValue, double value) {
        labelBucketCounts.putIfAbsent(labelName, new ConcurrentHashMap<>());
        labelSums.putIfAbsent(labelName, new ConcurrentHashMap<>());

        Map<String, AtomicLong[]> bucketsMap = labelBucketCounts.get(labelName);
        Map<String, AtomicLong> sumsMap = labelSums.get(labelName);

        bucketsMap.putIfAbsent(labelValue, createBucketArray());
        sumsMap.putIfAbsent(labelValue, new AtomicLong(0));

        AtomicLong[] bucketCounts = bucketsMap.get(labelValue);
        AtomicLong sum = sumsMap.get(labelValue);

        sum.addAndGet((long) (value * 1000));
        for (int i = 0; i < buckets.size(); i++) {
            if (value <= buckets.get(i)) {
                bucketCounts[i].incrementAndGet();
                return;
            }
        }
        bucketCounts[buckets.size()].incrementAndGet();
    }

    public long[] getBucketCounts(String labelName, String labelValue) {
        Map<String, AtomicLong[]> bucketsMap = labelBucketCounts.getOrDefault(labelName, Map.of());
        AtomicLong[] bucketCounts = bucketsMap.getOrDefault(labelValue, createBucketArray());

        long[] counts = new long[bucketCounts.length];
        for (int i = 0; i < bucketCounts.length; i++) {
            counts[i] = bucketCounts[i].get();
        }
        return counts;
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" histogram").append("\n");

        for (var labelEntry : labelBucketCounts.entrySet()) {
            String labelName = labelEntry.getKey();
            for (var valueEntry : labelEntry.getValue().entrySet()) {
                String labelValue = valueEntry.getKey();
                AtomicLong[] bucketCounts = valueEntry.getValue();
                AtomicLong sum = labelSums.get(labelName).get(labelValue);

                long cumulativeCount = 0;
                for (int i = 0; i < buckets.size(); i++) {
                    cumulativeCount += bucketCounts[i].get();
                    sb.append(metricName).append("_bucket{").append(labelName).append("=\"")
                            .append(labelValue).append("\",le=\"")
                            .append(buckets.get(i)).append("\"} ")
                            .append(cumulativeCount).append("\n");
                }
                cumulativeCount += bucketCounts[buckets.size()].get();
                sb.append(metricName).append("_bucket{").append(labelName).append("=\"")
                        .append(labelValue).append("\",le=\"+Inf\"} ")
                        .append(cumulativeCount).append("\n");

                sb.append(metricName).append("_sum{").append(labelName).append("=\"")
                        .append(labelValue).append("\"} ")
                        .append(sum.get()).append("\n");
                sb.append(metricName).append("_count{").append(labelName).append("=\"")
                        .append(labelValue).append("\"} ")
                        .append(cumulativeCount).append("\n");
            }
        }

        return sb.toString();
    }
}
