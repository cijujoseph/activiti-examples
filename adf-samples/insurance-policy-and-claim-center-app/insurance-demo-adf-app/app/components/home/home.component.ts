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

import { BpmUserService } from 'ng2-alfresco-userinfo';
import { BpmUserModel } from 'ng2-alfresco-userinfo/src/models/bpm-user.model';
import { environment } from '../../../environments/environment';

@Component({
    selector: 'home-view',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent {

    bpmUser: BpmUserModel;
    adminUser: boolean = false;

    constructor(private bpmUserService: BpmUserService) {
    }

    ngOnInit() {

        this.bpmUserService.getCurrentUserInfo()
            .subscribe((res) => {
                this.bpmUser = <BpmUserModel>res;
                if ((this.bpmUser.groups.filter(group => group.name === environment.adminGroupName && group.type === 1 && group.status === 'active').length) === 1) {
                    this.adminUser = true;
                }
            });
    }
}
