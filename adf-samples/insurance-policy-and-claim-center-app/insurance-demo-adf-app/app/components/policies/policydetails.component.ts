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

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { AlfrescoAuthenticationService, AlfrescoSettingsService, AlfrescoApiService } from 'ng2-alfresco-core';
import { DocumentListService, DocumentListComponent } from 'ng2-alfresco-documentlist';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { PolicyModelESResponse } from './policy.model';
import { PolicyService } from './policy.service';

import { environment } from '../../../environments/environment';

// declare let __moduleName: string;
// declare let AlfrescoApi: any;

@Component({
    selector: 'policy-details',
    templateUrl: './policydetails.component.html',
    styleUrls: ['./policydetails.component.css'],
    providers: [PolicyService]
})
export class PolicyDetailsComponent implements OnInit, OnDestroy {

    sub: Subscription;
    policyDetails: any = {};
    policyId: string;
    policyNodeId: string;
    policyDetailsFormId: string = environment.policyDetailsFormId;

    fileNodeId: string;
    fileShowed: boolean = false;
    isContentsAvailable: boolean = false;

    @ViewChild(DocumentListComponent)
    documentList: DocumentListComponent;

    ticket: string = localStorage.getItem('ticket-ECM');

    constructor(private route: ActivatedRoute,
        private router: Router,
        private apiService: AlfrescoApiService,
        private authService: AlfrescoAuthenticationService,
        public alfrescoSettingsService: AlfrescoSettingsService,
        private policyService: PolicyService,
        private documentListService: DocumentListService) {
    }

    ngOnInit() {
        this.sub = this.route.params.subscribe(params => {
            this.retrivePolicyDetails(params['id']);
            this.policyId = params['id'];
        });
        if (environment.providers === 'ALL') {
            this.apiService.getInstance().nodes.getNodeChildren(environment.insuranceDocumentsRootNodeId, {}).then(data => {
                let policyNode = data.list.entries.filter(item => item.entry.name === this.policyId);
                if (policyNode.length > 0) {
                    this.policyNodeId = policyNode[0].entry.id;
                    this.isContentsAvailable = true;
                }
            });
        }

    }

    ngOnDestroy() {
        if (this.sub) {
            this.sub.unsubscribe();
        }
    }

    backToPolicyList() {
        this.router.navigate(['/policies']);
    }
    startNewClaim() {
        this.router.navigate(['/start/new-claim'], { queryParams: { 'policyId': this.policyId } });
    }
    showFile(event) {
        if (event.value.entry.isFile) {
            this.fileNodeId = event.value.entry.id;
            this.fileShowed = true;
        } else {
            this.fileShowed = false;
        }
    }
    private retrivePolicyDetails(policyId: string): void {

        this.policyService.getPolicyDetails(policyId).subscribe(
            (res: PolicyModelESResponse) => {
                this.policyDetails = res._source;
                console.log(this.policyDetails);
            },
            (err) => {
                console.log(err);
            }
        );
    }
}
