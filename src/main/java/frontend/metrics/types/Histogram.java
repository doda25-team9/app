package frontend.metrics.types;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Histogram {
    private final String metricName;
    private final List<Double> buckets;
    private final AtomicLong[] bucketCounts;
    private final AtomicLong sum = new AtomicLong(0);
    private final String help;


    public Histogram(List<Double> buckets, String metricName, String help) {
        this.help = help;
        this.metricName = metricName;
        this.buckets = buckets;
        this.bucketCounts = new AtomicLong[buckets.size() + 1];
        for (int i = 0; i < bucketCounts.length; i++) {
            bucketCounts[i] = new AtomicLong(0);
        }
    }

    public void record(double value) {
        sum.addAndGet((long) (value * 1000));
        for (int i = 0; i < buckets.size(); i++) {
            if (value <= buckets.get(i)) {
                bucketCounts[i].incrementAndGet();

                return;
            }
        }
        bucketCounts[buckets.size()].incrementAndGet();
    }

    public long[] getBucketCounts() {
        long[] counts = new long[bucketCounts.length];
        for (int i = 0; i < bucketCounts.length; i++) {
            counts[i] = bucketCounts[i].get();
        }
        return counts;
    }

    public List<Double> getBuckets() {
        return buckets;
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP ").append(metricName).append(" ").append(help).append("\n");
        sb.append("# TYPE ").append(metricName).append(" histogram").append("\n");

        long cumulativeCount = 0;
        for (int i = 0; i < buckets.size(); i++) {
            cumulativeCount += bucketCounts[i].get();
            sb.append(metricName).append("_bucket{le=\"")
                    .append(buckets.get(i)).append("\"} ")
                    .append(cumulativeCount).append("\n");
        }
        cumulativeCount += bucketCounts[buckets.size()].get();
        sb.append(metricName).append("_bucket{le=\"+Inf\"} ")
                .append(cumulativeCount).append("\n");

        sb.append(metricName).append("_sum ").append(sum.get()).append("\n");
        sb.append(metricName).append("_count ").append(cumulativeCount).append("\n");

        return sb.toString();
    }
}
