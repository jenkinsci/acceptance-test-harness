# Guice is our glue

We use Guice to wire up components and test execution settings. This is because:

  * We need to support multiple test frameworks (JUnit & Cucumber) and didn't want it to glue components together
  * People who run tests can configure how they run tests by directly manipulating Guice bindings
    (see [WIRING.md](WIRING.md))


## Scopes
We define two [scopes](http://code.google.com/p/google-guice/wiki/Scopes) in this project.

  * `@Singleton`, which spans the entire JVM life time.
  * `@TestScope`, which spans the execution of a single test case.

Singleton scope has a `WorldCleaner` component you can inject to schedule any clean-up work that
gets run when the test harness shuts down. Test scope has a corresponding `TestCleaner` for this purpose.

    // say you have a test-scoped object or prototype-scoped object
    // that wants to run something at the end of test
    @TestScope
    class MyComponent implements Closeable {
        // when Guice creates your component, ask for TestCleaner
        @Inject
        public MyComponent(TestCleaner cleaner) {
            cleaner.addTask(this); // schedule the clean up at the end
        }

        // this gets called when a test ends
        public void close() {
            ...
        }
    }


## Defining extension point and extensions
[CloudBees extensibility API](https://github.com/cloudbees/extensibility-api) allows you to define additional
test related components in another Maven project, and have them participate in the Guice world.

For example, `JenkinsController` is a fairly generic contract that allows many different implementations,
and so we designate [JenkinsControllerFactory](../src/main/java/org/jenkinsci/test/acceptance/JenkinsController.java)
as an `@ExtensionPoint`, then various implementations put `@Extension` in its factory implementations,
and that's how we choose the right `JenkinsController` implementation when the user specifies `TYPE=tomcat`.

These extension implementations are discovered at runtime, so they can be even defined in separate Maven projects.

## Defining additional Guice modules and bindings
[CloudBees extensibility API](https://github.com/cloudbees/extensibility-api) also allows you to define
your Guice Module object to add more bindings.

To do so, implement a marker `ExtensionModule` interface and put `@Extension` on your module implementation.

    @Extension
    public class MyModule extends AbstractModule implements ExtensionModule {
        protected void configure() {
            install(new ModuleA());
            install(new ModuleB());
            bind(Foo.class).to(Bar.class);
            ...
        }
    }
