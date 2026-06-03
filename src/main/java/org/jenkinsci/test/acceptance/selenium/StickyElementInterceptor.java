package org.jenkinsci.test.acceptance.selenium;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.BeforeRequestSent;
import org.openqa.selenium.bidi.network.BytesValue;
import org.openqa.selenium.bidi.network.ContinueRequestParameters;
import org.openqa.selenium.bidi.network.Header;
import org.openqa.selenium.bidi.network.InterceptPhase;
import org.openqa.selenium.bidi.network.ProvideResponseParameters;
import org.openqa.selenium.bidi.network.RequestData;

/**
 * Request interceptor that updates Jenkins CSS styles that use sticky elements.
 * Jenkins' use of sticky elements often either prevents an element from being able to be scrolled into view, or can cause the click to go to a different element.
 * <p>
 * Especially in the configuration page, the floating DIVs at the top and
 * the bottom of the pages can interfere with WebDriver trying to click
 * the elements underneath it.
 * <p>
 * Trying to interact with an element when it's below another element causes the following error:
 *
 * <pre>
 * {@code
 * Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 227.711 sec <<< FAILURE! - in plugins.AntPluginTest
 * autoInstallAnt(plugins.AntPluginTest)  Time elapsed: 73.932 sec  <<< ERROR!
 * org.openqa.selenium.WebDriverException: unknown error: Element is not clickable at point (506, 967). Other element would receive the click: <div class="bottom-sticker-inner">...</div>
 *   (Session info: chrome=34.0.1847.116)
 *   (Driver info: chromedriver=2.10.267518,platform=Linux 3.13.0-24-generic x86_64) (WARNING: The server did not provide any stacktrace information)
 * Command duration or timeout: 61 milliseconds
 * Build info: version: '2.40.0', revision: '4c5c0568b004f67810ee41c459549aa4b09c651e', time: '2014-02-19 11:13:01'
 * System info: host: 'jglick-t520', ip: '127.0.1.1', os.name: 'Linux', os.arch: 'amd64', os.version: '3.13.0-24-generic', java.version: '1.7.0_55'
 * Session ID: 412a7b3ab0eb823da2cddd156e8d122c
 * Driver info: org.openqa.selenium.chrome.ChromeDriver
 * Capabilities [{platform=LINUX, acceptSslCerts=true, javascriptEnabled=true, browserName=chrome, chrome={userDataDir=/tmp/.com.google.Chrome.3Xt0hp}, rotatable=false, locationContextEnabled=true, version=34.0.1847.116, takesHeapSnapshot=true, cssSelectorsEnabled=true, databaseEnabled=false, handlesAlerts=true, browserConnectionEnabled=false, nativeEvents=true, webStorageEnabled=true, applicationCacheEnabled=false, takesScreenshot=true}]
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
 * 	at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
 * 	at org.openqa.selenium.remote.ErrorHandler.createThrowable(ErrorHandler.java:193)
 * 	at org.openqa.selenium.remote.ErrorHandler.throwIfResponseFailed(ErrorHandler.java:145)
 * 	at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:573)
 * 	at org.openqa.selenium.remote.RemoteWebElement.execute(RemoteWebElement.java:268)
 * 	at org.openqa.selenium.remote.RemoteWebElement.click(RemoteWebElement.java:79)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 * 	at java.lang.reflect.Method.invoke(Method.java:606)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement$1.invoke(EventFiringWebDriver.java:331)
 * 	at com.sun.proxy.$Proxy33.click(Unknown Source)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement.click(EventFiringWebDriver.java:344)
 * 	at org.jenkinsci.test.acceptance.po.CapybaraPortingLayer.clickButton(CapybaraPortingLayer.java:66)
 * 	at org.jenkinsci.test.acceptance.po.JenkinsConfig.addTool(JenkinsConfig.java:35)
 * 	at org.jenkinsci.test.acceptance.plugins.ant.AntInstallation.install(AntInstallation.java:37)
 * 	at plugins.AntPluginTest.autoInstallAnt(AntPluginTest.java:76)
 * 	}
 * </pre>
 *
 * <p>
 * This workaround simply replaces any occurrence of {@code position:\s*sticky} with {@code position: relative}
 */
public class StickyElementInterceptor implements AutoCloseable {

    private final Logger LOGGER = Logger.getLogger(StickyElementInterceptor.class.getName());

    private final Network network;
    private final String interceptId;
    private final HttpClient client = HttpClient.newHttpClient();

    public StickyElementInterceptor(WebDriver driver) {
        network = new Network(driver);
        AddInterceptParameters p = new AddInterceptParameters(InterceptPhase.BEFORE_REQUEST_SENT);
        interceptId = network.addIntercept(p);
        network.onBeforeRequestSent(this::handleRequest);
    }

    private void handleRequest(BeforeRequestSent request) {
        final String requestId = request.getRequest().getRequestId();
        final RequestData requestData = request.getRequest();
        String requestUrl = requestData.getUrl();
        if (request.isBlocked() && request.getIntercepts().contains(interceptId)) {
            if (requestUrl.endsWith(".css") && requestUrl.contains("/static/") && requestUrl.startsWith("http")) {
                // smells like a static jenkins css file...
                // extract a potential cookie for authentication
                String cookie = extractCookieHeader(requestData);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl));
                if (cookie != null) {
                    requestBuilder.header("Cookie", cookie);
                }
                HttpRequest clientRequest = requestBuilder.build();
                // Fetch and patch asynchronously so we do not block the BiDi event-dispatch thread on a
                // synchronous HTTP round-trip (which would delay processing of subsequent network events).
                client.sendAsync(clientRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))
                        .thenAccept(clientResponse -> providePatchedCss(requestId, requestUrl, clientResponse))
                        .exceptionally(t -> {
                            // The request stays blocked and will fail with a page-load timeout. A failure here
                            // likely indicates a bug either in this interceptor or in Jenkins, so surface it.
                            LOGGER.log(
                                    Level.WARNING,
                                    t,
                                    () -> "Could not retrieve CSS from Jenkins at URL: " + requestUrl);
                            return null;
                        });
            } else {
                if (requestUrl.startsWith("data:")) {
                    // XXX should not be intercepted
                    return;
                }
                try {
                    network.continueRequest(new ContinueRequestParameters(requestId));
                } catch (Exception ignored) {
                    if (requestUrl.endsWith("/apple-touch-icon.png")
                            || requestUrl.endsWith("/favicon.svg")
                            || requestUrl.endsWith("/favicon-dark.ico")) {
                        // these often cause errors so ignore them.
                        // it does not impact Jenkins or the browser based testing.
                        return;
                    }
                    // XXX exceptions here seem unusual, but they regularly occur with no side effect
                    // all of the form `"no such request","message":"Blocked request with id <UUID> not found`
                    // but log them all the same incase we have one that does have an impact.
                    LOGGER.log(Level.WARNING, ignored, () -> "failed to send response for " + requestUrl);
                }
            }
        }
    }

    /**
     * Patches the CSS body of the given response (removing sticky positioning) and provides it to the browser
     * in place of the originally intercepted request.
     */
    private void providePatchedCss(String requestId, String requestUrl, HttpResponse<String> clientResponse) {
        int responseCode = clientResponse.statusCode();
        if (responseCode != 200) {
            // if we do not have a 200 response then we would not have CSS to patch.
            // We could just allow the browser to make the request, but there is likely a bug
            // either here or in Jenkins, and so the exception would make the call to action obvious
            throw new IllegalStateException("Unexpected HTTP " + responseCode + " fetching CSS at " + requestUrl);
        }
        String css = clientResponse.body();
        String expires = clientResponse.headers().firstValue("Expires").orElse(null);
        String lastModified =
                clientResponse.headers().firstValue("Last-Modified").orElse(null);

        String patchedCSS = removeStickyPositioning(css);
        LOGGER.fine("filtering URL: " + requestUrl);
        ProvideResponseParameters responseParams = new ProvideResponseParameters(requestId);
        responseParams.body(new BytesValue(BytesValue.Type.STRING, patchedCSS));

        responseParams.statusCode(200);
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new BytesValue(BytesValue.Type.STRING, "text/css")));
        if (lastModified != null) {
            headers.add(new Header("Last-Modified", new BytesValue(BytesValue.Type.STRING, lastModified)));
        }
        if (expires != null) {
            headers.add(new Header("Expires", new BytesValue(BytesValue.Type.STRING, expires)));
        }
        responseParams.headers(headers);
        network.provideResponse(responseParams);
    }

    /**
     *
     * @param requestData
     * @return the raw cookie or {@code null} if the request was cookie-less.
     */
    @CheckForNull
    private String extractCookieHeader(RequestData requestData) {
        for (Header h : requestData.getHeaders()) {
            if ("Cookie".equalsIgnoreCase(h.getName())) {
                BytesValue v = h.getValue();

                switch (v.getType()) {
                    case BytesValue.Type.STRING:
                        return v.getValue();
                    default:
                        throw new IllegalStateException("expected a String header for cookie, but got: " + v.getType());
                }
            }
        }
        return null;
    }

    /**
     * Update the CSS to make all {@code sticky} CSS positions {@code relative}
     */
    private String removeStickyPositioning(String css) {
        return css.replaceAll("position:\\s*sticky", "position: relative");
    }

    @Override
    public void close() throws Exception {
        try (network;
                client) {
            // ensure the resources are closed
        }
    }
}
