import { Injectable } from '@angular/core';
import { Entity } from '../model/Entity';
import { EntityList } from '../model/EntityList';
import { EntityListColumn } from '../model/EntityListColumn';
import { EntityListItem } from '../model/EntityListItem';
import { EntityListPageResults } from '../model/EntityListPageResults';
import { AlfrescoApiService } from '@alfresco/adf-core';
import { Observable, from } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EntityDatagridService {

  constructor(private apiService: AlfrescoApiService) {

  }


  getVisibleColumns(itemType: string, formId: string): Observable<EntityListColumn[]> {

    if (formId == null) {
      formId = 'datagrid';
    }

    return from(this.apiService.getInstance()
      .webScript.executeWebScript('GET', `becpg/form?itemKind=type&itemId=${itemType}&formId=${formId}`, null, null, null, null)
      .then(ret => {
        const columns: EntityListColumn[] = [];

        if (ret.fields) {
          for (const i in ret.fields) {
            if (Object.prototype.hasOwnProperty.call(ret.fields, i)) {
              const column = new EntityListColumn();
              column.type = ret.fields[i].type;
              column.label = ret.fields[i].name;
              // column.dataType = ret.fields[i].dataType;
              // column.fieldName = ret.fields[i].id;
              column.name = ret.fields[i].id;
              columns.push(column);
            }

          }

          return columns;
        }
      }));

  }


  getEntityListItems(entity: Entity, list: EntityList, columns: EntityListColumn[], itemType: string): Observable<EntityListPageResults> {
    const colFields: string[] = [];

    for (const col in columns) {
      if (Object.prototype.hasOwnProperty.call(columns, col)) {
        colFields.push(columns[col].name.replace('prop_', '').replace('assoc_', ''));
      }
    }


    return from(this.apiService.getInstance()
      .webScript.executeWebScript('POST', 'becpg/entity/datalists/data/node/' + list.id.replace(':/', ''), {
        entityNodeRef: 'workspace://SpacesStore/' + entity.id,
        dataListName: list.name,
        itemType: itemType,
        site: entity.siteId,
        repo: (entity.siteId == null),
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
          pageResults.columns = columns.filter( col => col.label !== 'hidden');
          pageResults.items = [];


          for (const i in data.items) {
            if (Object.prototype.hasOwnProperty.call(data.items, i)) {
              const item = new EntityListItem();
              item.itemData = data.items[i].itemData;
              item.id = data.items[i].nodeRef;
              pageResults.items.push(item);
            }
          }

          return pageResults;

        }
      ));

  }


}
