/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.TestActivation;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerJob;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerNewServer;
import org.jenkinsci.test.acceptance.plugins.gerrit_trigger.GerritTriggerServer;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Set these (data) at mvn-test command line to use this test:<br>
 * <br>
 * - GerritTriggerTest.gtGerrituser=companion<br>
 * - GerritTriggerTest.gtHostname=gerrit.company.com<br>
 * - GerritTriggerTest.gtFrontEndUrl=https://gerrit.company.com/gerrit<br>
 * - GerritTriggerTest.gtProject=changed/by/this/test<br>
 * - GerritTriggerTest.gtPrivateKey=/home/companion/.ssh/id_rsa<br>
 *
 * @author Marco.Miller@ericsson.com
 * @author shebert@redhat.com
 */
@WithPlugins("gerrit-trigger")
@TestActivation({"gtGerrituser", "gtHostname", "gtFrontEndUrl", "gtProject", "gtPrivateKey"})
public class GerritTriggerTest extends AbstractJUnitTest {
    private static final Logger LOGGER = Logger.getLogger(GerritTriggerTest.class.getName());

    @Inject(optional = true)
    @Named("GerritTriggerTest.gtGerrituser")
    private String gtGerrituser;

    @Inject(optional = true)
    @Named("GerritTriggerTest.gtHostname")
    private String gtHostname;

    @Inject(optional = true)
    @Named("GerritTriggerTest.gtFrontEndUrl")
    private String gtGerritFrontUrl;

    @Inject(optional = true)
    @Named("GerritTriggerTest.gtProject")
    private String gtProject;

    @Inject(optional = true)
    @Named("GerritTriggerTest.gtPrivateKey")
    private String gtPrivateKey;

    // List of changes to abandon
    private List<String> changes = new ArrayList<>();
    private File ssh;

    @After
    public void abandonChanges() {
        for (String changeId : changes) {
            try {
                ProcessBuilder gerritQuery = new ProcessBuilder(
                        "ssh",
                        "-p",
                        "29418",
                        "-i",
                        gtPrivateKey,
                        gtGerrituser + "@" + gtHostname,
                        "gerrit",
                        "query",
                        "change_id=" + changeId,
                        "--format JSON");
                String json = removeLastLine(stringFrom(logProcessBuilderIssues(gerritQuery, "gerrit query")));
                JSONObject obj = new JSONObject(json);

                ProcessBuilder gerritAbandon = new ProcessBuilder(
                        "ssh",
                        "-p",
                        "29418",
                        "-i",
                        gtPrivateKey,
                        gtGerrituser + "@" + gtHostname,
                        "gerrit",
                        "review",
                        obj.getString("number") + ",1",
                        "--abandon");
                logProcessBuilderIssues(gerritAbandon, "gerrit abandon");
            } catch (InterruptedException | IOException | JSONException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    @Before
    public void setupSSHWrapper() throws IOException {
        ssh = File.createTempFile("jenkins", "ssh");
        ssh.deleteOnExit();

        Files.writeString(
                ssh.toPath(),
                "#!/bin/sh\n" + "exec ssh -o StrictHostKeyChecking=no -i " + gtPrivateKey + " \"$@\"",
                StandardCharsets.UTF_8);
        Files.setPosixFilePermissions(
                ssh.toPath(),
                new HashSet<>(List.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE)));

        changes = new ArrayList<>();
    }

    @BeforeClass
    public static void setup() throws IOException {
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
    }

    @Test
    public void gerrit_has_review_flags_checked_after_jenkins_set_them() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone(gtHostname);
        GerritTriggerServer server = new GerritTriggerServer(jenkins, gtHostname);
        server.saveTestServerConfig(gtHostname, gtGerritFrontUrl, gtGerrituser, gtPrivateKey);

        Job j = jenkins.jobs.create(FreeStyleJob.class);
        String jobName = j.name;
        GerritTriggerJob job = new GerritTriggerJob(jenkins, jobName);
        job.saveTestJobConfig(GerritTriggerJob.EventToTriggerOn.PatchsetCreated, gtHostname, gtProject);
        try {
            String changeId = pushChangeForReview(jobName).changeId;
            changes.add(changeId);
            Build b = new Build(j, j.getNextBuildNumber());
            assertTrue(b.waitUntilFinished().isSuccess());
            elasticSleep(10000);

            ProcessBuilder gerritQuery = new ProcessBuilder(
                    "ssh",
                    "-p",
                    "29418",
                    "-i",
                    gtPrivateKey,
                    gtGerrituser + "@" + gtHostname,
                    "gerrit",
                    "query",
                    "change_id=" + changeId,
                    "--all-approvals",
                    "--format JSON");
            String json = removeLastLine(stringFrom(logProcessBuilderIssues(gerritQuery, "gerrit query")));

            checkApprovalValueFromJSON(json, "Verified", 1);
            checkApprovalValueFromJSON(json, "Code-Review", 1);
        } catch (InterruptedException | IOException e) {
            fail(e.getMessage());
        }
    }

    private void checkApprovalValueFromJSON(String json, String labelName, int value) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray allArray = obj.getJSONArray("patchSets");
            boolean foundValue = false;
            for (int i = 0; i < allArray.length(); i++) {
                JSONObject testObj = allArray.getJSONObject(i);
                JSONArray approvals = testObj.getJSONArray("approvals");
                for (int a = 0; a < approvals.length(); a++) {
                    JSONObject aObj = approvals.getJSONObject(a);
                    if (aObj.has("type") && aObj.getInt("value") == value) {
                        foundValue = true;
                        break;
                    }
                }
            }
            assertTrue(labelName + " flag should be " + value, foundValue);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    private File createGitCommit(String jobName) throws IOException, InterruptedException {
        File dir = Files.createTempDirectory("jenkins" + "git").toFile();

        ProcessBuilder pb =
                new ProcessBuilder("git", "clone", gtGerrituser + "@" + gtHostname + ":" + gtProject, jobName);
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        assertThat(logProcessBuilderIssues(pb.directory(dir), "git clone").exitValue(), is(equalTo(0)));

        File file = new File(dir + "/" + jobName, jobName);
        file.delete(); // result !needed
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(String.valueOf(System.currentTimeMillis()));
        writer.close();
        dir = file.getParentFile();

        assertThat(
                logProcessBuilderIssues(new ProcessBuilder("git", "add", jobName).directory(dir), "git add")
                        .exitValue(),
                is(equalTo(0)));
        File hooksDir = new File(dir, ".git/hooks/");
        if (!hooksDir.exists()) {
            assertTrue(hooksDir.mkdir());
        }
        assertThat(
                logProcessBuilderIssues(
                                new ProcessBuilder(
                                                "scp",
                                                "-i",
                                                gtPrivateKey,
                                                "-p",
                                                "-P",
                                                "29418",
                                                gtGerrituser + "@" + gtHostname + ":hooks/commit-msg",
                                                ".git/hooks/")
                                        .directory(dir),
                                "scp commit-msg")
                        .exitValue(),
                is(equalTo(0)));
        assertThat(
                logProcessBuilderIssues(
                                new ProcessBuilder(
                                                "git",
                                                "commit",
                                                "-m",
                                                this.getClass().getName() + "(" + jobName + ")")
                                        .directory(dir),
                                "git commit")
                        .exitValue(),
                is(equalTo(0)));
        return dir;
    }

    private GitLogResult pushChangeForReview(String jobName) throws InterruptedException, IOException {
        File dir = createGitCommit(jobName);
        ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "HEAD:refs/for/master");
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        assertThat(logProcessBuilderIssues(pb.directory(dir), "git push").exitValue(), is(equalTo(0)));

        ProcessBuilder gitLog1Pb = new ProcessBuilder("git", "log", "-1").directory(dir);
        String output = stringFrom(logProcessBuilderIssues(gitLog1Pb, "git log"));

        GitLogResult gitLogResult = new GitLogResult();
        gitLogResult.changeId = valueFrom(output, ".+Change-Id:(.+)");
        gitLogResult.commitId = output.substring(6, 17);
        return gitLogResult;
    }

    private static String stringFrom(Process p) throws InterruptedException, IOException {
        assertThat(p.waitFor(), is(equalTo(0)));
        StringWriter writer = new StringWriter();
        IOUtils.copy(p.getInputStream(), writer, StandardCharsets.UTF_8);
        String string = writer.toString().replaceAll(System.lineSeparator(), "").replaceAll(" ", "");
        writer.close();
        return string;
    }

    private String valueFrom(String source, String regexWithGroup) {
        String value = null;
        Matcher m = Pattern.compile(regexWithGroup).matcher(source);
        if (m.matches()) {
            value = m.group(1);
        }
        assertThat(value, is(not(nullValue())));
        return value;
    }

    private Process logProcessBuilderIssues(ProcessBuilder pb, String commandName)
            throws InterruptedException, IOException {
        String dir = "";
        if (pb.directory() != null) {
            dir = pb.directory().getAbsolutePath();
        }
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        LOGGER.info("Running : " + pb.command() + " => directory: " + dir);
        Process processToRun = pb.start();
        int result = processToRun.waitFor();
        if (result != 0) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(processToRun.getErrorStream(), writer, StandardCharsets.UTF_8);
            LOGGER.severe("Issue occurred during command \"" + commandName + "\":\n" + writer);
            writer.close();
        }
        return processToRun;
    }

    @Test
    public void build_is_triggered_after_ref_is_updated() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone(gtHostname);
        GerritTriggerServer server = new GerritTriggerServer(jenkins, gtHostname);

        server.saveTestServerConfig(gtHostname, gtGerritFrontUrl, gtGerrituser, gtPrivateKey);

        Job j = jenkins.jobs.create(FreeStyleJob.class);
        String jobName = j.name;
        GerritTriggerJob job = new GerritTriggerJob(jenkins, jobName);

        job.saveTestJobConfig(GerritTriggerJob.EventToTriggerOn.RefUpdated, gtHostname, gtProject);

        try {
            pushChangeToGerritNotForReview(jobName);
            Build b = new Build(j, j.getNextBuildNumber());
            assertTrue(b.waitUntilFinished().isSuccess());
            elasticSleep(10000);
        } catch (InterruptedException | IOException e) {
            fail(e.getMessage());
        }
    }

    private String pushChangeToGerritNotForReview(String jobName) throws InterruptedException, IOException {
        File dir = createGitCommit(jobName);
        ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "master:master");
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        assertThat(logProcessBuilderIssues(pb.directory(dir), "git push").exitValue(), is(equalTo(0)));

        ProcessBuilder gitLog1Pb = new ProcessBuilder("git", "log", "-1").directory(dir);
        return valueFrom(stringFrom(logProcessBuilderIssues(gitLog1Pb, "git log")), ".+Change-Id:(.+)");
    }

    @Test
    public void build_is_triggered_after_comment_is_added() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone(gtHostname);
        GerritTriggerServer server = new GerritTriggerServer(jenkins, gtHostname);
        server.saveTestServerConfig(gtHostname, gtGerritFrontUrl, gtGerrituser, gtPrivateKey);

        Job j = jenkins.jobs.create(FreeStyleJob.class);
        String jobName = j.name;
        GerritTriggerJob job = new GerritTriggerJob(jenkins, jobName);
        job.saveTestJobConfig(GerritTriggerJob.EventToTriggerOn.CommentAdded, gtHostname, gtProject);
        try {
            GitLogResult gitLogResult = pushChangeForReview(jobName);
            addComment(gitLogResult.commitId);
            changes.add(gitLogResult.changeId);
            Build b = new Build(j, j.getNextBuildNumber());
            assertTrue(b.waitUntilFinished().isSuccess());
            elasticSleep(10000);

            ProcessBuilder gerritQuery = new ProcessBuilder(
                    "ssh",
                    "-p",
                    "29418",
                    "-i",
                    gtPrivateKey,
                    gtGerrituser + "@" + gtHostname,
                    "gerrit",
                    "query",
                    "change_id=" + gitLogResult.changeId,
                    "--all-approvals",
                    "--format JSON");
            String json = removeLastLine(stringFrom(logProcessBuilderIssues(gerritQuery, "gerrit query")));

            checkApprovalValueFromJSON(json, "Verified", 1);
            checkApprovalValueFromJSON(json, "Code-Review", 1);
        } catch (InterruptedException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void gerrit_trigger_build_when_draft_published() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone(gtHostname);
        GerritTriggerServer server = new GerritTriggerServer(jenkins, gtHostname);
        server.saveTestServerConfig(gtHostname, gtGerritFrontUrl, gtGerrituser, gtPrivateKey);

        Job j = jenkins.jobs.create(FreeStyleJob.class);
        GerritTriggerJob job = new GerritTriggerJob(jenkins, j.name);
        job.saveTestJobConfig(GerritTriggerJob.EventToTriggerOn.DraftPublished, gtHostname, gtProject);
        try {
            String changeId = pushDraftForPublishing(j.name);
            changes.add(changeId);
            Build b = new Build(j, j.getNextBuildNumber());
            assertTrue(b.waitUntilFinished().isSuccess());
            elasticSleep(10000);

            ProcessBuilder gerritQuery = new ProcessBuilder(
                    "ssh",
                    "-p",
                    "29418",
                    "-i",
                    gtPrivateKey,
                    gtGerrituser + "@" + gtHostname,
                    "gerrit",
                    "query",
                    "change_id=" + changeId,
                    "--all-approvals",
                    "--format JSON");
            String json = removeLastLine(stringFrom(logProcessBuilderIssues(gerritQuery, "gerrit query")));
            LOGGER.info(json);

            checkApprovalValueFromJSON(json, "Verified", 1);
            checkApprovalValueFromJSON(json, "Code-Review", 1);
        } catch (InterruptedException | IOException e) {
            fail(e.getMessage());
        }
    }

    private static String removeLastLine(String x) {
        if (x.lastIndexOf("\n") > 0) {
            return x.substring(0, x.lastIndexOf("\n"));
        } else {
            return x;
        }
    }

    public String pushDraftForPublishing(String jobName) throws InterruptedException, IOException {
        File dir = createGitCommit(jobName);

        ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "HEAD:refs/drafts/master");
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        assertThat(logProcessBuilderIssues(pb.directory(dir), "git push").exitValue(), is(equalTo(0)));

        ProcessBuilder gitLog1Pb = new ProcessBuilder("git", "rev-parse", "HEAD").directory(dir);
        String commitID = stringFrom(logProcessBuilderIssues(gitLog1Pb, "git log"));
        ProcessBuilder gitLog2Pb = new ProcessBuilder("git", "log", "-1").directory(dir);
        String changeID = valueFrom(stringFrom(logProcessBuilderIssues(gitLog2Pb, "git log")), ".+Change-Id:(.+)");
        assertThat(
                logProcessBuilderIssues(
                                new ProcessBuilder(
                                                "ssh",
                                                "-i",
                                                gtPrivateKey,
                                                "-p",
                                                "29418",
                                                gtGerrituser + "@" + gtHostname,
                                                "gerrit",
                                                "review",
                                                commitID,
                                                "--publish")
                                        .directory(dir),
                                "git publish")
                        .exitValue(),
                is(equalTo(0)));
        return changeID;
    }

    @Test
    public void gerrit_trigger_build_when_changes_merged() {
        GerritTriggerNewServer newServer = new GerritTriggerNewServer(jenkins);
        newServer.saveNewTestServerConfigIfNone(gtHostname);
        GerritTriggerServer server = new GerritTriggerServer(jenkins, gtHostname);
        server.saveTestServerConfig(gtHostname, gtGerritFrontUrl, gtGerrituser, gtPrivateKey);

        Job j = jenkins.jobs.create(FreeStyleJob.class);
        String jobName = j.name;
        GerritTriggerJob job = new GerritTriggerJob(jenkins, jobName);
        job.saveTestJobConfig(GerritTriggerJob.EventToTriggerOn.ChangeMerged, gtHostname, gtProject);
        try {
            String changeId = pushChangeForMerge(jobName);
            changes.add(changeId);
            Build b = new Build(j, j.getNextBuildNumber());
            assertTrue(b.waitUntilFinished().isSuccess());
            elasticSleep(10000);

            ProcessBuilder gerritQuery = new ProcessBuilder(
                    "ssh",
                    "-p",
                    "29418",
                    "-i",
                    gtPrivateKey,
                    gtGerrituser + "@" + gtHostname,
                    "gerrit",
                    "query",
                    "change_id=" + changeId,
                    "--all-approvals",
                    "--format JSON");
            String json = removeLastLine(stringFrom(logProcessBuilderIssues(gerritQuery, "gerrit query")));

            checkApprovalValueFromJSON(json, "Verified", 1);
            checkApprovalValueFromJSON(json, "Code-Review", 1);
        } catch (InterruptedException | IOException e) {
            fail(e.getMessage());
        }
    }

    public String pushChangeForMerge(String jobName) throws InterruptedException, IOException {
        File dir = createGitCommit(jobName);
        ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "HEAD:refs/for/master");
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());
        assertThat(logProcessBuilderIssues(pb.directory(dir), "git push").exitValue(), is(equalTo(0)));

        String commitID = stringFrom(
                logProcessBuilderIssues(new ProcessBuilder("git", "rev-parse", "HEAD").directory(dir), "git log"));
        ProcessBuilder gitLog2Pb = new ProcessBuilder("git", "log", "-1").directory(dir);
        String changeID = valueFrom(stringFrom(logProcessBuilderIssues(gitLog2Pb, "git log")), ".+Change-Id:(.+)");

        pb = new ProcessBuilder(
                "ssh",
                "-p",
                "29418",
                "-i",
                gtPrivateKey,
                gtGerrituser + "@" + gtHostname,
                "gerrit",
                "review",
                commitID,
                "--verified 1");
        assertThat(logProcessBuilderIssues(pb.directory(dir), "gerrit verify").exitValue(), is(equalTo(0)));

        pb = new ProcessBuilder(
                "ssh",
                "-p",
                "29418",
                "-i",
                gtPrivateKey,
                gtGerrituser + "@" + gtHostname,
                "gerrit",
                "review",
                commitID,
                "--code-review 2");
        assertThat(
                logProcessBuilderIssues(pb.directory(dir), "gerrit code review").exitValue(), is(equalTo(0)));

        pb = new ProcessBuilder(
                "ssh",
                "-p",
                "29418",
                "-i",
                gtPrivateKey,
                gtGerrituser + "@" + gtHostname,
                "gerrit",
                "review",
                commitID,
                "--submit");
        assertThat(logProcessBuilderIssues(pb.directory(dir), "gerrit merge").exitValue(), is(equalTo(0)));

        return changeID;
    }

    private void addComment(String commitId) throws InterruptedException, IOException {
        assertThat(
                logProcessBuilderIssues(
                                new ProcessBuilder(
                                        "ssh",
                                        "-p",
                                        "29418",
                                        "-i",
                                        gtPrivateKey,
                                        gtGerrituser + "@" + gtHostname,
                                        "gerrit",
                                        "review",
                                        commitId,
                                        "--code-review -2"),
                                "ssh gerrit --code-review")
                        .exitValue(),
                is(equalTo(0)));
    }

    private class GitLogResult {
        private String commitId;
        private String changeId;
    }
}
