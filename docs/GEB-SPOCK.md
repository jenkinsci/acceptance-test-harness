# Writing tests with Geb and Spock
This harness allows to write tests in [Groovy](http://groovy.codehaus.org/) with [Geb](http://www.gebish.org/) and [Spockframework](http://www.spockframework.org/). 

Here are only the specifics described that are needed to be compatible with the existing Java infrastructure and for a successful initialization of tests. Please see the documentation of [Groovy](http://groovy.codehaus.org/), [Geb](http://www.gebish.org/) and [Spockframework](http://www.spockframework.org/) for further information about writing the tests.

## Geb PageObjects
Page objects written with Geb are very simple. All Geb page objects must extends the `org.jenkinsci.test.acceptance.po.Page` class. This class takes care about the correct initialization of the PageObject. Geb PageObjects are actually independent from the existing Java infrastructure and can not be used as arguments for i.e. build steps.

This page objects are actually not compatible with the existing java infrastructure.
To make a page object compatible with the existing infrastructure, simply add a new instance variable of the favored class/interface to which the page object should be compatible and add the `@Delegate` annotation. It must be initialized, either inline or within the constructor.


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

Within the content closure are all WebElements you want to access from the test. The parent field annotated with @Delegate takes care that this page object is a valid PostBuildStep and can be used within the [Job Class](../src/main/java/org/jenkinsci/test/acceptance/po/Job.java) as build step. This field must be initialized with a valid instance of the PostBuildStep interface.
## Tests with Spock
Writing tests with the Spock Framework is very similar with writing a new JUnit test. All Spock tests have to extend the `org.jenkinsci.test.acceptance.geb.GebSpec` class that initialize the test environment.

Here a short example how to write a Spock test. This example is taken from [Jenkins' user management](../src/test/groovy/groovy/core/InternalUsersTest.groovy).
```groovy
import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.po.*

class InternalUsersTest extends GebSpec {

    def "Create, update and delete user"() {
        given: "Use the internal user authentification"
        to SecurityConfiguration
        useSecurity.value(true)
        securityRealm.jenkinsDB.click()
        submit.click()

        when: "create a new user"
        to AddUserPage
        fillUserInfo()
        signUp.click()

        then: "should be on user list"
        at UserListPage
        assert userNames.size() == 1
        ...
    }
}
```
## Examples
### PageObjects
Examples for Geb PageObjects can be found for the [Jacoco Plugin](../src/main/groovy/org/jenkinsci/test/acceptance/plugins/jacoco/) and the [Jenkins internal user management](../src/main/groovy/org/jenkinsci/test/acceptance/po/users/). 
### Tests using Spock
Examples for tests that using Spock can be found under [src/test/groovy/groovy](../src/test/groovy/groovy/).

There are two interesting examples. First the tests for the [Jenkins' user management](../src/test/groovy/groovy/core/InternalUsersTest.groovy), which is written with plain Geb, Spock and without any existing Java support. Second the [Jacoco Plugin](../src/test/groovy/groovy/plugins/JacocoPluginTest.groovy) test, which is a Spock test that mixes the existing PageObjects (i.e. the Jenkins, and the Job-Object) with a PostBuildStep written with Geb. 