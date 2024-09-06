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
            find(by.xpath("//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel1']//a"))
                    .click();
            find(by.xpath("//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel2']//a"))
                    .click();

            waitFor().withTimeout(Duration.ofSeconds(3)).until(checkBox::isDisplayed);
        }
        check(checkBox, true);
    }
}
