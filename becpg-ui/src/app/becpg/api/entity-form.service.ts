import { Injectable } from '@angular/core';
import {
    EcmModelService, NodeService, WidgetVisibilityService,
    FormService, FormModel, FormValues
} from '@alfresco/adf-core';
import { Observable, from } from 'rxjs';
import { AlfrescoApiService } from '@alfresco/adf-core';

@Injectable({
    providedIn: 'root'
})
export class EntityFormService {


    constructor(protected formService: FormService,
        protected visibilityService: WidgetVisibilityService,
        protected ecmModelService: EcmModelService,
        protected nodeService: NodeService,
        private apiService: AlfrescoApiService) {

    }

    loadForm(formId: string, itemId: string, isModel: boolean): Observable<FormModel> {

        const itemKind = isModel ? 'type' : 'node';

        if (!isModel) {
            itemId = 'workspace://SpacesStore/' + itemId;
        }

        
        return from(this.apiService.getInstance()
            .webScript.executeWebScript('GET', `becpg/form?itemKind=${itemKind}&itemId=${itemId}&formId=${formId}`, null, null, null, null)
            .then(ret => {
                return new FormModel(ret);
            }));

    }

    saveFormData(form: FormModel, data: FormValues) {
        throw new Error("Method not implemented.");
    }



}
