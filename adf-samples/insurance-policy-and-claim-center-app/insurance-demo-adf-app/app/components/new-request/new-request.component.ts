import { Component, OnInit, AfterContentChecked, OnChanges, ViewChild, ElementRef } from '@angular/core';
import { FormRenderingService, ActivitiStartForm, FormService, FormEvent, FormModel } from 'ng2-activiti-form';
import { ActivitiProcessService} from 'ng2-activiti-processlist';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import { ClaimService } from '../claims/claim.service';
import { ClaimModelESResponse } from '../claims/claim.model';

import { PolicyService } from '../policies/policy.service';
import { PolicyModelESResponse } from '../policies/policy.model';

import { environment } from '../../../environments/environment';


@Component({
    providers: [ClaimService, PolicyService],
    selector: 'app-new-request',
    templateUrl: './new-request.component.html',
    styleUrls: ['./new-request.component.css']
})
export class NewRequestComponent implements OnInit, AfterContentChecked, OnChanges {

    processDefinitionId: string;
    appId: string = environment.processAppId;
    processDefKey: string;

    @ViewChild(ActivitiStartForm)
    startForm: ActivitiStartForm;

    el: ElementRef;

    policyId: string;

    sub: Subscription;

    name: string;

    errorMessageId: string = '';

    newpolicy: boolean = true;

    claimDetails: any = {};
    policyDetails: any = {};

    claimId: string;

    cloneRequest: boolean = false;

    constructor(private activitiProcess: ActivitiProcessService, public router: Router,
        private formRenderingService: FormRenderingService, private route: ActivatedRoute,
        private formService: FormService,
        private claimService: ClaimService,
        private policyService: PolicyService,
        el: ElementRef) {
        this.el = el;

        formService.formLoaded.subscribe((e: FormEvent) => {
            if (!this.newpolicy) {
                if (this.cloneRequest && this.claimId) {
                    this.populateClaimDataFromClaim(this.claimId, e.form);
                } else if (this.policyId) {
                    this.populateClaimDataFromPolicy(this.policyId, e.form);
                }
            }
        });
    }

    ngOnInit() {
        this.sub = this.route.params.subscribe(params => {

            if (params['processKey'] === 'new-policy') {
                this.processDefKey = 'CreateNewInsurancePolicy';
                this.newpolicy = true;

            } else if (params['processKey'] === 'new-claim') {
                this.processDefKey = 'InsuranceClaimProcess';
                this.newpolicy = false;
                this.sub = this.route
                    .queryParams
                    .subscribe(queryParams => {
                        this.policyId = queryParams['policyId'] || '';
                        this.claimId = queryParams['claimId'] || '';
                        this.cloneRequest = queryParams['cloneRequest'] || '';
                    });
            }
        });

        this.activitiProcess.getProcessDefinitions(this.appId)
            .flatMap(response => response)
            .filter((process) => process.key === this.processDefKey)
            .subscribe(response => {
                this.processDefinitionId = response.id;
            });
    }

    ngAfterContentChecked() {
        // A workaround to hide the nameless task
        if (this.startForm && this.el.nativeElement.querySelector('.mdl-card__title')) {
            this.startForm.showTitle = false;
            this.startForm.showSaveButton = false;
            // adding the following as the widget is still there even after setting showTitle=false !
            this.el.nativeElement.querySelector('.mdl-card__title').style.display = 'none';

        }

    }
    ngOnChanges() {

    }

    onFormLoaded(event: any) {
        console.log('loaded');
    }

    onSuccessProcessList(event: any) {
        console.log(event);
    }

    public onOutcomeClick(outcome: string) {
        // hardcoding name for this time
        this.name = 'New Request - ADF';
        this.startProcess(outcome);
    }

    private resetErrorMessage(): void {
        this.errorMessageId = '';
    }

    private populateClaimDataFromClaim(claimId: string, form: FormModel): void {

        this.claimService.getClaimDetails(claimId).subscribe(
            (res: ClaimModelESResponse) => {
                this.claimDetails = res._source;
                for (let field of form.getFormFields()) {
                    if (this.claimDetails[field.id]) {
                        field.json.value = this.claimDetails[field.id];
                        field.value = this.claimDetails[field.id];
                    }
                }
            },
            (err) => {
                console.log(err);
            }
        );
    }
    private populateClaimDataFromPolicy(policyId: string, form: FormModel): void {

        this.policyService.getPolicyDetails(policyId).subscribe(
            (res: PolicyModelESResponse) => {
                this.policyDetails = res._source;
                for (let field of form.getFormFields()) {
                    if (this.policyDetails[field.id]) {
                        field.json.value = this.policyDetails[field.id];
                        field.value = this.policyDetails[field.id];
                    }
                }
            },
            (err) => {
                console.log(err);
            }
        );
    }

    public startProcess(outcome?: string) {
        if (this.processDefinitionId && this.name) {
            this.resetErrorMessage();
            let formValues = this.startForm ? this.startForm.form.values : undefined;
            this.activitiProcess.startProcess(this.processDefinitionId, this.name, outcome, formValues).subscribe(
                (res) => {
                    this.name = '';
                    this.router.navigate(['/home']);
                },
                (err) => {
                    this.errorMessageId = 'START_PROCESS.ERROR.START';
                    console.log(err);
                }
            );
        }
    }

}


