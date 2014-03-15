# Page Objects

To promote reuse between test cases, this project defines [Page Objects](http://code.google.com/p/selenium/wiki/PageObjects)
that covers the surface of Jenkins and its plugins.

`PageObject` class is the main entry point of page objects. It represents a specific URL of a web server
and provides methods that interacts with this URL.

Jenkins exposes different domain objects at different URLs, and so our page objects generally follow this
server-side domain model structure, including inheritance and class name.

`ContainerPageObject` is a subtype of `PageObject` for domain models that spans several URLs, not just a single URL.
Most Jenkins domain objects fit in this category.

The top-level page object is `Jenkins`, and from there you'll see page objects that map to familiar
concepts like `Job` and `Build`.


## Page Area
There's a subtype of `PageObject` called `PageArea`, which is a micro page object
that maps to a section of a page that contains a series of INPUT controls.

A typical usage of this is to map a single <tt>config.jelly</tt> of a builder/publisher/etc
