package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.guice.GuiceFactory;
import org.jvnet.hudson.annotation_indexer.Index;
import org.kohsuke.cukes.StepDefFinder;

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
     * Find cucumber annotated step definitions and hook through index
     */
    @Override
    public void loadGlue(cucumber.runtime.Glue glue, List<String> gluePaths) {
        super.loadGlue(glue, gluePaths);
        try {

            for (Method method : StepDefFinder.list(classLoader)) {
                for (Annotation a : method.getAnnotations()) {
                    if (a.annotationType().isAnnotationPresent(StepDefAnnotation.class)) {
                        addStepDefinition(a, method);
                    }
                }
            }

            for (Method method : Index.list(Before.class, classLoader, Method.class)) {
                addHook(method.getAnnotation(Before.class), method);
            }

            for (Method method : Index.list(After.class, classLoader, Method.class)) {
                addHook(method.getAnnotation(After.class), method);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
