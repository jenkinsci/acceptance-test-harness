# Mix-in Page Objects
Sometimes, different Jenkins model objects have common traits.
For example, both Jenkins root object and views act as a container of jobs, and they offer a common set of operations, like creating/deleting a job,
starting a build.

In situations like this we want their corresponding page objects to share a common set of methods.
To promote this, the test harness defines them in a separate class that extends from a marker type called `MixIn`.

Mix-in page objects are just normal page objects that have the exact same URL as some other page objects.
The common convention is for the "primary" page object to keep them in its public final field like this:


    public class View extends ContainerPageObject {
        public final JobsMixIn jobs = new JobsMixIn(this);
        ...
    }

And the calling clients can use this like:

    View view = ...;
    FreeStyleProject f = view.jobs.create();

