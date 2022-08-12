package org.jenkinsci.test.acceptance.utils.groovy;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * {@link Script} that delegates access like {@link Closure} does.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ClosureScript extends Script {
    private Object delegate;
    private MetaClass delegateMetaClass;

    public ClosureScript() {
    }

    public ClosureScript(Binding binding) {
        super(binding);
    }

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
        this.delegateMetaClass = InvokerHelper.getMetaClass(delegate);
    }

    @Override
    public void setBinding(Binding binding) {
        super.setBinding(binding);
        setDelegate(binding.getProperty("delegate"));
    }

    @Override
    public Object getProperty(String property) {
        try {
            return delegateMetaClass.getProperty(delegate,property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    @Override
    public void setProperty(String property, Object newValue) {
        try {
            delegateMetaClass.setProperty(delegate,property,newValue);
        } catch (MissingPropertyException e) {
            super.setProperty(property, newValue);
        }
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return delegateMetaClass.invokeMethod(delegate, name, args);
        } catch (MissingMethodException e) {
            return super.invokeMethod(name,args);
        }
    }
}
