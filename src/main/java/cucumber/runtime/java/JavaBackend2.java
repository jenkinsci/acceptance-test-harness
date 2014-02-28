package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.guice.GuiceFactory;
import org.jenkinsci.test.acceptance.Glue;
import org.jvnet.hudson.annotation_indexer.Index;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Hooks into Cucumber to load step definitions and hooks properly.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaBackend2 extends JavaBackend {
    private final ClassLoader classLoader;

    public JavaBackend2(ResourceLoader resourceLoader, ClassLoader classLoader) throws IOException {
        super(new GuiceFactory(), new ResourceLoaderClassFinder(resourceLoader,classLoader));
        this.classLoader = classLoader;
    }

    /**
     * Scan {@link Glue} classes, then add hooks and steps.
     */
    @Override
    public void loadGlue(cucumber.runtime.Glue glue, List<String> gluePaths) {
        super.loadGlue(glue, gluePaths);
        try {
            for (Class c : Index.list(Glue.class, classLoader, Class.class)) {
                for (Method method : c.getMethods()) {
                    for (Annotation a : method.getAnnotations()) {
                        if (a.annotationType().isAnnotationPresent(StepDefAnnotation.class)) {
                            addStepDefinition(a, method);
                            break;
                        }
                        if (a.annotationType()==Before.class || a.annotationType()==After.class) {
                            addHook(a, method);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
