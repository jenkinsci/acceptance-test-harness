package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 *
 * Created by larca on 25.04.17.
 */
public class LogParserOutputPage extends PageObject{

    public enum LOGPARSERFRAME{
        SIDEBAR("sidebar"), CONTENT("content");
        private String framename;
        LOGPARSERFRAME(String framename){ this.framename = framename; }
        public String getFramename(){ return this.framename; }
    }

    public LogParserOutputPage(PageObject po) {
        super(po.injector, po.url(""));
    }


    private String restoreURL = null;
    public void openFrameInWindow(LOGPARSERFRAME frame){
        switchToMainframe();
        WebElement e = driver.findElement(By.xpath("//frame[@name='" + frame.getFramename() + "']"));
        restoreURL = driver.getCurrentUrl();
        driver.navigate().to(e.getAttribute("src"));
    }
    public void restoreWindow(){
        if(restoreURL != null){
            driver.navigate().to(restoreURL);
            restoreURL = null;
        }
    }


    public boolean switchToFrame(LOGPARSERFRAME frame){
        switchToMainframe();
        try{
            WebElement e = driver.findElement(By.xpath("//frame[@name='" + frame.getFramename() + "']"));
            driver.switchTo().frame(e);
        }
        catch(Exception ex){
            return false;
        }
        return true;
    }

    public void switchToDefaultContent(){
        driver.switchTo().defaultContent();
    }

    private boolean switchToMainframe(){
        try {
            WebElement e = driver.findElement(By.xpath("//div[@id='main-panel']//table//tbody//tr//td//iframe"));
            driver.switchTo().frame(e);
        }catch(Exception ex){
            return false;
        }

        return true;
    }
}
