import { Component, OnInit, Input, OnChanges } from '@angular/core';
import { LazyLoadEvent } from 'primeng';
import { EntityDatagridService } from '../../api/entity-datagrid.service';
import { Entity } from '../../model/Entity';
import { EntityList } from '../../model/EntityList';
import { EntityListColumn } from '../../model/EntityListColumn';
import { EntityListItem } from '../../model/EntityListItem';
import { EntityListPageResults } from '../../model/EntityListPageResults';
import { Observable, from } from 'rxjs';
import { StringifyOptions } from 'querystring';

@Component({
  selector: 'app-entity-datagrid',
  templateUrl: './entity-datagrid.component.html',
  styleUrls: ['./entity-datagrid.component.scss']
})
export class EntityDatagridComponent implements OnChanges, OnInit {

  pageResults: Observable<EntityListPageResults>;

  columns: Observable<EntityListColumn[]>;

  selectItems: EntityListItem[];

  @Input() list: EntityList;

  @Input() entity: Entity;

  @Input() formId: string;

  @Input() itemType: string;

  loading: boolean;

  constructor(private entityDatagridService: EntityDatagridService) { }

  ngOnInit() {

    this.loadColumns();

  }

  ngOnChanges() {
    this.loadColumns();
  }


  loadColumns() {

    if (this.entity != null && this.list != null) {
      this.loading = true;

      if (this.itemType == null) {
        this.itemType = this.list.nodeType;
      }

      this.columns = this.entityDatagridService.getVisibleColumns(this.itemType, this.formId);

      this.columns.subscribe(columns => {
        this.pageResults = this.entityDatagridService.getEntityListItems(this.entity, this.list, columns, this.itemType);
      });

      this.pageResults.subscribe(data => { this.loading = false; });
    }
  }

  onSort() {

  }


  // loadData(event: LazyLoadEvent) {
  //   //event.first = First row offset
  //   //event.rows = Number of rows per page
  //   //event.sortField = Field name to sort in single sort mode
  //   //event.sortOrder = Sort order as number, 1 for asc and -1 for dec in single sort mode
  //   //multiSortMeta: An array of SortMeta objects used in multiple columns sorting. Each SortMeta has field and order properties.
  //   //filters: Filters object having field as key and filter value, filter matchMode as value
  //   //globalFilter: Value of the global filter if available

  //   const pageResults = this.entityApiService.getEntityListItems(this.entity, this.list, this.columns);

  //   this.items = pageResults.items;
  // }
}
