package org.jenkinsci.test.acceptance;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Context {
    public List<String> ate = new ArrayList<String>();
}
