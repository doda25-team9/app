package frontend.ctrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import frontend.metrics.MetricsRegistry;
import frontend.metrics.types.Counter;
import frontend.metrics.types.Gauge;
import frontend.metrics.types.Histogram;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import frontend.data.Sms;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/sms")
public class FrontendController {

    private String modelHost;
    private RestTemplateBuilder rest;
    private final MetricsRegistry metricsRegistry;

    public FrontendController(RestTemplateBuilder rest, Environment env, MetricsRegistry metricsRegistry) {
        this.rest = rest;
        this.modelHost = env.getProperty("MODEL_HOST");
        this.metricsRegistry = metricsRegistry;
        assertModelHost();
        initializeMetrics();
    }

    private void assertModelHost() {
        if (modelHost == null || modelHost.strip().isEmpty()) {
            System.err.println("ERROR: ENV variable MODEL_HOST is null or empty");
            System.exit(1);
        }
        modelHost = modelHost.strip();
        if (modelHost.indexOf("://") == -1) {
            var m = "ERROR: ENV variable MODEL_HOST is missing protocol, like \"http://...\" (was: \"%s\")\n";
            System.err.printf(m, modelHost);
            System.exit(1);
        } else {
            System.out.printf("Working with MODEL_HOST=\"%s\"\n", modelHost);
        }
    }

    private void initializeMetrics() {
        metricsRegistry.addCounter("sms_requests_total",
                new Counter("sms_requests_total", "Total number of SMS prediction requests received"));
        metricsRegistry.addGauge("active_users",
                new Gauge("active_users", "Current number of active users"));
        metricsRegistry.addHistogram("request_duration", new Histogram(List.of(0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 2.0, 5.0),
                "request_duration", "Histogram of request durations in seconds"));
        metricsRegistry.addCounter("predictions_result_total",
                new Counter( "predictions_result_total", "Total number of SMS predictions with result"));
        metricsRegistry.addHistogram("sms_length", new Histogram(List.of(10.0, 20.0, 30.0, 40.0, 50.0, 100.0, 200.0, 500.0),
                "sms_length", "Histogram of SMS lengths in characters"));
        metricsRegistry.addGauge("last_request_duration_ms",
                new Gauge("last_request_duration_ms", "Duration of the last request in milliseconds"));
        metricsRegistry.addGauge("last_sms_length_characters",
                new Gauge("last_sms_length_characters", "Length of the last SMS in characters"));
    }


    @GetMapping("")
    public String redirectToSlash(HttpServletRequest request) {
        // relative REST requests in JS will end up on / and not on /sms
        return "redirect:" + request.getRequestURI() + "/";
    }

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("hostname", modelHost);
        return "sms/index";
    }

    @PostMapping({ "", "/" })
    @ResponseBody
    public Sms predict(@RequestBody Sms sms) {
        System.out.printf("Requesting prediction for \"%s\" ...\n", sms.sms);
        recordRequestMetrics(sms);

        long startTime = System.currentTimeMillis();
        sms.result = getPrediction(sms);
        long durationMs = System.currentTimeMillis() - startTime;

        recordPredictionMetrics(durationMs);
        recordResultMetrics(sms);

        System.out.printf("Prediction: %s\n", sms.result);
        return sms;
    }

    private String getPrediction(Sms sms) {
        try {
            var url = new URI(modelHost + "/predict");
            var c = rest.build().postForEntity(url, sms, Sms.class);
            return c.getBody().result.trim();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void recordRequestMetrics(Sms sms) {
        metricsRegistry.getCounter("sms_requests_total").increment("endpoint", "/sms");
        metricsRegistry.getGauge("active_users").increment("endpoint", "/sms");
        metricsRegistry.getGauge("last_sms_length_characters").set("endpoint", "/sms", sms.sms.length());

    }

    private void recordPredictionMetrics(long durationMs) {
        metricsRegistry.getHistogram("request_duration").record("endpoint", "/sms", durationMs / 1000.0);
        metricsRegistry.getGauge("last_request_duration_ms").set("endpoint", "/sms", (int) durationMs);
    }

    private void recordResultMetrics(Sms sms) {
        metricsRegistry.getGauge("active_users").decrement("endpoint", "/sms");

        if (sms.result.equalsIgnoreCase("spam")) {
            metricsRegistry.getCounter("predictions_result_total").increment("result", "spam");
            metricsRegistry.getHistogram("sms_length").record("result", "spam", sms.sms.length());
        } else if (sms.result.equalsIgnoreCase("ham")) {
            metricsRegistry.getCounter("predictions_result_total").increment("result", "ham");
            metricsRegistry.getHistogram("sms_length").record("result", "ham", sms.sms.length());
        }
    }
}