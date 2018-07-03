package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.net.URL;
import java.util.List;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

/**
 * Page object that represents the source code view.
 *
 * @author Frank Christian Geyer
 * @author Ullric Hafner
 * @author Deniz Mardin
 * @author Stephan Plöderl
 */
public class ConsoleLogView extends PageObject {
    /**
     * Creates a new source code view.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    public ConsoleLogView(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Returns the title displayed in the header.
     *
     * @return the title
     */
    public String getTitle() {
        return find(by.tagName("h1")).getText();
    }

    /**
     * Returns the highlighted text.
     *
     * @return the highlighted text
     */
    public String getHighlightedText() {
        if (getStyleTags().size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < getStyleTags().size(); i++) {
                stringBuilder.append(getStyleTags().get(i).getText());
                int firstElementOccurrence = 0;
                if (i == firstElementOccurrence) {
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            }
            return stringBuilder.toString();
        }
        else {
            int onlyOneElementOccurrence = 0;
            return getStyleTags().get(onlyOneElementOccurrence).getText();
        }
    }

    private List<WebElement> getStyleTags() {
        return driver.findElements(by.xpath("//td[contains(@style, 'background-color')]"));
    }
}
