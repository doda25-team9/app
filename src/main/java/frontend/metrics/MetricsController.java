package frontend.metrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private final MetricsRegistry metricsRegistry;

    public MetricsController(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @GetMapping("/metrics")
    public String metrics() {
        StringBuilder sb = new StringBuilder();

        metricsRegistry.counters.values().forEach(counter -> {
            sb.append(counter.export());
        });
        metricsRegistry.gauges.values().forEach(gauge -> {
            sb.append(gauge.export());
        });
        metricsRegistry.histograms.values().forEach(histogram -> {
            sb.append(histogram.export());
        });

        sb.append("# EOF\n");
        return sb.toString();
    }

}