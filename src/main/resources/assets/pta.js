function getWindowTop() {
  return window.pageYOffset || document.documentElement.scrollTop;
}

function getElementRectangle(element) {
  const rect = element.getBoundingClientRect();
  const style = element.currentStyle || window.getComputedStyle(element);

  return {
    top: rect.top + getWindowTop(),
    width: rect.width,
    height: rect.height + parseInt(style.marginTop, 10) + parseInt(style.marginBottom, 10),
  };
}

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

function initAll() {
  const attorneyBanners = document.querySelectorAll('[data-module="pta-attorney-banner"]');
  attorneyBanners.forEach((attorneyBanner) => {
    new AttorneyBanner(attorneyBanner).init();
  });
}

initAll();

