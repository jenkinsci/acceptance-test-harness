// Disable smooth scrolling so scrolling is instantaneous
document.querySelector("html").style.scrollBehavior = "initial";

// Disable common sticky elements
const header = document.getElementById("page-header");
const bottomAppBar = document.getElementById("bottom-sticker");
// https://github.com/jenkinsci/jenkins/commit/6481e78d20a0c689859058da5a029489e8b5072c introduced a shadow on a different div!?
const bottomShadow = document.querySelector(".jenkins-bottom-app-bar__shadow")

document.querySelectorAll(".bottom-sticker-inner")
  .forEach(element => {
      // there can be multiple bottom stickers (e.g. in a dialog) but there's no class on the actual element
      const bottomSticker = element.parentNode
      if (bottomSticker) {
          bottomSticker.style.position = "relative";
      }
  })

document.querySelectorAll(".jenkins-bottom-app-bar__shadow")
  .forEach(element => {
    element.style.position = "initial";
  })

if (header) {
    header.style.position = "relative";
}

if (bottomAppBar) {
    bottomAppBar.style.position = "relative";
}

if (bottomShadow) {
    bottomShadow.style.position = "relative";
}
