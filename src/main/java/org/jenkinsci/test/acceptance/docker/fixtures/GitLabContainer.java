package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@DockerFixture(id = "gitlab-plugin", ports = 22)
public class GitLabContainer extends DockerContainer {
    protected static final String REPO_DIR = "/home/gitlab/gitlabRepo";

    private final String PRIVATE_TOKEN ="TEMP_TOKEN"; //TODO: Change this for a real token

    private static OkHttpClient client = new OkHttpClient();

    public String host() {
        return ipBound(22);
    }

    public int port() {
        return port(22);
    }

    public URL getUrl() throws IOException {
        return new URL("http://" + host() + ":" + port());
    }

    /** URL visible from the host. */
    public String getRepoUrl() {
        return "ssh://git@" + host() + ":" + port() + REPO_DIR;
    }

    @Deprecated
    public String getRepoUrlInsideDocker() throws IOException {
        return "ssh://git@" + getIpAddress() + REPO_DIR;
    }

    /**
     * URL visible from other Docker containers.
     * @param alias an alias for this container’s {@link #getCid} passed to {@code --link}
     */
    public String getRepoUrlInsideDocker(String alias) throws IOException {
        return "ssh://git@" + alias + REPO_DIR;
    }

    public Response createRepo(String repoName) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{ \"name\": \"" + repoName + "\" }");
        Request request = new Request.Builder().url("http://localhost/api/v4/projects").post(body).addHeader("Content-Type", "application/json").addHeader("PRIVATE-TOKEN", PRIVATE_TOKEN).build();
        return sendRequest(request);
    }

    private Response sendRequest(Request request){
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}