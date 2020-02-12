import { Component, OnInit, ViewChild } from '@angular/core';
import { NgxPhoneSelectDirective } from 'ngx-phone-select';
import { User } from 'src/app/shared/models/User';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MyErrorStateMatcher } from 'src/app/auth/errorStateMatcher';
import { AuthService } from 'src/app/auth/auth.service';
@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  constructor(private authService: AuthService, private formBuilder: FormBuilder) { }

  // convenience getter for easy access to form fields
  get userFormControls() { return this.userForm.controls; }
  isFr: boolean;
  selectedLanguage: string;
  user = new User();
  userToUpdate = new User();
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
  // Forms
  userForm: FormGroup;
  pwdForm: FormGroup;
  matcher = new MyErrorStateMatcher();
  // Variables for Front messages
  show: boolean;
  updateSuccess: boolean;
  exists: boolean;
  error: boolean;
  validPhone = true;
  countryCode: string;
  validPhoneNumber = true;
  hideOldPassword: boolean;
  pwdUpdated = false;
  formLocked = true;

  @ViewChild('phoneSelect', { static: false }) phoneSelect: NgxPhoneSelectDirective;

  ngOnInit() {

    this.userForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', [Validators.required]],
      organization: ['',],
      phone: ['', Validators.required]
    });

    this.pwdForm = this.formBuilder.group({
      oldPassword: ['', Validators.required],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required],
    }, { validator: this.checkPasswords });

    this.oldPassword = '';
    this.getUser();
  }


  checkPasswords(group: FormGroup) { // here we have the 'passwords' group
    if (group) {
      const pass = group.controls.password.value;
      const confirmPass = group.controls.confirmPassword.value;
      if (!pass && !confirmPass) {
        return null;
      }
      return (pass === confirmPass) ? null : { notSame: true };
    }
  }

  updateProfile() {
    if (this.formLocked) {
      this.formLocked = false;
    } else {
      // Add the Country Code For the Phone Number
      //   // if (this.user.phone && (this.user.phone.toString().indexOf(0 + this.phoneSelect.getCountryData().dialCode) !== 0)) {
      //   // this.user.phone = 0 + this.phoneSelect.getCountryData().dialCode + this.user.phone;
      // }
      if (this.phoneSelect.isValidNumber() === false) {
        this.validPhoneNumber = false;
        // Stop The Submit If the Phone Number Is Invalid
        return;
      } else {
        this.validPhoneNumber = true;
      }
      // if (this.confirmPassword !== this.user.password) {
      //   this.error = true;
      //   return;
      // }
      this.error = false;
      this.exists = false;
      this.updateSuccess = false;
      // send service to check name
      const phoneNumber = this.user.phone;
      const userToSave = Object.assign({}, this.user);
      userToSave.phone = this.phoneSelect.getCountryData().iso2 + '#' + this.phoneSelect.getCountryData().dialCode + '#' + this.user.phone;
      this.authService.updateProfile(userToSave).subscribe(res => {
        if (res) {
          this.exists = false;
          this.updateSuccess = true;
          this.confirmPassword = '';
          this.show = false;
          res.phone = phoneNumber;
          this.user = res;
          this.formLocked = true;
        }
      },
        err => {
          this.exists = true;
          this.updateSuccess = false;
        }
      );
    }
  }

  getUser() {
    this.authService.getUser(localStorage.getItem('username')).subscribe(res => {
      this.user = res;
      if (this.user.phone) {
        this.phoneSelect.setCountry(this.user.phone.substring(0, this.user.phone.indexOf('#')));
        this.user.phone = this.user.phone.substr(this.user.phone.lastIndexOf('#') + 1);
      }
    });
  }

  updatePassword() {
    this.authService.updatePassword(this.oldPassword, this.newPassword).subscribe(res => {
      if (res) {
        this.pwdUpdated = true;
        this.error = false;
      } else {
        this.pwdUpdated = false;
        this.error = true;
      }
    });
  }

  togglePwdSection() {
    this.show = !this.show;
    setTimeout(() => {
      window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }, 50);
  }

  unlockForm() {
    this.formLocked = false;
    this.userToUpdate = Object.assign({}, this.user);
    this.updateSuccess = false;
  }

  cancelUpdate() {
    this.formLocked = true;
    this.user = this.userToUpdate;
    this.updateSuccess = false;
  }

  log() {
    this.phoneSelect.isValidNumber();
  }
}
