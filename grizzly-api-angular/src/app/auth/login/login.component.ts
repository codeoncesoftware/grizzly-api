import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import * as authActions from './../../store/authentication/auth.actions';
import { catchError } from 'rxjs/operators';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';
import { Store } from '@ngrx/store';
import { AuthState } from 'src/app/store/authentication/auth.state';

@Component({
  selector: 'app-login',
  styleUrls: ['./login.component.scss'],
  templateUrl: './login.component.html'
})

export class LoginComponent implements OnInit {
  userObj = { username: '', password: '' };
  token: string;
  errorMessage: string;
  username = '';
  pass = '';
  logged = true;
  error: boolean;
  // Reset Password Section
  usernameToSendResetPass: string;
  resetPasswordBool = false;
  successfulReset = false;
  resetErrorMessage = false;

  isFr: boolean;
  selectedLanguage: string;

  constructor(private authService: AuthService, private store: Store<AuthState>, private router: Router, private appTranslateService: AppTranslateService) {
  }

  ngOnInit() {
    // Set i18n Language
    if (localStorage.getItem('grizzly-lang')) {
      this.setLang(localStorage.getItem('grizzly-lang').toLowerCase());
    } else {
      this.setLang(navigator.language);
    }
  }


  setLang(lang: string) {

    this.appTranslateService.setDefaultLang(lang);
    localStorage.setItem('grizzly-lang', lang.toLowerCase());

    if (lang.includes('fr')) {
      this.selectedLanguage = 'FR';
      this.isFr = false;
    } else {
      this.selectedLanguage = 'EN';
      this.isFr = true;
    }

  }

  login() {
    localStorage.clear();
    this.userObj.username = this.username;
    this.userObj.password = this.pass;
    this.error = false;
    if (this.username && this.pass) {
      this.authService.login(this.userObj).subscribe(res => {
        if (res) {
          this.logged = true;
          // Save selected language
          localStorage.setItem('grizzly-lang', this.selectedLanguage.toLowerCase());
          // tslint:disable-next-line: no-string-literal
          localStorage.setItem('token', res['access_token']);
          localStorage.setItem('username', this.username);
          this.router.navigate(['/app/dashboard']);
        }
      },
        err => {
          if (err.status === 401) {
            if (err.error === 4011) {
              this.errorMessage = 'auth.signin.errors.credentials';
            } else {
              this.errorMessage = 'auth.signin.errors.validAccount';
            }
            this.logged = false;
          }
        }
      );
    } else {
      this.error = true;
    }

  }

}
