var css = '* { overflow: visible !important }',
    head = document.head || document.getElementsByTagName('head')[0],
    style = document.createElement('style');

style.type = 'text/css';
style.id = 'ath-overflow-override'
if (style.styleSheet){
  style.styleSheet.cssText = css;
} else {
  style.appendChild(document.createTextNode(css));
}

head.appendChild(style);
