import { Component, OnInit } from '@angular/core';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss']
})
export class NotFoundComponent implements OnInit {
  isFr: boolean;
  selectedLanguage: string;

  constructor(private appTranslateService: AppTranslateService) { }

  ngOnInit() {
    // Set i18n Language
    if (localStorage.getItem('grizzly-lang')) {
      this.setLang(localStorage.getItem('grizzly-lang'));
    } else {
      this.setLang(navigator.language);
    }
  }

  setLang(lang: string) {

    this.appTranslateService.setDefaultLang(lang);

    if (lang.includes('fr')) {
      this.selectedLanguage = 'FR';
      this.isFr = false;
    } else {
      this.selectedLanguage = 'EN';
      this.isFr = true;
    }

  }
}
