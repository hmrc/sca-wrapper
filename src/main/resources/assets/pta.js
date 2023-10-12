import AttorneyBanner from './components/attorney-banner/attorney-banner';

function initAll() {
  const attorneyBanners = document.querySelectorAll('[data-module="pta-attorney-banner"]');
  attorneyBanners.forEach((attorneyBanner) => {
    new AttorneyBanner(attorneyBanner).init();
  });
}

initAll();
