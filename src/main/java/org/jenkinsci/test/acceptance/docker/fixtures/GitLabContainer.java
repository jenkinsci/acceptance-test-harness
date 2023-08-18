package org.jenkinsci.test.acceptance.docker.fixtures;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
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

import static org.junit.Assert.assertTrue;

@DockerFixture(id = "gitlab-plugin", ports = 22)
public class GitLabContainer extends DockerContainer {
    protected static final String REPO_DIR = "/home/gitlab/gitlabRepo";

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

    public Response createRepo(String repoName, String token) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{ \"name\": \"" + repoName + "\" }");
        Request request = new Request.Builder().url("http://" + getIpAddress()+"/api/v4/projects").post(body).addHeader("Content-Type", "application/json").addHeader("PRIVATE-TOKEN", token).build();
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

    public void deleteRepo(String token) throws IOException {
        // get the project id and delete the project
        GitLabApi gitlabapi = new GitLabApi("http://" + getIpAddress(), token);
        ProjectApi projApi = new ProjectApi(gitlabapi);

        try {
            Project project = projApi.getProjects().get(0);
            projApi.deleteProject(project.getId());
            assertTrue(projApi.getProjects().size()==0);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String createUserToken() throws IOException, InterruptedException {
        return Docker.cmd("exec", getCid()).add("/bin/bash",  "-c", "gitlab-rails runner -e production /usr/bin/create_user.rb")
                .popen()
                .verifyOrDieWith("Unable to create user").trim();
    }
}