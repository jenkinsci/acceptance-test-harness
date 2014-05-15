// Visually navigate to the element in order to interact with it.
// Elements without path attribute are ignored

var e = arguments[0];

if (e.getAttribute("path")) {
    // Scroll to the element. It will appear at the top edge of the screen.
    e.scrollIntoView();
    // Scroll a bit back so breadcrumbs are not hiding the element.
    window.scrollBy(0, -40);
}
