package org.jenkinsci.test.acceptance.guice;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.jenkinsci.groovy.binder.GroovyWiringModule;

import java.net.URL;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyWiringModule2 extends GroovyWiringModule {
    public GroovyWiringModule2(URL... scripts) {
        super(scripts);
    }

    public GroovyWiringModule2(Collection<URL> scripts) {
        super(scripts);
    }

    @Override
    protected CompilerConfiguration configureCompiler() {
        CompilerConfiguration cc = super.configureCompiler();
        cc.setScriptBaseClass(AdditionalBinderDsl.class.getName());
        return cc;
    }
}
