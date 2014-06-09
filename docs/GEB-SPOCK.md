# Writing tests with Geb and Spock
This harness allows to write tests in [Groovy](http://groovy.codehaus.org/) with [Geb](http://www.gebish.org/) and [Spockframework](http://www.spockframework.org/). 

## Geb PageObjects
Page objects written with Geb are very simple. Here is an example how to write a Geb page object that can be used as a PostBuildStep within every JUnit test.
```groovy
import org.jenkinsci.test.acceptance.geb.proxies.PostBuildStepProxy
import org.jenkinsci.test.acceptance.po.Describable
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.Page
import org.jenkinsci.test.acceptance.po.PostBuildStep

@Describable("Some post build step")
class SomePostBuildStep extends Page {

    static content = {
        area { $("div[descriptorid='hudson.plugins.jacoco.JacocoPublisher']") }
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
As you can see it extends the Page class. This class take care for correctly initialization of selenium webdriver. Within the content closure are all WebElements you wan't to access from the test. The parent field annotated with @Delegate take care that this page object is valid PostBuildStep and can be used within the [Job Class](../src/main/java/org/jenkinsci/test/acceptance/po/Job.java) as build step. This field must be initialized with a valid instance of the PostBuildStep interface.