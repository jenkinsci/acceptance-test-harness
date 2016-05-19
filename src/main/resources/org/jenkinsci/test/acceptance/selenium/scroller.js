// Visually navigate to the element in order to interact with it.

var eYCoord = arguments[0];

// Scroll to the element. It will appear at the top edge of the screen.
// We subtract a bit so as to accommodate fixed position banners at the top
// (e.g. breadcrumbs, tabbars etc), making sure they are not hiding the element.
window.scrollTo(0, eYCoord - 200);