package org.jenkinsci.test.acceptance.plugins.csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ContentSecurityPolicyReport extends PageObject {
    public ContentSecurityPolicyReport(Jenkins context) {
        super(context, context.url("content-security-policy-reports/"));
    }

    public List<String> getReport() {
        List<String> lines = new ArrayList<>();
        WebElement table = getElement(By.className("jenkins-table"));
        if (table == null) {
            return Collections.emptyList();
        }

        List<WebElement> headers = table.findElements(By.tagName("th"));
        StringBuilder sb = new StringBuilder();
        for (WebElement header : headers) {
            sb.append(header.getText()).append("\t");
        }
        lines.add(sb.toString());
        sb = new StringBuilder();
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (WebElement cell : cells) {
                sb.append(cell.getText()).append("\t");
            }
            lines.add(sb.toString());
            sb = new StringBuilder();
        }
        return lines;
    }
}
