/*!
 * @license
 * Copyright 2016 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component } from '@angular/core';
import { Router, NavigationStart } from '@angular/router';
import { ActivitiTaskListService, TaskQueryRequestRepresentationModel } from 'ng2-activiti-tasklist';


import {
  AlfrescoTranslationService,
  AlfrescoAuthenticationService,
  AlfrescoSettingsService,
  StorageService,
  LogService
} from 'ng2-alfresco-core';

import { BpmUserService } from 'ng2-alfresco-userinfo';
import { BpmUserModel } from 'ng2-alfresco-userinfo/src/models/bpm-user.model';
import { environment } from '../environments/environment';
declare var document: any;

@Component({
  selector: 'alfresco-app',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  searchTerm: string = '';
  taskCount: number = 0;
  taskListRequestNode: TaskQueryRequestRepresentationModel;
  bpmUser: BpmUserModel;
  adminUser: boolean = false;
  ecmHost: string = environment.ecmUrl;
  bpmHost: string = environment.bpmUrl;
  platforms: string = environment.providers;

  constructor(public authService: AlfrescoAuthenticationService,
    public router: Router,
    public alfrescoSettingsService: AlfrescoSettingsService,
    private translate: AlfrescoTranslationService,
    private storage: StorageService,
    private logService: LogService,
    public taskListService: ActivitiTaskListService,
    private bpmUserService: BpmUserService) {
    this.setEcmHost();
    this.setBpmHost();
    this.setProvider();

    if (translate) {
      translate.addTranslationFolder('app', 'resources');
    }
  }

  isAPageWithHeaderBar(): boolean {
    return location.pathname === '/login' || location.pathname === '/settings';
  }

  ngOnInit() {

    this.taskListRequestNode = this.createTaskListRequestNode();
    this.router.events.subscribe((events) => {
      if (events instanceof NavigationStart) {
        if (events.url !== '/login' && events.url !== '/') {
          this.load(this.taskListRequestNode);

          if (this.storage.hasItem('adminUser')) {
            this.adminUser = this.storage.getItem('adminUser') === 'true';

          } else {
            this.bpmUserService.getCurrentUserInfo()
              .subscribe((res) => {
                this.bpmUser = <BpmUserModel>res;
                
                if (this.bpmUser.externalId) {
                  this.storage.setItem('externalId', this.bpmUser.externalId);
                } else {
                  this.storage.setItem('externalId', this.bpmUser.email);
                }
                if ((this.bpmUser.groups.filter(group => group.name === 'admin-group' && group.type === 1 && group.status === 'active').length) === 1) {
                  this.storage.setItem('adminUser', 'true');
                  this.adminUser = true;
                } else {
                  this.storage.setItem('adminUser', 'false');
                  this.adminUser = false;
                }
              });
          }
        }
      }
    });
  }

  onLogout(event) {
    event.preventDefault();
    if (this.storage.hasItem('adminUser')) {
      this.storage.removeItem('adminUser');
      this.storage.removeItem('externalId');
    }
    this.authService.logout()
      .subscribe(
      () => {
        this.navigateToLogin();
      },
      (error: any) => {
        if (error && error.response && error.response.status === 401) {
          this.navigateToLogin();
        } else {
          this.logService.error('An unknown error occurred while logging out', error);
          this.navigateToLogin();
        }
      }
      );
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
    this.hideDrawer();
  }


  onToggleSearch(event) {
    let expandedHeaderClass = 'header-search-expanded',
      header = document.querySelector('header');
    if (event.expanded) {
      header.classList.add(expandedHeaderClass);
    } else {
      header.classList.remove(expandedHeaderClass);
    }
  }


  changeLanguage(lang: string) {
    this.translate.use(lang);
    this.hideDrawer();
  }

  hideDrawer() {
    // todo: workaround for drawer closing
    // document.querySelector('.mdl-layout').MaterialLayout.toggleDrawer();
    // Ciju - a better solution to toggle drawer from https://github.com/google/material-design-lite/issues/1246.
    // The above one was popping up the drawer when I put logout on top panel
    document.querySelector('.mdl-layout__drawer').addEventListener('click', function () {
      document.querySelector('.mdl-layout__obfuscator').classList.remove('is-visible');
      this.classList.remove('is-visible');
    }, false);
  }

  private setEcmHost() {
    if (this.storage.hasItem(`ecmHost`)) {
      this.alfrescoSettingsService.ecmHost = this.storage.getItem(`ecmHost`);
      this.ecmHost = this.storage.getItem(`ecmHost`);
    } else {
      this.alfrescoSettingsService.ecmHost = this.ecmHost;
    }
  }

  private setBpmHost() {
    if (this.storage.hasItem(`bpmHost`)) {
      this.alfrescoSettingsService.bpmHost = this.storage.getItem(`bpmHost`);
      this.bpmHost = this.storage.getItem(`bpmHost`);
    } else {
      this.alfrescoSettingsService.bpmHost = this.bpmHost;
    }
  }

  private setProvider() {
    if (this.storage.hasItem(`providers`)) {
      this.alfrescoSettingsService.setProviders(this.storage.getItem(`providers`));
    }
  }

  private load(requestNode: TaskQueryRequestRepresentationModel) {
    this.taskListService.getTotalTasks(requestNode).subscribe(
      (res) => {
        this.taskCount = res.total;
      }, (err) => {
        this.logService.error(err);
      });
  }
  private createTaskListRequestNode() {
    let requestNode = {
      assignment: 'assignee',
      state: 'open'
    };
    return new TaskQueryRequestRepresentationModel(requestNode);
  }
}
