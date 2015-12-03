package org.jenkinsci.test.acceptance.junit;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.ClassRule;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Convenience base class to derive your plain-old JUnit tests from when using {@link JenkinsAcceptanceTestRule} as a {@link ClassRule}.
 * <ol>
 *     <li>Use of the {@link ClassRule} is intended for those cases in which stateful tests are performed:
 *     e.g., tests targeted at and existing instance. An example would be test suites to perform tasks
 *     soak test environment: populate data, check status, schedule jobs.</li>
 *     <li>Use of the {@link ClassRule} is not recommended for the normal ATH use case in which stateless tests are always
 *     the preferred option. The exception would be tests with a very expensive initialization sequence, but in
 *     that case this circumstance should be clearly documented, as well as the measures taken to avoid issues for
 *     being stateful.</li>
 * </ol>
 *
 * @author Andres Rodriguez
 */
public class AbstractClassBasedJUnitTest extends CapybaraPortingLayerImpl {

    @ClassRule
    public static JenkinsAcceptanceTestRule rules = JenkinsAcceptanceTestRule.classRule();

    /** Method {@link RuleAnnotation}s are forbidden. */
    public final NoMethodRuleAnnotationRule noMethodRuleAnnotations = NoMethodRuleAnnotationRule.rule();

    /**
     * Jenkins under test.
     */
    @Inject
    public static Jenkins jenkins;

    /**
     * This field receives a valid web driver object you can use to talk to Jenkins.
     */
    @Inject
    public static WebDriver driver;

    public AbstractClassBasedJUnitTest() {
        super(null);
    }
}
