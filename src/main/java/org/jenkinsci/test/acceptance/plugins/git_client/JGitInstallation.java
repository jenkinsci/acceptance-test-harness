package org.jenkinsci.test.acceptance.plugins.git_client;

import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;

@ToolInstallationPageObject(name = "Git", installer = "hudson.tools.ZipExtractionInstaller")
public class JGitInstallation extends ToolInstallation {

    public JGitInstallation(Jenkins jenkins, String path) {
        super(jenkins, path);
    }

    public static JGitInstallation addJGit(Jenkins jenkins) {
        ConfigurablePageObject toolsPage = ToolInstallation.ensureConfigPage(jenkins);

        final String name = JGitInstallation.class
                .getAnnotation(ToolInstallationPageObject.class)
                .name();
        final Control button = toolsPage.control(by.button("Add " + name));

        String pathPrefix =
                button.resolve().getAttribute("path").replaceAll(Pattern.quote("hetero-list-add[tool]"), "tool");
        return ToolInstallation.addTool(
                jenkins, JGitInstallation.class, pathPrefix, () -> button.selectDropdownMenu("JGit"));
    }
}
