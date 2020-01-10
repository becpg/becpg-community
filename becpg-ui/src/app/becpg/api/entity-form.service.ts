import { Injectable } from '@angular/core';
import {
    EcmModelService, NodeService, WidgetVisibilityService,
    FormService, FormRenderingService, FormBaseComponent, FormOutcomeModel,
    FormEvent, FormErrorEvent, FormFieldModel, FormFieldOption,
    FormModel, FormOutcomeEvent, FormValues, ContentLinkModel
} from '@alfresco/adf-core';
import { Observable, of, Subject, from } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { AlfrescoApiService } from '@alfresco/adf-core';

@Injectable({
    providedIn: 'root'
})
export class EntityFormService {
    saveFormData(form: FormModel, data: FormValues) {
        throw new Error("Method not implemented.");
    }


    constructor(protected formService: FormService,
        protected visibilityService: WidgetVisibilityService,
        protected ecmModelService: EcmModelService,
        protected nodeService: NodeService,
        private apiService: AlfrescoApiService) {

    }

    loadForm(formId: string, itemId: string, isModel: boolean): Observable<FormModel> {

        let itemKind = isModel ? 'type' : 'node';

        if(!isModel){
            itemId = 'workspace://SpacesStore/'+itemId;
        }


         return from ( this.apiService.getInstance()
         .webScript.executeWebScript('GET', 'becpg/form?itemKind='+itemKind+'&itemId='+itemId+'&formId='+formId, null, null, null, null)
         .then(ret => {
               return new FormModel(ret);
         }));

    }



}
