package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The basic Build statistics portlet shipped with the dashboard view plugin.
 *
 * @author Rene Zarwel
 */
@Describable("Build statistics")
public class BuildStatisticsPortlet extends AbstractDashboardViewPortlet {

  public BuildStatisticsPortlet(DashboardView parent, String path) {
    super(parent, path);
  }

  public WebElement getTable(){
    return find(By.id("statistics"));
  }

  public int getFailedJobsCount(){
    return Integer.valueOf(getTable().findElement(By.xpath(".//tbody/tr[2]/td[3]")).getText().trim());
  }

  public int getUnstableJobsCount(){
    return Integer.valueOf(getTable().findElement(By.xpath(".//tbody/tr[3]/td[3]")).getText().trim());
  }

  public int getSuccessJobsCount(){
    return Integer.valueOf(getTable().findElement(By.xpath(".//tbody/tr[4]/td[3]")).getText().trim());
  }

}
