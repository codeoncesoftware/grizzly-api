import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { User } from '../shared/models/User';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  baseUrl: string = environment.baseUrl;

  constructor(private http: HttpClient, private router: Router, private translateService: TranslateService) { }

  login(userObj) {
    return this.http.post<string>(this.baseUrl + '/api/auth/login', userObj, { responseType: 'json' });
  }

  logout() {
    this.http.get<boolean>(this.baseUrl + '/api/auth/logout').subscribe(res => {
      const lang =  localStorage.getItem('grizzly-lang');
      localStorage.clear();
      // Keep selected lang
      if (lang) {
        localStorage.setItem('grizzly-lang', lang);
      }
      this.router.navigate(['/login']);
    });
  }

  getUser(username): Observable<User> {
    return this.http.get<User>(this.baseUrl + '/api/user/' + username);
  }

  updateProfile(user): Observable<User> {
    return this.http.put<User>(this.baseUrl + '/api/user/update' , user);
  }

  updatePassword(oldPwd, newPwd) {
    return this.http.put(this.baseUrl + '/api/user/update/pwd', {}, {params: {oldPwd, newPwd}});
  }

}
