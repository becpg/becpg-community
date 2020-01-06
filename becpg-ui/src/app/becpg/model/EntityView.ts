import { EntityList } from './EntityList';

export class EntityView {
    id: string;
    list: EntityList;
  cards: any;
  sort: any;

    constructor(list: EntityList) {
        this.id = list.name;
        this.list = list;
    }

    isValid(): boolean {
        return 'Valid' === this.list.state;
    }

}
