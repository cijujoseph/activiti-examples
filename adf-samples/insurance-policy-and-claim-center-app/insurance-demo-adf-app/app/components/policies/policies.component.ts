
import { Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs/Rx';
import {
    ObjectDataTableAdapter,
    DataTableAdapter,
    DataRowEvent
} from 'ng2-alfresco-datatable';
import {
    ContentActionModel,
    DocumentListService,
    FolderActionsService,
    DocumentActionsService
} from 'ng2-alfresco-documentlist';
import {
    StorageService
} from 'ng2-alfresco-core';
import { PolicyModelESResponse } from './policy.model';
import { PolicyService } from './policy.service';

declare let __moduleName: string;
declare let dialogPolyfill: any;

@Component({
    selector: 'app-policies-component',
    templateUrl: './policies.component.html',
    styleUrls: ['./policies.component.css'],
    providers: [PolicyService, DocumentListService, DocumentActionsService, FolderActionsService]
})
export class PoliciesComponent implements OnInit {

    adminUser: boolean = false;
    policies: PolicyModelESResponse[];
    contextActionHandler: Subject<any> = new Subject();
    data: DataTableAdapter;
    actions: ContentActionModel[] = [];

    private defaultSchemaColumn: any[] = [
        { type: 'text', key: '_source.policyId', title: 'Policy ID', sortable: true },
        { type: 'text', key: '_source.customerName', title: 'Customer Name', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.insuranceType', title: 'Insurance Type', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.insuranceAmount', title: 'Insured Amount', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.startDate', title: 'Start Date', sortable: true }
    ];

    constructor(private policyService: PolicyService, private router: Router, private storage: StorageService) {

    }
    ngOnInit() {
        let queryParam = '';
        this.adminUser = this.storage.getItem('adminUser') === 'true';
        if (!this.adminUser) {
            queryParam = '&q=customerId:' + this.storage.getItem('externalId');
        }
        this.policyService.getPolicies(queryParam).subscribe(
            (res: PolicyModelESResponse[]) => {
                this.policies = res;
                this.data = this.initDefaultSchemaColumns();
            },
            (err) => {
                console.log(err);
            }
        );
    }
   /**
    * Return an initDefaultSchemaColumns instance with the default Schema Column
    * @returns {ObjectDataTableAdapter}
    */
    initDefaultSchemaColumns(): ObjectDataTableAdapter {
        return new ObjectDataTableAdapter(
            this.policies,
            this.defaultSchemaColumn
        );
    }
    onRowClick(event: DataRowEvent) {
    }
    policyDetails(event: any) {
        this.router.navigate(['/policydetails', event.value.obj._id]);
    }
}
