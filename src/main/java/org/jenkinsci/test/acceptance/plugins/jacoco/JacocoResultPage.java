package org.jenkinsci.test.acceptance.plugins.jacoco;

import com.fasterxml.jackson.databind.JsonNode;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for the jacoco result page.
 */
public class JacocoResultPage extends ContainerPageObject {

    private JsonNode data = null;

    public JacocoResultPage(Build context) {
        super(context, context.url("jacoco/"));
    }

    public JacocoResultPage(JacocoResultPage context, String pkg) {
        super(context, context.url(pkg + '/'));
    }

    public double instructionCoverage() {
        return data().get("instructionCoverage").get("percentageFloat").asDouble();
    }

    public double branchCoverage() {
        return data().get("branchCoverage").get("percentageFloat").asDouble();
    }

    public double complexityScore() {
        return data().get("complexityScore").get("percentageFloat").asDouble();
    }

    public double lineCoverage() {
        return data().get("lineCoverage").get("percentageFloat").asDouble();
    }

    public double methodCoverage() {
        return data().get("methodCoverage").get("percentageFloat").asDouble();
    }

    public double classCoverage() {
        return data().get("classCoverage").get("percentageFloat").asDouble();
    }

    private JsonNode data() {
        open(); // Follow what we are doing in browser
        if (data == null) {
            data = getJson();
        }
        return data;
    }
}
