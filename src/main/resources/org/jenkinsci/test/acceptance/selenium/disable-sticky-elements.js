// Disable smooth scrolling so scrolling is instantaneous
document.querySelector("html").style.scrollBehavior = "initial";

// Disable common sticky elements
const breadcrumbBar = document.querySelector("#breadcrumbBar");
const bottomAppBar = document.querySelector("#bottom-sticker");

if (breadcrumbBar) {
    breadcrumbBar.style.position = "relative";
}

if (bottomAppBar) {
    bottomAppBar.style.position = "relative";
}
