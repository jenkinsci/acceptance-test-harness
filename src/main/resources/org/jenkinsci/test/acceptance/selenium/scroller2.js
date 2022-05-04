// Disable smooth scrolling so scrolling is instantaneous
document.querySelector("html").style.scrollBehavior = "initial"
document.querySelector("html").style.background = "red"
document.querySelector("body").style.background = "blue"

// Disable common sticky elements
const breadcrumbBar = document.querySelector("#breadcrumbBar");
const bottomAppBar = document.querySelector("#bottom-sticker");

if (breadcrumbBar) {
    breadcrumbBar.style.position = "relative";
}

if (bottomAppBar) {
    bottomAppBar.style.position = "relative";
}
