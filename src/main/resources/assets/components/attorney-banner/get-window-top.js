/* istanbul ignore file */
function getWindowTop() {
  return window.pageYOffset || document.documentElement.scrollTop;
}

export default getWindowTop;
