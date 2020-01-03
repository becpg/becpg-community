import { Injectable } from '@angular/core';
import { Entity } from '../model/Entity';
import { EntityList } from '../model/EntityList';
import { EntityReport } from '../model/EntityReport';
import { EntityListColumn } from '../model/EntityListColumn';
import { EntityListItem } from '../model/EntityListItem';
import { EntityListPageResults } from '../model/EntityListPageResults';
import { AlfrescoApiService } from '@alfresco/adf-core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import beCPGModel from '../../../assets/becpg/datalist-columns.json';

@Injectable({
  providedIn: 'root'
})
export class EntityApiService {



  constructor(private apiService: AlfrescoApiService, private http: HttpClient) { }

  getEntity(id: string): Entity {

    const entity = new Entity();

    this.apiService.getInstance().webScript.executeWebScript('GET', 'becpg/entitylists/node/' + id.replace(':/', '')).then(
      (data) => {

        entity.name = data.entity.name;
        entity.id = data.entity.nodeRef.replace('workspace://SpacesStore/','');
        entity.parentId = data.entity.parentNodeRef;
        entity.datalists = [];
        //TODO user access

        for (const i in data.datalists) {
          if (Object.prototype.hasOwnProperty.call(data.datalists, i)) {
            const list = new EntityList();
            list.description = data.datalists[i].description;
            list.nodeType = data.datalists[i].itemType;
            list.name = data.datalists[i].name.replace('View-','');
            list.state = data.datalists[i].state;
            list.title = data.datalists[i].title;
            list.id = data.datalists[i].nodeRef;
            //TODO permissions
            entity.datalists.push(list);
          }
        }

      }, (error) => {
        console.log('Error' + error);
      });



    return entity;
  }



  getEntityListItems(entity: Entity, list: EntityList, columns: EntityListColumn[]): Promise<EntityListPageResults>{
    return new Promise((success) => {
   
    const colFields: string[] = [];

    for (const col in columns) {
      if (Object.prototype.hasOwnProperty.call(columns, col)) {
        colFields.push(columns[col].name);
      }
    }


    console.log(colFields);

    this.apiService.getInstance().webScript.executeWebScript('POST', 'becpg/entity/datalists/data/node/' + list.id.replace(':/', ''), {
      entityNodeRef: entity.id,
      dataListName: list.name,
      itemType: list.nodeType,
      site: null, //TODO
      repo: true, //TODO
      pageSize: 50
    }, null, null,
      {
        fields: colFields,
        page: 1,
        extraParams: null,
        filter:
          { filterOwner: null, filterId: 'all', filterData: '', filterParams: null }
      }

    ).then(
      (data) => {
        const pageResults = new EntityListPageResults();

        pageResults.parentId = data.metadata.parent.nodeRef;
        pageResults.pageSize = data.metadata.pageSize;
        pageResults.queryExecutionId = data.metadata.queryExecutionId;
        pageResults.startIndex = data.startIndex;
        pageResults.totalRecords = data.totalRecords;
        pageResults.columns = columns;
        pageResults.items = [];


        for (const i in data.items) {
          if (Object.prototype.hasOwnProperty.call(data.items, i)) {
            const item = new EntityListItem();
            item.itemData = data.items[i].itemData;
            item.id = data.items[i].nodeRef;
            pageResults.items.push(item);
          }
        }

         success(pageResults);

      }, (error) => {
        console.log('Error' + error);
      });

    });
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

  getVisibleColumns(list: EntityList): Promise<EntityListColumn[]> {
    return new Promise((success) => {


      const fields: string[] = [];
      const forcedFields: string[] = [];

      if (beCPGModel.datagrids[list.nodeType] &&
        beCPGModel.datagrids[list.nodeType]['default']) {
        for (const i in beCPGModel.datagrids[list.nodeType]['default']) {
          if (Object.prototype.hasOwnProperty.call(beCPGModel.datagrids[list.nodeType]['default'], i)) {
            const column = new EntityListColumn();
            fields.push(i);
            if (beCPGModel.datagrids[list.nodeType]['default'][i].forced) {
              forcedFields.push(i);
            }
          }
        }
      }

      this.apiService.getInstance().webScript.executeWebScript('POST', 'api/formdefinitions', null, null, null, {
        itemKind: 'type',
        itemId: list.nodeType,
        fields: fields,
        force: forcedFields
      }
      ).then(
        (ret) => {

          const columns: EntityListColumn[] = [];

          if (ret.data.definition.fields) {
            for (const i in ret.data.definition.fields) {
              if (Object.prototype.hasOwnProperty.call(ret.data.definition.fields, i)) {
                const column = new EntityListColumn();
                column.type = ret.data.definition.fields[i].type;
                column.label = ret.data.definition.fields[i].label;
                column.dataType = ret.data.definition.fields[i].dataType;
                column.fieldName = ret.data.definition.fields[i].dataKeyName;
                column.name = ret.data.definition.fields[i].name.replace(':', '_');
                columns.push(column);
              }

            }
          }
          success(columns);

        }
      );

    });



  }




}
