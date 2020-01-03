import { PathInfo } from '@alfresco/js-api';
import { Node } from '@alfresco/js-api';
import { PermissionsInfo } from '@alfresco/js-api';
import { EntityList } from './EntityList';

//  "container": "workspace:\/\/SpacesStore\/850ea5b0-347f-459d-bf48-47f9006f583d",
//     "permissions":
//     {
//        "create": true
//     },
   
//     "entity" : {
//           "nodeRef": "workspace://SpacesStore/22d2ccb9-1beb-4330-bb76-1ccc03318e0b",
//           "parentNodeRef" : "workspace://SpacesStore/181f2c64-fe19-4787-b1fe-157a5e2c93f0",
//           "name": "test",
//           "userAccess":
//           {
//              "create": true,
//              "edit": true,
//              "delete":true
//           },
//           "userSecurityRoles":[
//               "ApplyChangeOrder",
//               "CreateChangeOrder"
//           ],
//           "aspects": 
//           [
//              "bcpg:formulatedEntityAspect",
//              "bcpg:consumerAspect",
//              "bcpg:effectivityAspect",
//              "bcpg:erpCodeAspect",
//              "bcpg:productMicrobioCriteriaAspect",
//              "bcpg:legalNameAspect",
//              "sys:referenceable",
//              "bcpg:contractDateAspect",
//              "bcpg:entityScoreAspect",
//              "sys:localized",
//              "bcpg:entityTplRefAspect",
//              "bcpg:codeAspect",
//              "bcpg:entityListsAspect",
//              "rep:reportEntityAspect",
//              "bcpg:transformationAspect",
//              "cm:titled",
//              "bcpg:profitabilityAspect",
//              "bcpg:clientsAspect",
//              "sys:cascadeUpdate",
//              "cm:auditable",
//              "bcpg:eanAspect",
//              "bcpg:productAspect"
//           ],
//           "type": "bcpg:finishedProduct",
//           "path": ""
  
//      }
//  }


export class Entity extends Node{
 /*
  Extend from node

  id: string;
  parentId?: string;
  name: string;
  nodeType: string;
  aspectNames?: string[];
  properties?: any;
  path?: PathInfo;
  permissions?: PermissionsInfo;
  allowableOperations?: string[];
  */

  datalists?: EntityList[];

  isEntityTemplate = true;
}