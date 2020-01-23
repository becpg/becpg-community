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


  renderers = {};


  constructor(private apiService: AlfrescoApiService) {
      // /**
			//  * Person
			//  */
      // this.registerRenderer("cm:person", function(oRecord, data, label, scope) {
      //   return '<span class="person">' + $userProfile(data.metadata, data.displayValue) + '</span>';
      // });

      // /**
      // * Boolean
      // */
      // this.registerRenderer("boolean", function(oRecord, data, label, scope) {
      //   var booleanValueTrue = scope.msg("data.boolean.true");
      //   var booleanValueFalse = scope.msg("data.boolean.false");
      //   return data.value == true ? booleanValueTrue : booleanValueFalse;
      // });

      // /**
      // * Authority container
      // */
      // this.registerRenderer("cm:authoritycontainer", function(oRecord, data, label, scope) {
      //   return '<span class="userGroup">' + $html(data.displayValue) + '</span>';
      // });

      // /**
      // * Content
      // */
      // this.registerRenderer([ "cm:content", "cm:cmobject", "cm:folder" ], function(oRecord, data, label, scope) {
      //   var url = scope._buildCellUrl(data);
      //   var html = '<a href="' + url + '">';
      //   html += '<img src="'
      //         + Alfresco.constants.URL_RESCONTEXT
      //         + 'components/images/filetypes/'
      //         + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null),
      //               16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue)
      //         + '" />';
      //   html += ' ' + $html(data.displayValue) + '</a>';
      //   return html;
      // });
      
      // this.registerRenderer([ "content_cm:content"], function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
      //  var nodeRef = new Alfresco.util.NodeRef(oRecord.getData("nodeRef"));

      //  oColumn.width = 100;

      //  Dom.setStyle(elCell, "width", oColumn.width + "px");
      //  Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
     

      //  return '<span class="thumbnail"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri
      //  + '/content/thumbnails/doclib?c=queue&ph=true&timestamp='+(new Date())+'"  /></span>';
       
      // });


  }



  registerRenderer(propertyName: any , renderer : any) : boolean {
    
      if (propertyName instanceof Array) {
        for ( var i in propertyName) {
          this.renderers[propertyName[i]] = renderer;
        }

      } else {
        this.renderers[propertyName] = renderer;
      }
      return true;
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
              column.hidden = ret.fields[i].name === 'hidden' ;
              column.label = column.hidden ? '' : ret.fields[i].name;
              // column.dataType = ret.fields[i].dataType;
              // column.fieldName = ret.fields[i].id;
              column.name = ret.fields[i].id;
              column.sortable  = (column.type === 'property');

              column.options = ret.fields[i].options;
              column.required = ret.fields[i].required;

              // column.sortable =  true;
              // column.filter = true;
              // column.filterMatchMode = 'contains';
              // column.allowToggle= true;
              // column.style= { 'width': '200px', 'vertical-align': 'top' };


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


  sort() {
    // var url = me.options.sortUrl + "/" + dstData.nodeRef
    // .replace(":/", "") + "?selectedNodeRefs=" + srcData.nodeRef;


  }






}
