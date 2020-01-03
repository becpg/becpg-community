import { Component, OnInit, Input  } from '@angular/core';
import { LazyLoadEvent } from 'primeng';
import { EntityApiService } from '../api/entity-api.service';
import { Entity } from '../model/Entity';
import { EntityList } from '../model/EntityList';
import { EntityListColumn } from '../model/EntityListColumn';
import { EntityListItem } from '../model/EntityListItem';
import { EntityListPageResults } from '../model/EntityListPageResults';


@Component({
  selector: 'app-entity-list',
  templateUrl: './entity-list.component.html',
  styleUrls: ['./entity-list.component.css']
})
export class EntityListComponent implements OnInit {

  pageResults: Promise<EntityListPageResults>;

  selectItems: EntityListItem[];

  @Input() list: EntityList;

  @Input() entity: Entity;

  loading: boolean;

  constructor(private entityApiService: EntityApiService) { }

  ngOnInit() {

    this.loading = true;

    this.entityApiService.getVisibleColumns(this.list).then(columns => {
      this.loading = false;
      this.pageResults = this.entityApiService.getEntityListItems(this.entity, this.list, columns);

    }

    );
    
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
