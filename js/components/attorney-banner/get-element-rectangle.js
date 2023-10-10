/* istanbul ignore file */
import getWindowTop from './get-window-top';

function getElementRectangle(element) {
  const rect = element.getBoundingClientRect();
  const style = element.currentStyle || window.getComputedStyle(element);

  return {
    top: rect.top + getWindowTop(),
    width: rect.width,
    height: rect.height + parseInt(style.marginTop, 10) + parseInt(style.marginBottom, 10),
  };
}

export default getElementRectangle;
