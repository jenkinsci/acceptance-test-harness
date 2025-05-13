package org.jenkinsci.test.acceptance.recorder.har;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.*;
import java.util.logging.Logger;
import org.openqa.selenium.bidi.network.BaseParameters;
import org.openqa.selenium.bidi.network.BeforeRequestSent;
import org.openqa.selenium.bidi.network.RequestData;
import org.openqa.selenium.bidi.network.ResponseData;
import org.openqa.selenium.bidi.network.ResponseDetails;

public class BiDiHARRecorder {
    
    private static final Logger LOGGER = Logger.getLogger(BiDiHARRecorder.class.getName());

    private static final String HAR_VERSION = "1.2";

    private final String browser;
    private final String version;

    private final Map<String, NetworkEntry> networkEntries = Collections.synchronizedMap(new HashMap<>());
    private final List<PageTiming> pageTimings = new ArrayList<>();
    private final List<String> topLevelContexts = new ArrayList<>();

    private boolean isInMicroseconds;
//    private boolean recording;
    private final String page;

    public BiDiHARRecorder(String page) {
        this(page, "unknwon browser", "unknown version");
    }

    public BiDiHARRecorder(@NonNull String page, @NonNull String browser, @NonNull String version) {
        this.page = page;
        this.browser = browser;
        this.version = version;
    }

//    public void startRecording(String initialPageUrl) {
//        if (recording) {
//            throw new IllegalStateException("HAR recording already started");
//        }
//        if (initialPageUrl != null) {
//            this.initialPageUrl = initialPageUrl;
//        }
//        recording = true;
//    }

//    public Map<String, Object> stopRecording(String lastPageUrl) {
//        if (!recording) {
//            throw new IllegalStateException("HAR recording not started");
//        }
//        log("Stop recording");
//        if (lastPageUrl != null) {
//            this.lastPageUrl = lastPageUrl;
//        }
//        Map<String, Object> harExport = exportAsHar();
//        networkEntries.clear();
//        pageTimings.clear();
//        recording = false;
//        return harExport;
//    }

//    public void recordEvent(String method, Map<String, Object> params) {
//        if (!recording) {
//            throw new IllegalStateException("HAR recording not started");
//        }
//        if (method == null || params == null) {
//            throw new IllegalArgumentException("recordEvent expects a method and params");
//        }
//        switch (method) {
//            case "network.beforeRequestSent":
//                onBeforeRequestSent(params);
//                break;
//            case "network.responseCompleted":
//                onResponseCompleted(params);
//                break;
//            case "browsingContext.contextCreated":
//                onContextCreatedEvent(params);
//                break;
//            case "browsingContext.domContentLoaded":
//                onBrowsingContextEvent("domContentLoaded", params);
//                break;
//            case "browsingContext.load":
//                onBrowsingContextEvent("load", params);
//                break;
//        }
//    }

//    private void onBeforeRequestSent(Map<String, Object> params) {
//        String id = params.get("request").toString() + "-" + params.get("redirectCount");
//        String url = params.get("url").toString();
//        if (isDataURL(url)) {
//            return;
//        }
//        networkEntries.add(new NetworkEntry(params.get("context").toString(), id, url, (int) params.get("redirectCount"), params));
//    }

  public void onBeforeRequestSent(BeforeRequestSent event) {
      final String id = computeId(event);
      final RequestData request = event.getRequest();
      String url = request.getUrl();
      if (isDataURL(url)) {
          return;
      }
      request.getTimings();
      networkEntries.put(id, new NetworkEntry(event.getBrowsingContextId(), id, url, event.getRedirectCount(), request));
    }

//    private void onBrowsingContextEvent(String type, Map<String, Object> params) {
//        String context = params.get("context").toString();
//        double timestamp = (double) params.get("timestamp");
//        String url = params.get("url").toString();
//        if (!topLevelContexts.contains(context)) {
//            log("Browsing context event \"" + type + "\" for url: " + shortUrl(url) + " discarded (not a top-level context)");
//            return;
//        }
//        double relativeTime = Double.POSITIVE_INFINITY;
//        double startedTime = -1;
//        if ("load".equals(type)) {
//            log("Event \"load\" for url: " + shortUrl(url) + " (context id: " + context + ")");
//            PageTiming firstTiming = findLast(pageTimings, timing -> timing.getContextId().equals(context));
//            if (firstTiming == null || !"domContentLoaded".equals(firstTiming.getType())) {
//                log("Warning: \"domContentLoaded\" event not found for \"load\" for url: " + shortUrl(url) + " (context id: " + context + ")");
//                return;
//            }
//            startedTime = firstTiming.getStartedTime();
//            url = firstTiming.getUrl();
//        } else {
//            log("Event \"domContentLoaded\" for url: " + shortUrl(url) + " (context id: " + context + ")");
//            NetworkEntry firstRequest = findLast(networkEntries, entry -> entry.getContextId().equals(context) && entry.getUrl().equals(url));
//            if (firstRequest == null) {
//                log("Warning: No request found for \"domContentLoaded\" using url: " + shortUrl(url) + " and context id: " + context);
//                return;
//            }
//            startedTime = normalizeTiming((double) firstRequest.getRequest().get("timings.requestTime"));
//        }
//        relativeTime = timestamp - startedTime;
//        pageTimings.add(new PageTiming(context, relativeTime, startedTime, timestamp, type, url));
//    }

//    private void onContextCreatedEvent(Map<String, Object> params) {
//        if (!params.containsKey("parent")) {
//            topLevelContexts.add(params.get("context").toString());
//        }
//    }

    public void onResponseCompleted(ResponseDetails event) {
        ResponseData response = event.getResponseData();
        String id = computeId(event);
        String url = event.getRequest().getUrl();
        if (isDataURL(url)) {
            return;
        }
        // find the matching entry
        
        NetworkEntry entry = networkEntries.get(id);
        if (entry != null) {
            // replace the request as the timing details are now populated
            entry.setRequest(event.getRequest());
            entry.setResponse(event.getResponseData());
        } else {
            // TODO we have all the data to put this in the HAR in any case!
            LOGGER.warning( () -> "No matching entry found for url: " + url + " (id: " + id + ")");
        }
    }

    private static final String computeId(BaseParameters event) {
        // https://w3c.github.io/webdriver-bidi/#type-network-Request
        // Each network request has an associated request id, which is a string uniquely identifying that request. 
        // The identifier for a request resulting from a redirect matches that of the request that initiated it.

        // so include the redirect count to make this unique
        return event.getRequest().getRequestId() + "-" + event.getRedirectCount();
    }

    private Map<String, Object> exportAsHar() {
        Map<String, Object> recording = new HashMap<>();
        Map<String, Object> log = new HashMap<>();
        log.put("version", HAR_VERSION);
        log.put("creator", Map.of("name", "Jenkins ATH", "version", version));
        log.put("browser", Map.of("name", browser, "version", version));
        //log.put("pages", new ArrayList<>());
        // todo Sort the values and convert to JSON correctly!
        log.put("entries", networkEntries.values());
        recording.put("log", log);
        return recording;
    }

    private boolean isDataURL(String url) {
        // do not log data URLs
        // we start with them and we display them for proxy issues etc...
        return url.startsWith("data:");
    }

    private double normalizeTiming(double timing) {
        if (isInMicroseconds) {
            return timing / 1000;
        }
        return timing;
    }


    private <T> T findLast(List<T> list, java.util.function.Predicate<T> predicate) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (predicate.test(list.get(i))) {
                return list.get(i);
            }
        }
        return null;
    }

    private static class NetworkEntry {
        private final String contextId;
        private final String id;
        private final String url;
        private final long redirectCount;
        private RequestData request;
        private ResponseData response;

        public NetworkEntry(String contextId, String id, String url, long redirectCount, RequestData request) {
            this.contextId = contextId;
            this.id = id;
            this.url = url;
            this.redirectCount = redirectCount;
            this.request = request;
        }

        public String getContextId() {
            return contextId;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public RequestData getRequest() {
            return request;
        }

        public void setRequest(RequestData request) {
            this.request = request;
        }

        public void setResponse(ResponseData response) {
            this.response = response;
        }
    }

    private static class PageTiming {
        private final String contextId;
        private final double relativeTime;
        private final double startedTime;
        private final double timestamp;
        private final String type;
        private final String url;

        public PageTiming(String contextId, double relativeTime, double startedTime, double timestamp, String type, String url) {
            this.contextId = contextId;
            this.relativeTime = relativeTime;
            this.startedTime = startedTime;
            this.timestamp = timestamp;
            this.type = type;
            this.url = url;
        }

        public String getContextId() {
            return contextId;
        }

        public double getStartedTime() {
            return startedTime;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }
    }
}