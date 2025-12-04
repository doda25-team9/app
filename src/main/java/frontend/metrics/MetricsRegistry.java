package frontend.metrics;

import frontend.metrics.types.Counter;
import frontend.metrics.types.Gauge;
import frontend.metrics.types.Histogram;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricsRegistry {
    public final Map<String, Counter> counters = new ConcurrentHashMap<>();
    public final Map<String, Gauge> gauges = new ConcurrentHashMap<>();
    public final Map<String, Histogram> histograms = new ConcurrentHashMap<>();


    public Counter getCounter(String name) {
        return counters.get(name);
    }
    public Gauge getGauge(String name) {
        return gauges.get(name);
    }
    public Histogram getHistogram(String name) {
        return histograms.get(name);
    }

    public void addCounter(String name, Counter counter) {
        counters.put(name, counter);
    }
    public void addGauge(String name, Gauge gauge) {
        gauges.put(name, gauge);
    }
    public void addHistogram(String name, Histogram histogram) {
        histograms.put(name, histogram);
    }

}