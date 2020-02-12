import { Component, OnInit } from '@angular/core';
import { APPCONFIG } from '../../config';
import * as authActions from './../../store/authentication/auth.actions';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';
import { AuthService } from 'src/app/auth/auth.service';
import { Store } from '@ngrx/store';
import { AuthState } from 'src/app/store/authentication/auth.state';
import { User } from 'src/app/shared/models/User';


@Component({
  selector: 'app-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html'
})

export class AppHeaderComponent implements OnInit {
  public AppConfig: any;
  isFr: boolean;
  selectedLanguage: string;

  firstName: string;
  lastName: string;

  constructor(private appTranslateService: AppTranslateService, private authService: AuthService, private store: Store<AuthState>) {

  }

  ngOnInit() {
    this.AppConfig = APPCONFIG;
    // Set i18n Language
    if (localStorage.getItem('grizzly-lang')) {
      this.setLang(localStorage.getItem('grizzly-lang').toLowerCase());
    } else {
      this.setLang(navigator.language);
    }

    this.store.select('auth').subscribe(res => {
      if (localStorage.getItem('firstName') && localStorage.getItem('lastName')) {
        this.firstName = localStorage.getItem('firstName');
        this.lastName = localStorage.getItem('lastName');
      } else {
        this.store.dispatch(new authActions.LoginUser(localStorage.getItem('username')));
      }
    });
  }

  setLang(lang: string) {

    this.appTranslateService.setDefaultLang(lang);
    localStorage.setItem('grizzly-lang', lang);

    if (lang.includes('fr')) {
      this.selectedLanguage = 'FR';
      this.isFr = false;
    } else {
      this.selectedLanguage = 'EN';
      this.isFr = true;
    }

  }

  logout() {
    this.authService.logout();
  }
}
