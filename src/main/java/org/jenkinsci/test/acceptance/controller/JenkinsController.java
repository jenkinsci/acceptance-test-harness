package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import org.codehaus.groovy.util.StringUtil;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.jenkinsci.test.acceptance.ControllerException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Vivek Pandey
 */
@ExtensionPoint
public abstract class JenkinsController {
    protected static final String WORKSPACE = System.getenv("WORKSPACE") != null? System.getenv("WORKSPACE") : System.getProperty("user.dir");

    public abstract void start();

    public abstract void stop();

    public abstract URL getUrl();

    public abstract void diagnose();

    public abstract void tearDown();

    protected String downloadPathElement(){
        String source = "http://updates.jenkins-ci.org/latest/form-element-path.hpi";
        String target =  WORKSPACE+ File.separator + "path-element.hpi";
        if(!FileUtils.fileExists(target)){
            FileWriter fileWriter = null;
            HttpURLConnection uc = null;
            try {
                fileWriter = new FileWriter(target);
                uc = (HttpURLConnection) new URL(target).openConnection();
                IOUtil.copy(uc.getInputStream(), fileWriter);
            } catch (FileNotFoundException e) {
                throw new ControllerException(String.format("Failed to open %s for write operation", target), e);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileWriter != null){
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        throw new ControllerException(String.format("Failed to close %s after writing",target),e);
                    }
                }
            }
        }
    }




}
