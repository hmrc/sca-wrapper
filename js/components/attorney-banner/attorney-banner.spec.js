import { fireEvent } from '@testing-library/dom';
import AttorneyBanner from './attorney-banner';
import * as calculateRectangle from './get-element-rectangle';
import * as getWindowTop from './get-window-top';

describe('attorney-banner', () => {
  beforeEach(() => {
    document.body.innerHTML = `
<div class="govuk-width-container">
    <div class="pta-attorney-banner " data-module="pta-attorney-banner">
        <div class="pta-attorney-banner__text">
            You are using this service for <span class="govuk-!-font-weight-bold">Joe Bloggs</span>.
        </div>
        <a href="#" class="govuk-link pta-attorney-banner__link">Return to your account</a>
    </div>
</div>
  `;
    jest.spyOn(calculateRectangle, 'default').mockReturnValue({
      top: 100,
      width: 960,
      height: 100,
    });
    jest.spyOn(getWindowTop, 'default').mockReturnValue(0);

    new AttorneyBanner(document.querySelector('[data-module=pta-attorney-banner]')).init();
  });

  it('should insert a placeholder', () => {
    expect(document.querySelector('.pta-attorney-banner__placeholder')).toBeTruthy();
  });

  it('should add the fixed class when appropriate', () => {
    expect(document.querySelector('.pta-attorney-banner--fixed')).toBeFalsy();

    jest.spyOn(getWindowTop, 'default').mockReturnValue(200);
    fireEvent.scroll(document);

    expect(document.querySelector('.pta-attorney-banner--fixed')).toBeTruthy();
  });

  it('should remove the fixed class when scrolled back up', () => {
    expect(document.querySelector('.pta-attorney-banner--fixed')).toBeFalsy();

    jest.spyOn(getWindowTop, 'default').mockReturnValue(200);
    fireEvent.scroll(document);
    expect(document.querySelector('.pta-attorney-banner--fixed')).toBeTruthy();

    jest.spyOn(getWindowTop, 'default').mockReturnValue(0);
    fireEvent.scroll(document);
    expect(document.querySelector('.pta-attorney-banner--fixed')).toBeFalsy();
  });
});
