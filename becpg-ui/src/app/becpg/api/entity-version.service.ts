import { Injectable } from '@angular/core';
import { AlfrescoApiService } from '@alfresco/adf-core';
import { BeCPGHelper } from '../utils/becpg-helper.';

@Injectable({
  providedIn: 'root'
})
export class EntityVersionService {

  constructor(private apiService: AlfrescoApiService){ }


  getEntityVersions(id: string){
    const nodeRef =  BeCPGHelper.NODE_REF_SPACE_PREFIX + id;
    return this.apiService.getInstance().webScript.executeWebScript('GET', 'becpg/api/entity-version?mode=branches&nodeRef=' + nodeRef,
            null, null, null, null);
  }

  getEntityHistory(id: string){
    const nodeRef =  BeCPGHelper.NODE_REF_SPACE_PREFIX + id;
    return this.apiService.getInstance().webScript.executeWebScript('GET', 'becpg/api/entity-version?mode=graph&nodeRef=' + nodeRef,
            null, null, null, null);
  }



}
