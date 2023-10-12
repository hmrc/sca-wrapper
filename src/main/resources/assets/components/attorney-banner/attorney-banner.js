// This code is based on original code from assets-frontend: https://github.com/hmrc/assets-frontend/blob/main/assets/javascripts/modules/attorneyBanner.js
import getWindowTop from './get-window-top';
import getElementRectangle from './get-element-rectangle';

const bannerPlaceholderClass = 'pta-attorney-banner__placeholder';
const bannerFixedModifierClass = 'pta-attorney-banner--fixed';

function insertPlaceholder(module, originalRectangle) {
  const placeholder = document.createElement('div');
  placeholder.className = bannerPlaceholderClass;
  placeholder.style.width = `${originalRectangle.width}px`;
  placeholder.style.height = `${originalRectangle.height}px`;
  placeholder.style.display = 'none';
  module.parentNode.insertBefore(placeholder, module);
  return placeholder;
}

function fixBanner(module, placeholder) {
  module.classList.add(bannerFixedModifierClass);
  // eslint-disable-next-line no-param-reassign
  placeholder.style.display = 'block';
}

function unfixBanner(module, placeholder) {
  module.classList.remove(bannerFixedModifierClass);
  // eslint-disable-next-line no-param-reassign
  placeholder.style.display = 'none';
}

function AttorneyBanner($module) {
  this.$module = $module;
}

AttorneyBanner.prototype.init = function init() {
  const originalRectangle = getElementRectangle(this.$module);

  // A placeholder takes up the space left by the fixed banner
  // Note, this doesn't cater for situations where the browser is re-sized after
  // this component has been initialised.
  const placeholder = insertPlaceholder(this.$module, originalRectangle);

  document.addEventListener('scroll', () => {
    if (getWindowTop() > originalRectangle.top) {
      fixBanner(this.$module, placeholder);
    } else {
      unfixBanner(this.$module, placeholder);
    }
  });
};

export default AttorneyBanner;
