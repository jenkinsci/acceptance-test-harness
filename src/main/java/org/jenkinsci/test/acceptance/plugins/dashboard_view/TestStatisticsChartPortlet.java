package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * The test statistics chart shipped with the dashboard view plugin.
 *
 * @author René Zarwel
 */
@Describable("Test Statistics Chart")
public class TestStatisticsChartPortlet extends AbstractDashboardViewPortlet {

    /**
     * ID of this Portlet in Listview.
     **/
    public static final String TEST_STATISTICS_CHART = "Test Statistics Chart";

    /**
     * Constructs a new test statistics chart portlet.
     *
     * @param parent Dashboard view this portlet is scoped to.
     * @param path   Absolute path to the area.
     */
    public TestStatisticsChartPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Gets the chart image as {@link WebElement}.
     *
     * @return The chart_image-{@link WebElement} containing the statistics.
     * @throws NoSuchElementException if the chart is not found
     */
    public WebElement getChart() throws NoSuchElementException {
        return find(By.xpath("//div[contains(.,'" + TEST_STATISTICS_CHART + "')]/following::img[1]"));
    }

    /**
     * Gets the chart image as {@link BufferedImage}.
     *
     * @return The chart_image-{@link BufferedImage} containing the statistics.
     * @throws IOException if the chart is not found
     */
    public BufferedImage getImage() throws IOException {
        URL imageUrl = new URL(getChart().getAttribute("src"));
        return ImageIO.read(imageUrl);
    }

}
