import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
    providedIn: 'root'
})
export class AppTranslateService {

    constructor(private translateService: TranslateService) {

    }

    setDefaultLang(lang: string) {
        let defaultLang = 'en';
        if (lang.toLowerCase().includes('fr')) {
            defaultLang = 'fr';
        }
        this.translateService.setDefaultLang(defaultLang);
    }

    getMessage(key: string) {
        return this.translateService.instant(key);
    }

}
