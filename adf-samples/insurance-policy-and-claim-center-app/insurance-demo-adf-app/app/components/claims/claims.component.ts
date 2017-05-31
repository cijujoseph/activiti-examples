
import { Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs/Rx';
import { StorageService } from 'ng2-alfresco-core';
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
import { ClaimModelESResponse } from './claim.model';
import { ClaimService } from './claim.service';

declare let __moduleName: string;
declare let dialogPolyfill: any;
@Component({
    selector: 'app-claims-component',
    templateUrl: './claims.component.html',
    styleUrls: ['./claims.component.css'],
    providers: [ClaimService, DocumentListService, DocumentActionsService, FolderActionsService]
})
export class ClaimsComponent implements OnInit {

    adminUser: boolean = false;

    claims: ClaimModelESResponse[];
    options: Map<string, boolean> = new Map<string, boolean>();

    contextActionHandler: Subject<any> = new Subject();
    data: DataTableAdapter;
    filteredData: ClaimModelESResponse[];
    actions: ContentActionModel[] = [];

    private defaultSchemaColumn: any[] = [
        { type: 'text', key: '_source.policyId', title: 'Policy ID', sortable: true },
        { type: 'text', key: '_source.claimId', title: 'Claim ID', sortable: true },
        { type: 'text', key: '_source.customerName', title: 'Customer Name', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.dateOfIncident', title: 'Incident Date', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.eyeWitnesses', title: 'Eye Witnesses', sortable: true, cssClass: 'desktop-only' },
        { type: 'text', key: '_source.claimStatus', title: 'Claim Status', sortable: true, cssClass: 'desktop-only' }
    ];

    constructor(private claimService: ClaimService,
        private router: Router,
        private storage: StorageService) {

    }
    ngOnInit() {
        let queryParam = '';
        this.adminUser = this.storage.getItem('adminUser') === 'true';
        if (!this.adminUser) {
            queryParam = '&q=customerId:' + this.storage.getItem('externalId');
        }
       
        this.claimService.getClaims(queryParam).subscribe(
            (res: ClaimModelESResponse[]) => {
                this.claims = res;
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
            this.claims,
            this.defaultSchemaColumn
        );
    }
    onRowClick(event: DataRowEvent) {
    }

    claimDetails(event: any) {
        this.router.navigate(['/claimdetails', event.value.obj._id]);
    }
}
