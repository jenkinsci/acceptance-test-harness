package org.jenkinsci.test.acceptance.junit;

import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Indicates the native commands necessary to run tests.
 * 
 * Works for Windows and UNIX systems. 
 *
 * <p>
 * Test gets skipped under the following circumstances:
 * <ul>
 * <li>If any of these commands do not exist.</li>
 * <li>If a LocalController is not used, since we cannot ensure that the commands are present
 * in the machine running Jenkins.</li>
 * </ul>
 * 
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(Native.RuleImpl.class)
public @interface Native {
    String[] value();

    public class RuleImpl implements TestRule {
        @Inject JenkinsController controller;
        
        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                
                @Override
                public void evaluate() throws Throwable {
                    verifyNativeCommandPresent(d.getAnnotation(Native.class));
                    verifyNativeCommandPresent(d.getTestClass().getAnnotation(Native.class));

                    base.evaluate();
                }

                private void verifyNativeCommandPresent(Native n) throws IOException, InterruptedException {
                    if (n==null)        return;
                    
                    // Checks performed in this annotation only makes sense if Jenkins and Tests are being executed in the same machine
                    if (!(controller instanceof LocalController)) {
                        throw new AssumptionViolatedException("Test skipped. Native should be used with a local controller, otherwise it cannot be fully trusted to work as expected.");
                    }
                    
                    List<String> supportedCmds = Lists.newLinkedList();
                    // Get PATH. Valid for both Unix and Windows
                    String path = System.getenv("PATH");
                    if (path != null) {
                        // Get all directories in PATH
                        String[] dirsInPath = path.split(File.pathSeparator);
                        
                        // Iterate over PATH directories to get all supported commands
                        for (String dirName : dirsInPath) {
                            File dir = new File(dirName);
                            if (dir.isDirectory()) {
                                String[] filesInDir = dir.list();
                                if (filesInDir != null) {
                                    supportedCmds.addAll(Arrays.asList(filesInDir));
                                }
                            }
                        }
                    }
                    // An cmd is not Native if it cannot be found in the PATH
                    for (String cmd : n.value()) {
                        if (!supportedCmds.contains(cmd) && !supportedCmds.contains(cmd + ".bat") && !supportedCmds.contains(cmd + ".exe")) {
                            throw new AssumptionViolatedException(cmd + " is needed for the test but doesn't exist in the system.");
                        }
                    }
                }
            };
        }
    }
}
