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
package org.jenkinsci.test.acceptance.plugins.gerrit_trigger;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * Page Object for Gerrit Trigger test-job (configuration) page.
 * @author Marco.Miller@ericsson.com
 */
public class GerritTriggerJob extends PageObject {

    public final Jenkins jenkins;
    public final Control event = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger");
    public final Control server = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/serverName");
    public final Control advanced = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/advanced-button");
    public final Control passVerif = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritBuildSuccessfulVerifiedValue");
    public final Control failVerif = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritBuildFailedVerifiedValue");
    public final Control passRev = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritBuildSuccessfulCodeReviewValue");
    public final Control failRev = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritBuildFailedCodeReviewValue");
    public final Control project = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritProjects/pattern");
    public final Control branch = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritProjects/branches/pattern");
    public final Control triggerOnAdd = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/hetero-list-add[triggerOnEvents]");
    public final Control commentAddedTriggerVerdictCategory = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/triggerOnEvents/verdictCategory");
    public final Control commentAddedTriggerApprovalValue = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/triggerOnEvents/commentAddedTriggerApprovalValue");

    public GerritTriggerJob(Jenkins jenkins,String jobName) {
        super(jenkins.injector,jenkins.url("job/"+jobName+"/configure"));
        this.jenkins = jenkins;
    }

    /**
     * Saves harness' gerrit-trigger test-job configuration.
     * @param eventToTriggerOn event to trigger on
     */
    public void saveTestJobConfig(EventToTriggerOn eventToTriggerOn) {
        if(!event.resolve().isSelected()) event.click();
        server.select(this.getClass().getPackage().getName());

        String displayName = eventToTriggerOn.getDisplayName();
        switch (eventToTriggerOn) {
            case PatchsetCreated: case RefUpdated: case ChangeMerged: case DraftPublished:
                triggerOnAdd.selectDropdownMenu(displayName);
                break;

            case CommentAdded:
                triggerOnAdd.selectDropdownMenu(displayName);
                commentAddedTriggerVerdictCategory.select("Code-Review");
                commentAddedTriggerApprovalValue.set("-2");
                break;

            default:
                throw new IllegalArgumentException("not supported event to trigger on: " + displayName);
        }
        advanced.click();
        passVerif.set("1");
        failVerif.set("-1");
        passRev.set("1");
        failRev.set("-1");
        project.set(GerritTriggerEnv.get().getProject());
        branch.set("master");
        clickButton("Save");
    }

    /**
     * represents the options of events to trigger on for Gerrit-Trigger plugin
     */
    public enum EventToTriggerOn {
        ChangeAbandoned("Change Abandoned"), ChangeMerged("Change Merged"), ChangeRestored(
            "Change Restored"),
        CommentAdded("Comment Added"), CommentAddedContainsRegularExpression(
            "Comment Added Contains Regular Expression"),
        DraftPublished("Draft Published"), PatchsetCreated("Patchset Created"), RefUpdated(
            "Ref Updated");
        private String displayName;

        EventToTriggerOn(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
