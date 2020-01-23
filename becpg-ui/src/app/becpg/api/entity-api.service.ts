import { Injectable } from '@angular/core';
import { Entity } from '../model/Entity';
import { EntityList } from '../model/EntityList';
import { EntityReport } from '../model/EntityReport';
import { EntityListColumn } from '../model/EntityListColumn';
import { EntityListItem } from '../model/EntityListItem';
import { EntityListPageResults } from '../model/EntityListPageResults';
import { AlfrescoApiService, AppConfigService } from '@alfresco/adf-core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, from } from 'rxjs';

import beCPGModel from '../../../assets/becpg/datalist-columns.json';
import { BeCPGHelper } from '../utils/becpg-helper.';
import { EntityLink } from '../model/EntityLink';

@Injectable({
  providedIn: 'root'
})
export class EntityApiService {


  constructor(private apiService: AlfrescoApiService,
    private appConfig: AppConfigService,
    private http: HttpClient) {
  }

  getEntity(id: string): Observable<Entity> {
    return from(
      this.apiService.getInstance().webScript.executeWebScript('GET', 'becpg/entitylists/node/' + id.replace(':/', '')).then(
        (data) => {
          const entity = new Entity();
          entity.name = data.entity.name;
          entity.id = data.entity.nodeRef.replace('workspace://SpacesStore/', '');
          entity.parentId = data.entity.parentNodeRef;
          entity.datalists = [];
          //TODO user access

          for (const i in data.datalists) {
            if (Object.prototype.hasOwnProperty.call(data.datalists, i)) {
              const list = new EntityList();
              list.description = data.datalists[i].description;
              list.nodeType = data.datalists[i].itemType;
              list.name = data.datalists[i].name.replace('View-', '');
              list.state = data.datalists[i].state;
              list.title = data.datalists[i].title;
              list.id = data.datalists[i].nodeRef;
              //TODO permissions
              entity.datalists.push(list);
            }
          }

          return entity;
        }
      )
    );
  }


  getRecents(id: string, viewId: string): Observable<EntityLink[]> {

    const params = {};
    if (id != null) {
      params['entityNodeRef'] = BeCPGHelper.NODE_REF_SPACE_PREFIX + id;
    }

    if (viewId != null) {
      params['list'] = viewId;
    }

    return from(
      this.apiService.getInstance().webScript.executeWebScript('GET', 'becpg/dockbar', params).then(
        (data) => {
          const entities: EntityLink[] = [];

          for (let i = 0; i < data.items.length; i++) {
            const item = data.items[i];

            entities.push({
              'name': item.displayName,
              'nodeType': item.itemType,
              'id': item.nodeRef.replace(BeCPGHelper.NODE_REF_SPACE_PREFIX, ''),
              'viewId': item.list
            });

          }

          return entities;

        }));
  }


  getEntityLogoUrl(nodeId: string): string {

    const timeStamp = (new Date()).getTime();
    return this.apiService.getInstance().contentClient.host + '/alfresco/service/api/node/workspace/SpacesStore/' + nodeId +
    '/content/thumbnails/doclib?c=queue&ph=true&lastModified=' +
     timeStamp +
     this.apiService.getInstance().contentClient.getAlfTicket(null);

  }

  uploadEntityLogo(logoToUpload: File, entityId: string): Observable<any> {
    const nodeRef = 'workspace://SpacesStore/' + entityId;
    const ticket = this.apiService.getInstance().getTicketEcm();
    const entityLogoEndpoint = this.apiService.getInstance().contentClient.host + '/alfresco/service/becpg/entity/uploadlogo';
    const formData: FormData = new FormData();
    formData.append('filedata', logoToUpload, logoToUpload.name);
    formData.append('updateNodeRef', nodeRef);
    let headers = new HttpHeaders({
      'Autorization': ticket
    });

    return this.http.post<any>(entityLogoEndpoint, formData, {
      headers: headers,
      reportProgress: true,
      observe: 'events'
    });
    /*
    * Otherwise call native alfresco-js-api */
    // return this.apiService.getInstance().contentClient.callApi('/alfresco/service/becpg/entity/uploadlogo', 'POST', {}, {}, {
    //   headers: headers,
    //   reportProgress: true,
    //   observe: 'events'
    // }, formData, formData, ['multipart/form-data'], ['application/json'], null, null, 'application/json');
    


  }



  getEntityReports(entity: Entity): EntityReport[] {
    const reports: EntityReport[] = [];
    //TODO

    // //private final static String CONTENT_DOWNLOAD_API_URL = "becpg/report/node/content/{0}/{1}/{2}/{3}?entityNodeRef={4}";

    // this.apiService.getInstance().webScript.executeWebScript('GET', 'module/entity-datagrid/config/columns').then(
    //   (data) => {

    //     for (const i in data) {
    //       if (Object.prototype.hasOwnProperty.call(data, i)) {
    //         const column = new EntityListColumn();
    //         column.type = data[i].type;
    //         column.label = data[i].label;
    //         column.dataType = data[i].dataType;
    //         reports.push(column);
    //       }
    //     }

    //   }, (error) => {
    //     console.log('Error' + error);
    //   });



    return reports;

  }




}
