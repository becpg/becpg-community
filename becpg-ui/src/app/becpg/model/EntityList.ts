
import { PermissionsInfo } from '@alfresco/js-api';

//   
// {
//     "entityName" : "test",
//      "name": "packagingList",
//      "title": "Emballages",
//      "description": "Liste des emballages et UL",
//      "nodeRef": "workspace://SpacesStore/ca3f42f5-3f0a-450c-9141-f088f8fecea1",
//      "itemType": "bcpg:packagingList",
//      "state"  :"ToValidate",
//      "permissions":
//      {
//         "edit": true,
//         "delete": true,
//         "changeState": true
//      }
//   }


export class EntityList {
   id: string;
   name: string;
   title: string;
   description: string;
   nodeType: string;
   state: string;
   permissions: PermissionsInfo[];
}
