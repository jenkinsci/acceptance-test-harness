# Page Objects

To promote reuse between test cases, this project defines [Page Objects](http://code.google.com/p/selenium/wiki/PageObjects)
that cover the surface of Jenkins and its plugins.

The `PageObject` class is the main entry point of page objects.
It represents a specific URL of a web server and provides methods that interacts with this URL.

Jenkins exposes different domain objects at different URLs, and so our page objects generally follow this
server-side domain model structure, including inheritance and class name.

`ContainerPageObject` is a subtype of `PageObject` for domain models that spans several URLs, not just a single URL.
Most Jenkins domain objects fit in this category.
This abstraction provides support for actions, configuration, api, etc.

The top-level page object is `Jenkins`, and from there you'll see page objects that map to familiar
concepts like `Job` and `Build`.

Core page objects are located in the `org.jenkinsci.test.acceptance.po` package,
plugin specific page objects should be placed in `org.jenkinsci.test.acceptance.plugins.ARTIFACT_ID`.

## Implementing Page Objects
See [CapybaraPortingLayer.java](../src/main/java/org/jenkinsci/test/acceptance/po/CapybaraPortingLayer.java)
for various helper methods and instance fields you can use to write page objects.

This class defines a number of convenience methods that wraps WebDriver, such as this:

    // find a button whose text/ID/etc is "Start" and click it
    find(by.button("Start")).click()

The 'by' field defines a number of selector factory methods that simplify the code that selects
a specific element.


## Page Area
There's a `PageObject`-like class called `PageArea`, which is a micro page object
that maps to a section of a page that contains a series of INPUT controls.

A typical usage of this is to map a single <code>config.jelly</code> of a builder/publisher/etc.

Page area object uses [Form element path plugin](https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin)
to refer to input controls relative to its location in the hierarchy of controls.
Controls are mapped to `Control` instances by their relative path name, which maps to the "field" attribute
of the &lt;f:entry> tag on the sever side:

    // this annotation associates the text showin the drop-menu down to the page area it inserts
    // this maps to BuildStepDescriptor.getDisplayName() on the server-side
    @BuildStepPageObject("Invoke Ant")
    public class AntBuildStep extends BuildStep {
        // Ant configuration page defines two INPUT elements, so you bind them each to controls
        public final Control targets = control("targets");
        public final Control antName = control("antName");

        // parent represents the job URL your control is in, and
        // path represents the portion in the configuration page this build step is at
        public AntBuildStep(Job parent, String path) {
            super(parent, path);
        }
    }

    // how to use this
    Job job = ...
    AntBuildStep ant = job.addBuildStep(AntBuildStep.class);
    ant.antName.set(name);

## Control
Controls are like `WebElement`s, but they are late-binding. That is, a `Control` instance gets created
long before the actual element may appear in HTML. This allows page objects to define form fields as public
final instance variables on the `PageObject` subtype, and hide the details of how to select those elements.

This pattern can be seen most often in `PageArea` subtypes, as they are used in the form-heavy configuration pages.

`Control` also offers a richer set of methods to interact with form elements, making it easier to write correct code.
