package plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.warnings.MavenConsoleParser;
import org.jenkinsci.test.acceptance.plugins.warnings.SourceCodeView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesRecorder;
import org.jenkinsci.test.acceptance.po.Build;
import org.junit.Test;

import plugins.warnings.assertions.MavenConsoleParserAssert;
import plugins.warnings.assertions.SourceCodeViewAssert;

/**
 * This class is an UI test for the source code view and the MavenConsoleParser view and is intended to be merged with
 * the WarningsPluginTest class.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
@WithPlugins("warnings")
public class MavenConsoleParserAndSourceCodeViewTest extends AbstractJUnitTest {

    private static final String DEFAULT_ENTRY_PATH_ECLIPSE = "/eclipseResult/";
    private static final String DEFAULT_ENTRY_PATH_MAVEN = "/mavenResult/";

    private static final String DIRECTORY_WITH_TESTFILES = "/analysis/mavenconsoleparserandsourceviewfiles/";
    private static final String PREFIX_TESTFILE_PATH = "src/test/resources";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Verifies that source codes shown on the web page (headers + file contents) are displayed correctly.
     */
    @Test
    public void shouldVerifyThatHeadersAndFileContentsAreShownCorrectlyInTheSourceCodeView() throws IOException {

        List<String> files = new ArrayList<>(Arrays.asList(
                DIRECTORY_WITH_TESTFILES + "SampleClassWithBrokenPackageNaming.java",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithNamespace.cs",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithNamespaceBetweenCode.cs",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithNestedAndNormalNamespace.cs",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithoutNamespace.cs",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithoutPackage.java",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithPackage.java",
                DIRECTORY_WITH_TESTFILES + "SampleClassWithUnconventionalPackageNaming.java"));

        List<String> headers = new ArrayList<>(Arrays.asList("Content of file NOT_EXISTING_FILE",
                "Content of file SampleClassWithBrokenPackageNaming.java",
                "Content of file SampleClassWithNamespace.cs",
                "Content of file SampleClassWithNamespaceBetweenCode.cs",
                "Content of file SampleClassWithNestedAndNormalNamespace.cs",
                "Content of file SampleClassWithoutNamespace.cs",
                "Content of file SampleClassWithoutPackage.java",
                "Content of file SampleClassWithPackage.java",
                "Content of file SampleClassWithUnconventionalPackageNaming.java"));

        List<String> fileContentList = new ArrayList<>();
        prepareFileContentList(files, fileContentList);

        files.add(DIRECTORY_WITH_TESTFILES + "DUMMY_FILE_WITH_CONTENT");

        MavenModuleSet job = installMavenAndCreateMavenProject();
        copyDirectoryToWorkspace(job, PREFIX_TESTFILE_PATH + DIRECTORY_WITH_TESTFILES);
        configureJob(job, "Eclipse ECJ", "**/*Classes.txt");
        job.save();

        buildMavenJobWithExpectedFailureResult(job);

        String eclipseResultPath = job.getLastBuild().getNumber() + DEFAULT_ENTRY_PATH_ECLIPSE;

        SourceCodeView sourceCodeView = new SourceCodeView(job, jenkins.getName(),
                eclipseResultPath).processSourceCodeData();

        SourceCodeViewAssert.assertThat(sourceCodeView).hasCorrectFileSize(fileContentList.size());
        SourceCodeViewAssert.assertThat(sourceCodeView).hasCorrectHeaderSize(headers.size());
        SourceCodeViewAssert.assertThat(sourceCodeView).fileSizeIsMatchingHeaderSize();
        SourceCodeViewAssert.assertThat(sourceCodeView).hasCorrectSources(fileContentList);
        SourceCodeViewAssert.assertThat(sourceCodeView).hasCorrectHeaders(headers);
    }

    /**
     * Verifies that messages from the MavenConsoleParser are displayed correctly.
     */
    @Test
    public void shouldVerifyThatMessagesFromTheMavenConsoleParserAreDisplayedCorrectly() {

        String fileWithModuleConfiguration =
                DIRECTORY_WITH_TESTFILES + "pom.xml";

        List<String> parserExpectedMessages = new ArrayList<>(Arrays.asList(
                "[WARNING] For this reason, future Maven versions might no longer support building such malformed projects."
                        + LINE_SEPARATOR +
                        "[WARNING]",
                "[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!",
                "[ERROR] For more information about the errors and possible solutions, please read the following articles:"
                        + LINE_SEPARATOR +
                        "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginConfigurationException"
        ));

        MavenModuleSet job = installMavenAndCreateMavenProject();
        copyResourceFilesToWorkspace(job, fileWithModuleConfiguration);
        configureJob(job, "Maven", "");
        job.save();

        buildMavenJobWithExpectedFailureResult(job);

        String mavenResultPath = job.getLastBuild().getNumber() + DEFAULT_ENTRY_PATH_MAVEN;

        MavenConsoleParser mavenConsoleParser = new MavenConsoleParser(job, jenkins.getName(),
                mavenResultPath).processMavenConsoleParserOutput();

        String headerMessage = "Console Details";
        MavenConsoleParserAssert.assertThat(mavenConsoleParser).fileSizeIsMatchingHeaderSize();
        MavenConsoleParserAssert.assertThat(mavenConsoleParser).containsMessage(parserExpectedMessages);
        MavenConsoleParserAssert.assertThat(mavenConsoleParser).hasCorrectHeader(headerMessage);
    }

    private MavenModuleSet installMavenAndCreateMavenProject() {
        MavenInstallation.installSomeMaven(jenkins);
        return jenkins.getJobs().create(MavenModuleSet.class);
    }

    private void configureJob(final MavenModuleSet job, final String toolName, final String pattern) {
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setToolWithPattern(toolName, pattern);
        recorder.openAdvancedOptions();
        recorder.deleteFilter();
        recorder.setEnabledForFailure(true);
    }

    private void buildMavenJobWithExpectedFailureResult(
            final MavenModuleSet job) {
        Build build = job.startBuild().waitUntilFinished();
        build.shouldFail();
    }

    private void copyResourceFilesToWorkspace(final MavenModuleSet job,
            final String... resources) {
        for (String file : resources) {
            job.copyResource(file);
        }
    }

    private void prepareFileContentList(final List<String> files, final List<String> fileContentList)
            throws IOException {
        fileContentList.add("Content of file NOT_EXISTING_FILE" + LINE_SEPARATOR
                + "Can't read file: /NOT/EXISTING/PATH/TO/NOT_EXISTING_FILE (No such file or directory)");
        addFileContentToList(files, fileContentList);
    }

    private void addFileContentToList(final List<String> files, final List<String> fileContentList) throws IOException {
        for (String fileContent : files) {
            InputStream encoded = this.getClass().getResourceAsStream(fileContent);
            fileContentList.add(IOUtils.toString(encoded, Charset.defaultCharset()));
        }
    }

    private void copyDirectoryToWorkspace(final MavenModuleSet job,
            final String directory) throws MalformedURLException {
        job.copyDir(new Resource(new File(new File(directory).getAbsolutePath()).toURI().toURL()));
    }

}
