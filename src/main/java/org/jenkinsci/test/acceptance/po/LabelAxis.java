package org.jenkinsci.test.acceptance.po;

import java.time.Duration;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable({"Agents", "Slaves"}) // Agents for Matrix Project Plugin >= 1.15
public class LabelAxis extends Axis {
    public LabelAxis(PageObject context, String path) {
        super(context, path);
    }

    public void select(String name) {
        WebElement checkBox =
                find(by.path(getPath())).findElement(by.xpath(".//input[@name='values' and @json='%s']", name));
        if (!checkBox.isDisplayed()) {
            // unfold the labels and slaves sub-nodes
            find(by.xpath("(//button[@class='jenkins-button mp-label-axis__button'])[1]"))
                    .click();
            find(by.xpath("(//button[@class='jenkins-button mp-label-axis__button'])[2]"))
                    .click();

            waitFor().withTimeout(Duration.ofSeconds(3)).until(checkBox::isDisplayed);
        }
        check(checkBox, true);
    }
}
