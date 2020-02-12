import { I18nDatePipe } from './i18n-date.pipe';

describe('I18nDatePipe', () => {
  it('create an instance', () => {
    const pipe = new I18nDatePipe(null);
    expect(pipe).toBeTruthy();
  });
});
