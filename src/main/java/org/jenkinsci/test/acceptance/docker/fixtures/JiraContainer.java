package org.jenkinsci.test.acceptance.docker.fixtures;

import static org.jenkinsci.test.acceptance.po.PageObject.createRandomName;

import hudson.plugins.jira.soap.JiraSoapService;
import hudson.plugins.jira.soap.RemoteComment;
import hudson.plugins.jira.soap.RemoteIssue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import javax.xml.rpc.ServiceException;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.jira.JIRA;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;

/**
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id = "jira", ports = 2990)
public class JiraContainer extends DockerContainer {

    private JiraSoapService svc;
    private String token;

    public URL getURL() throws MalformedURLException {
        return new URL("http://" + ipBound(2990) + ':' + port(2990) + "/jira/");
    }

    /**
     * Wait until JIRA becomes up and running.
     */
    public void waitForReady(CapybaraPortingLayer p) {
        p.waitFor()
                .withMessage("Waiting for jira to come up")
                .withTimeout(Duration.ofSeconds(2000)) // [INFO] jira started successfully in 1064s
                .until(() -> {
                    try {
                        URLConnection connection = getURL().openConnection();
                        connection.setConnectTimeout(1000); // Prevent waiting too long for connection to timeout
                        String s = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                        return s.contains("System Dashboard");
                    } catch (SocketException e) {
                        return null;
                    }
                });
    }

    /**
     * Creates a project in JIRA
     *
     * @param key
     *      All caps JIRA project unique key like "JENKINS"
     * @param displayName
     *      Human readable description.
     */
    public void createProject(String key, String displayName) throws IOException, ServiceException {
        connect();
        svc.createProject(token, key, displayName, null, null, "admin", null, null, null);
    }

    public void createProject(String key) throws IOException, ServiceException {
        createProject(key, createRandomName());
    }

    /**
     * Creates a new issue in JIRA
     */
    public void createIssue(String key, String summary, String description) throws IOException, ServiceException {
        connect();
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(key);
        issue.setSummary(summary);
        issue.setDescription(description);
        issue.setType("3"); // Task
        svc.createIssue(token, issue);
    }

    public void createIssue(String key) throws IOException, ServiceException {
        createIssue(key, createRandomName(), createRandomName());
    }

    private void connect() throws IOException, ServiceException {
        if (svc == null) {
            svc = JIRA.connect(getURL());
            token = svc.login("admin", "admin");
        }
    }

    public List<RemoteComment> getComments(String ticket) throws IOException, ServiceException {
        connect();
        return List.of(svc.getComments(token, ticket));
    }

    public JiraSoapService getSvc() {
        return svc;
    }

    public String getToken() {
        return token;
    }
}
