package cucumber.runtime.java;

import cucumber.runtime.Glue;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.guice.GuiceFactory;
import org.jenkinsci.test.acceptance.StepDefinition;
import org.jvnet.hudson.annotation_indexer.Index;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class JavaBackend2 extends JavaBackend {
    private final ClassLoader classLoader;

    public JavaBackend2(ResourceLoader resourceLoader, ClassLoader classLoader) throws IOException {
        super(new GuiceFactory(), new ResourceLoaderClassFinder(resourceLoader,classLoader));
        this.classLoader = classLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        super.loadGlue(glue, gluePaths);
        try {
            for (Class c : Index.list(StepDefinition.class, classLoader, Class.class)) {
                for (Method method : c.getMethods()) {
                    Annotation a = hasCukeStepAnnotation(method);
                    if (a!=null)
                        addStepDefinition(a, method);
                }
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private Annotation hasCukeStepAnnotation(Method m) {
        for (Annotation a : m.getAnnotations()) {
            if (a.annotationType().getAnnotation(StepDefAnnotation.class)!=null)
                return a;
        }
        return null;
    }
}
