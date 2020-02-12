import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'i18nDate'
})
export class I18nDatePipe extends DatePipe implements PipeTransform {

  constructor(translateService: TranslateService) {
    super(translateService.currentLang);
  }

}
