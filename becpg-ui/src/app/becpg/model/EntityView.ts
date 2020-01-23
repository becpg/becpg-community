import { EntityList } from './EntityList';

export class EntityView {
    id: string;
    list: EntityList;
    cards: any;
    sort: any;
    isValid: boolean;

   constructor(list: EntityList) {
        this.id = list.name;
        this.list = list;
        this.isValid = 'Valid' === list.state; 
   }

}
