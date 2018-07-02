package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * PageObject for warnings charts.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
public class WarningsCharts extends ContainerPageObject {
    private static final String RESULT_PATH_END = "Result/";

    /**
     * Constructor of the class {@link WarningsCharts}.
     *
     * @param parent
     *         the build
     * @param id
     *         the url of the page
     */
    public WarningsCharts(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }
}
