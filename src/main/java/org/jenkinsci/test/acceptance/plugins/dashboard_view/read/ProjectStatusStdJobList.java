package org.jenkinsci.test.acceptance.plugins.dashboard_view.read;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides some read accesses to the std job list in a dashboard view.
 *
 * @author Peter Müller
 */
public class ProjectStatusStdJobList extends PageAreaImpl {

    /**
     * Prefix for id used in the html.
     */
    private static final String JOB_ID_PREFIX = "job_";
    /**
     * The main table.
     */
    private final By projectStatusTable = By.xpath("//table[@id=\"projectstatus\"]/tbody");
    /**
     * The header of the table.
     */
    private final By header = By.xpath(".//tr[@class=\"header\"]");

    public ProjectStatusStdJobList(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Get all headers as string displayed in the table.
     *
     * @return the names of the headers.
     */
    public List<String> getHeaders() {
        return find(projectStatusTable).findElement(header)
                .findElements(By.xpath(".//a"))
                .stream().map(WebElement::getText)
                .collect(Collectors.toList());

    }


    /**
     * Get all job ids (names) appearing in the table.
     *
     * @return a list of all ids displayed in the table.
     */
    public List<String> getJobIDs() {
        return find(projectStatusTable).findElements(By.xpath(".//tr"))
                .stream()
                // ignore the header row from <2.321
                .filter(tr -> !StringUtils.contains(tr.getAttribute("class"), "header"))
                .map(el -> el.getAttribute("id"))
                .map(s -> s.replaceFirst(JOB_ID_PREFIX, ""))
                .collect(Collectors.toList());

    }
}
