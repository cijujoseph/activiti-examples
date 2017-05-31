/* * * ./app/comments/services/comment.service.ts * * */
// Imports
import { Injectable } from '@angular/core';
import { Http, Response} from '@angular/http';
import { PolicyModelESResponse } from './policy.model';
import { Observable } from 'rxjs/Rx';

import { environment } from '../../../environments/environment';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class PolicyService {
    private esUrlBase;
    private policyEventUri;
    private policiesUrl;
    // Resolve HTTP using the constructor
    constructor(private http: Http) {
        this.esUrlBase = environment.elasticsearchUrl;
        this.policyEventUri = '/insuranceindex/policyevent/';
        this.policiesUrl = this.esUrlBase + this.policyEventUri + '_search?pretty=true';
    }

    getPolicies(queryParam: string): Observable<PolicyModelESResponse[]> {
        return this.http.get(this.policiesUrl + queryParam)
            .map((res: Response) => res.json().hits.hits)
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));

    }

    getPolicyDetails(id: string): Observable<PolicyModelESResponse> {
        let policyDetailsUrl = this.esUrlBase + this.policyEventUri + id;
        return this.http.get(policyDetailsUrl)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));

    }

}
