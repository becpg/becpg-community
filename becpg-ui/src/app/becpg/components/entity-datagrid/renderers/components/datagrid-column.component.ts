import { EntityListItem } from '../../../../model/EntityListItem';
import { EntityListColumn } from '../../../../model/EntityListColumn';

export interface DatagridColumnComponent {
    item: EntityListItem;
    column: EntityListColumn;
}