package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;

import java.net.URL;

/**
 * PageObject for the Subversion "advanced" section.
 * Created by karl on 6/20/14.
 */
public class SubversionSvmAdvanced extends PageObject {
    public final Control excludedRegions = control(by.input("_.excludedRegions"));

    public SubversionSvmAdvanced(PageObject context, URL url) {
        super(context, url);
    }
}