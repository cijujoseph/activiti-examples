/* * * ./app/comments/services/comment.service.ts * * */
// Imports
import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { ClaimModelESResponse } from './claim.model';
import { Observable } from 'rxjs/Rx';

import { environment } from '../../../environments/environment';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ClaimService {
    private esUrlBase;
    private claimEventUri;
    private claimsUrl;
    // Resolve HTTP using the constructor
    constructor(private http: Http) {

        this.esUrlBase = environment.elasticsearchUrl;
        this.claimEventUri = '/insuranceindex/claimevent/';
        this.claimsUrl = this.esUrlBase + this.claimEventUri + '_search?pretty=true';
    }


    getClaims(queryParam: string): Observable<ClaimModelESResponse[]> {
        return this.http.get(this.claimsUrl + queryParam)
            .map((res: Response) => res.json().hits.hits)
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));

    }

    getClaimDetails(id: string): Observable<ClaimModelESResponse> {
        let claimDetailsUrl = this.esUrlBase + this.claimEventUri + id;
        return this.http.get(claimDetailsUrl)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));

    }

}
