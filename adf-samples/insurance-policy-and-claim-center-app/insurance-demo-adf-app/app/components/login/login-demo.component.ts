

import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Validators } from '@angular/forms';
import { StorageService, LogService } from 'ng2-alfresco-core';

import { BpmUserService } from 'ng2-alfresco-userinfo';
import { BpmUserModel } from 'ng2-alfresco-userinfo/src/models/bpm-user.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'login-demo',
  templateUrl: './login-demo.component.html',
  styleUrls: ['./login-demo.component.css']
})
export class LoginDemoComponent implements OnInit {

  @ViewChild('alfrescologin')
  alfrescologin: any;

  providers: string = environment.providers;
  blackListUsername: string;
  customValidation: any;

  disableCsrf: boolean = false;
  isECM: boolean = true;
  isBPM: boolean = false;
  customMinLenght: number = 2;
  bpmUser: BpmUserModel;

  constructor(private router: Router,
    private storage: StorageService,
    private bpmUserService: BpmUserService,
    private logService: LogService) {
    this.customValidation = {
      username: ['', Validators.compose([Validators.required, Validators.minLength(this.customMinLenght)])],
      password: ['', Validators.required]
    };
  }

  ngOnInit() {
    this.alfrescologin.addCustomValidationError('username', 'required', 'LOGIN.MESSAGES.USERNAME-REQUIRED');
    this.alfrescologin.addCustomValidationError('username', 'minlength', 'LOGIN.MESSAGES.USERNAME-MIN', { customMinLenght: this.customMinLenght });
    this.alfrescologin.addCustomValidationError('password', 'required', 'LOGIN.MESSAGES.PASSWORD-REQUIRED');

    if (this.storage.hasItem('providers')) {
      this.providers = this.storage.getItem('providers');
    }

    this.initProviders();
  }

  initProviders() {
    if (this.providers === 'BPM') {
      this.isECM = false;
      this.isBPM = true;
    } else if (this.providers === 'ECM') {
      this.isECM = true;
      this.isBPM = false;
    } else if (this.providers === 'ALL') {
      this.isECM = true;
      this.isBPM = true;
    }
  }

  onLogin($event) {
   
    this.bpmUserService.getCurrentUserInfo()
      .subscribe((res) => {
        this.bpmUser = <BpmUserModel>res;
        console.log(this.bpmUser);
        if (this.bpmUser.externalId) {
          this.storage.setItem('externalId', this.bpmUser.externalId);
        } else {
          this.storage.setItem('externalId', this.bpmUser.email);
        }
        if ((this.bpmUser.groups.filter(group => group.name === environment.adminGroupName && group.type === 1 && group.status === 'active').length) === 1) {
          this.storage.setItem('adminUser', 'true');
        } else {
          this.storage.setItem('adminUser', 'false');
        }
      });
    this.router.navigate(['/home']);
  }

  onError($event) {
    this.logService.error($event);
  }

  toggleECM() {
    this.isECM = !this.isECM;
    this.storage.setItem('providers', this.updateProvider());
  }

  toggleBPM() {
    this.isBPM = !this.isBPM;
    this.storage.setItem('providers', this.updateProvider());
  }

  toggleCSRF() {
    this.disableCsrf = !this.disableCsrf;
  }

  updateProvider() {
    if (this.isBPM && this.isECM) {
      this.providers = 'ALL';
      return this.providers;
    }

    if (this.isECM) {
      this.providers = 'ECM';
      return this.providers;
    }

    if (this.isBPM) {
      this.providers = 'BPM';
      return this.providers;
    }

    this.providers = '';
    return this.providers;
  }

  validateForm(event: any) {
    let values = event.values;
    if (values.controls['username'].value === this.blackListUsername) {
      this.alfrescologin.addCustomFormError('username', 'This particular username has been blocked');
      event.preventDefault();
    }
  }

}
