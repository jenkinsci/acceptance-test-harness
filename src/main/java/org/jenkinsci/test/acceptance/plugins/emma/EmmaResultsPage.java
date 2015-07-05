/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.plugins.emma;

import org.jenkinsci.test.acceptance.po.PageObject;
import com.google.inject.Injector;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object for Emma coverage report page.
 * @author Orjan Percy
 */
public class EmmaResultsPage extends PageObject {

    public static int NO_RESULT_CELLS = 8;
    public EmmaResultsPage(Injector injector, URL url) {
        super(injector, url);
    }

    /*
     * Verify the Emma coverage report.
     */
    public void assertHasResult(List<String> correct) {
        open();
        find(by.link("Coverage Report")).click();

        // Extract the result data and verify.
        String content = find(by.id("main-panel")).getText();
        Pattern p = Pattern.compile("\\d+(?:[,.]\\d+)");
        Matcher m = p.matcher(content);
        List<String> l = new ArrayList<String>(NO_RESULT_CELLS);
        while (m.find()) {
            l.add(m.group());
        }
        assert(l.equals(correct));
    }
}
