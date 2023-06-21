package org.jenkinsci.test.acceptance.plugins.subversion;

import java.net.URL;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * PageObject for the Subversion "advanced" section.
 *
 * @author Matthias Karl
 */
public class SubversionSvmAdvanced extends PageObject {
    public final Control excludedRegions = control(by.input("_.excludedRegions"));

    public SubversionSvmAdvanced(PageObject context, URL url) {
        super(context, url);
    }
}