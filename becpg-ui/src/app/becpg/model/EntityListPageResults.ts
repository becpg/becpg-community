import { EntityListItem } from './EntityListItem';
import { EntityListColumn } from './EntityListColumn';

export class EntityListPageResults {
    items: EntityListItem[];
    parentId: string;
    pageSize: number;
    queryExecutionId: string;
    startIndex: number;
    totalRecords: number;
    columns : EntityListColumn[];
}
