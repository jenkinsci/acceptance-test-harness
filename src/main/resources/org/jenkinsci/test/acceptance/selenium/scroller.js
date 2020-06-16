// Visually navigate to the element in order to interact with it.

var eYCoord = arguments[0];
var eXCoord = arguments[1];
var id = arguments[2];
var footer = document.getElementsByTagName("footer");
var sticker = document.getElementById("bottom-sticker");

// Scroll to the element. It will appear at the top edge of the screen.
// We subtract a bit so as to accommodate fixed position banners at the top
// (e.g. breadcrumbs, tabbars etc), making sure they are not hiding the element.
window.scrollTo(eXCoord - 100, eYCoord - 200);
if(sticker == null || !sticker.contains(document.getElementById(id)) || footer.length != 1){
  return true;
}
else{
  //it is sticker button and it is moved up (footer does not overlap)
  return (footer[0].getBoundingClientRect().top >= sticker.getBoundingClientRect().bottom)
}