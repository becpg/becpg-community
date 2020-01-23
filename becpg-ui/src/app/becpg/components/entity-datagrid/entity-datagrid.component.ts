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
import { SortEvent } from 'primeng/api';
import { MenuItem } from 'primeng/api';


interface FilterableMenuItem extends MenuItem {
  permission: string;
}

@Component({
  selector: 'app-entity-datagrid',
  templateUrl: './entity-datagrid.component.html',
  styleUrls: ['./entity-datagrid.component.scss']
})
export class EntityDatagridComponent implements OnChanges, OnInit {



  pageResults: Observable<EntityListPageResults>;

  columns: Observable<EntityListColumn[]>;


  initialColumns: EntityListColumn[];

  selectedColumns: EntityListColumn[];

  filterOn = false;

  currentFilter =
    {
      filterId: 'all',
      filterData: ''
    };

  allPages: false;

  queryExecutionId: string;

  selectedItems: EntityListItem[];

  selectedItem: EntityListItem;

  selectedMenuItems: FilterableMenuItem[];

  rowMenuItems: FilterableMenuItem[];

  calendarLocale = 'fr';

  dateFormat = 'dd/mm/yyyy';

  //extraDataParams : "&clearCache=true&repo=true&effectiveFilterOn="+instance.options.effectiveFilterOn
  effectiveFilterOn = false;

  @Input() customLists: EntityList[];

  @Input() list: EntityList;

  @Input() entity: Entity;

  @Input() formId: string;

  @Input() itemType: string;

  loading: boolean;

  constructor(private entityDatagridService: EntityDatagridService) { }

  ngOnInit() {

    this.selectedMenuItems = [
      { icon: 'pi pi-search', command: (event) => this.onActionShowDetails(), permission: 'details', label: 'actions.show-details' },
      { icon: 'pi pi-search', command: (event) => this.onActionDuplicate(), permission: 'create', label: 'menu.selected-items.duplicate' },
      { icon: 'pi pi-search', command: (event) => this.onActionDelete(), permission: 'delete', label: 'menu.selected-items.delete' },
      { icon: 'pi pi-search', command: (event) => this.onActionUp(), permission: 'sort', label: 'menu.selected-items.up' },
      { icon: 'pi pi-search', command: (event) => this.onActionDown(), permission: 'sort', label: 'menu.selected-items.down' },
      { icon: 'pi pi-search', command: (event) => this.onActionShowWused(), permission: 'wused', label: 'actions.wused' },
      { icon: 'pi pi-search', command: (event) => this.onActionBulkEdit(), permission: 'edit,allPages', label: 'actions.bulk-edit' },
      { icon: 'pi pi-search', command: (event) => this.onActionSelectColor(), permission: 'edit', label: 'actions.select-color' },
    ];


    this.rowMenuItems = [
      { icon: 'pi pi-search', command: (event) => this.onActionEdit(), permission: 'edit', label: 'actions.edit' },
      { icon: 'pi pi-search', command: (event) => this.onActionShowDetails(), permission: 'details', label: 'actions.show-details' },
      { icon: 'pi pi-search', command: (event) => this.onActionDuplicate(), permission: 'create', label: 'actions.duplicate-row' },
      { icon: 'pi pi-search', command: (event) => this.onActionDelete(), permission: 'delete', label: 'actions.delete-row' },
      { icon: 'pi pi-search', command: (event) => this.onActionShowComments(), permission: 'edit', label: 'actions.comment' },
      { icon: 'pi pi-search', command: (event) => this.onActionShowWused(), permission: 'wused', label: 'actions.wused' },
      { icon: 'pi pi-search', command: (event) => this.onActionSelectColor(), permission: 'edit', label: 'actions.select-color' },
      { icon: 'pi pi-search', command: (event) => this.onActionUploadContent(), permission: 'content', label: 'actions.upload-content' },

    ];

    this.loadColumns();

  }

  ngOnChanges() {
    this.loadColumns();
  }


  filterRowMenuItems() {
   const userAccess = this.selectedItem.permissions.userAccess;

    this.rowMenuItems.forEach(item => {
      const actionPermissions = item.permission.split(',');
      for (let j = 0, jj = actionPermissions.length; j < jj; j++) {
        let aP = actionPermissions[j];
        // Support "negative" permissions
        if ((aP.charAt(0) === "~") ? !!userAccess[aP.substring(1)] : !userAccess[aP]) {
          item.disabled = true;
          break;
        }
      }
    }
    );

  }

  filterSelectedMenuItems() {

    const userAccess = {};

    // Check each item for user permissions
    for (let i = 0, ii = this.selectedItems.length; i < ii; i++) {
      const item = this.selectedItems[i];

      // Required user access level - logical AND of
      // each
      // item's permissions
      const itemAccess = item.permissions.userAccess;
      for (const index in itemAccess) {
        if (itemAccess.hasOwnProperty(index)) {
          userAccess[index] = (userAccess[index] === undefined ? itemAccess[index]
            : userAccess[index] && itemAccess[index]);
        }
      }
    }

    this.selectedMenuItems.forEach(item => {
      const actionPermissions = item.permission.split(',');
      let disabled = false;
      let disabledForAllPages = true;

      for (let j = 0, jj = actionPermissions.length; j < jj; j++) {
        if (actionPermissions[j] !== 'allPages') {
          if ((!userAccess[actionPermissions[j]])) {

            disabled = true;
            break;
          }
        } else {
          disabledForAllPages = false;
        }
      }

      if (this.allPages && (this.queryExecutionId == null || disabledForAllPages)) {
        disabled = true;
      }

      item.disabled = disabled;

    }

    );
  }


  onRowSelect(event) {
    this.filterSelectedMenuItems();
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
        this.pageResults.subscribe(data => { this.loading = false; });
        this.initialColumns = columns.filter(col => !col.hidden);
        this.selectedColumns = this.initialColumns;
      });


    }
  }





  toggleFilter(): void {
    this.filterOn = !this.filterOn;
  }

  onActionAdd(): void {

  }

  onActionShowDetails(): void {

  }

  onActionDuplicate(): void {

  }

  onActionDelete(): void {

  }

  onActionUp(): void {

  }

  onActionDown(): void {

  }

  onActionShowWused(): void {

  }


  onActionBulkEdit(): void {

  }

  onActionSelectColor(): void {

  }

  onActionEdit(): void {

  }

  onActionShowComments(): void {

  }

  onActionUploadContent(): void {

  }


  hasPermissions( permissions: string): boolean {  
    return true;
  }



  loadData(event: LazyLoadEvent) {
    //event.first = First row offset
    //event.rows = Number of rows per page
    //event.sortField = Field name to sort in single sort mode
    //event.sortOrder = Sort order as number, 1 for asc and -1 for dec in single sort mode
    //multiSortMeta: An array of SortMeta objects used in multiple columns sorting. Each SortMeta has field and order properties.
    //filters: Filters object having field as key and filter value, filter matchMode as value
    //globalFilter: Value of the global filter if available

    this.pageResults = this.entityDatagridService.getEntityListItems(this.entity, this.list, this.selectedColumns, this.itemType);
    this.pageResults.subscribe(data => { this.loading = false; });
  }
}
