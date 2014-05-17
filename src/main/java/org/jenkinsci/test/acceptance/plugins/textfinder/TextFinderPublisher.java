package org.jenkinsci.test.acceptance.plugins.textfinder;

import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Created by Martin Ende on 5/17/14.
 */


public class TextFinderPublisher extends PostBuildStep {

    public TextFinderPublisher(Job parent, String path) {
        super(parent, path);
    }

}
