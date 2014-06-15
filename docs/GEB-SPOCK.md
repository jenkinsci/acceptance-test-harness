# Writing tests with Geb and Spock
This harness allows to write tests in [Groovy](http://groovy.codehaus.org/) with [Geb](http://www.gebish.org/) and [Spockframework](http://www.spockframework.org/). 

Here are only the specifics described they are needed to be compatible with the existing Java infrastructure and for a successful initialization of tests. Please see the documentation of [Groovy](http://groovy.codehaus.org/), [Geb](http://www.gebish.org/) and [Spockframework](http://www.spockframework.org/) for further information about writing the tests.

## Geb PageObjects
Page objects written with Geb are very simple. All Geb page objects must extends the `org.jenkinsci.test.acceptance.po.Page` class. This class takes care about the correct initialization the PageObject. Geb PageObjects are actually independent from the existing Java infrastructure and can not be used as arguments for i.e. build steps.

To make a page object compatible with the existing infrastructure, simply add a new instance variable of the favored class/interface and add the `@Delegate` annotation. Of course it must be initialized, either inline or within the constructor.


Here is an example how to write a Geb page object that can be used as a PostBuildStep within every JUnit test.
```groovy
import org.jenkinsci.test.acceptance.geb.proxies.PostBuildStepProxy
import org.jenkinsci.test.acceptance.po.Describable
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.Page
import org.jenkinsci.test.acceptance.po.PostBuildStep

@Describable("Some post build step")
class SomePostBuildStep extends Page {

    static content = {
        area { $("div[descriptorid='hudson.plugins.example.ExamplePublisher']") }
        execPattern { area.find("input[path='/publisher/execPattern']") }
    }

    @Delegate
    PostBuildStep parent;

    SomePostBuildStep(Job job, String path) {
        super(job.injector)
        parent = new PostBuildStepProxy(job, path)
    }
}
```

Within the content closure are all WebElements you wan't to access from the test. The parent field annotated with @Delegate take care that this page object is valid PostBuildStep and can be used within the [Job Class](../src/main/java/org/jenkinsci/test/acceptance/po/Job.java) as build step. This field must be initialized with a valid instance of the PostBuildStep interface.

## Examples
### PageObjects
Examples for Geb PageObjects can be found for the [Jacoco Plugin](../src/main/groovy/org/jenkinsci/test/acceptance/plugins/jacoco/) and the [Jenkins internal user management](../src/main/groovy/org/jenkinsci/test/acceptance/po). 
### Tests using Spock
Examples for tests they using Spock can be found under [src/test/groovy/groovy](../src/test/groovy/groovy/).