package cucumber.runtime;

import cucumber.runtime.java.ObjectFactory;
import org.jenkinsci.test.acceptance.guice.World;

/**
 * Takes over cucumber object instantiation by Guice to incorporating
 * the cloudbees-extensibility component world.
 *
 * @author Kohsuke Kawaguchi
 */
public class ObjectFactoryImpl implements ObjectFactory {
    private final World world = World.get();

    /**
     * This is the constructor automatically instantiated by Cucumber.
     */
    public ObjectFactoryImpl() {
    }

    public <T> T getInstance(Class<T> type) {
        return world.getInjector().getInstance(type);
    }

    public void addClass(Class<?> glue) {
        // we let people to put these classes into the right scope and let auto-binder find them
    }

    public void start() {
        world.startTestScope("unknown");
    }

    public void stop() {
        world.endTestScope();
    }
}
