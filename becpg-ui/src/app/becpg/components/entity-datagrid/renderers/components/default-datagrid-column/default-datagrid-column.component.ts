import { Component, OnInit } from '@angular/core';
import { EntityListItem } from '../../../../../model/EntityListItem';
import { EntityListColumn } from '../../../../../model/EntityListColumn';
import { DatagridColumnComponent } from '../datagrid-column.component';

@Component({
  selector: 'app-default-datagrid-column',
  templateUrl: './default-datagrid-column.component.html',
  styleUrls: ['./default-datagrid-column.component.scss']
})
export class DefaultDatagridColumnComponent implements DatagridColumnComponent {

  item: EntityListItem;
  column: EntityListColumn;

  constructor() { }


  isArray(): boolean {
    return Array.isArray(this.item.itemData[this.column.name]);
  }

}
