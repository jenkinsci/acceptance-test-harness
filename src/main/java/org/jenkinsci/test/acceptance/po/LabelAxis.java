package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.Matchers;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Slaves")
public class LabelAxis extends Axis {
    public LabelAxis(PageObject context, String path) {
        super(context, path);
    }

    public void select(String name) {
        WebElement checkBox = find(by.path(getPath())).findElement(by.xpath(".//input[@name='values' and @json='%s']", name));
        if (!checkBox.isDisplayed()) {
            // unfold the labels and slaves sub-nodes
            find(by.xpath("//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel1']//a")).click();
            find(by.xpath("//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel2']//a")).click();

            waitFor(checkBox, Matchers.displayed(), 3);
        }
        check(checkBox, true);
    }
}

