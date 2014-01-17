/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

var isEntity = false;

 var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
	   isEntity = (documentDetails.item.node.aspects.indexOf("bcpg:entityListsAspect") >0);
   }

//Find the default DocumentList widget and replace it with the custom widget
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "DocumentVersions")
  {
	  
	  
    model.widgets[i].name = "beCPG.custom.DocumentVersions";
    model.widgets[i].options.isEntity  = isEntity;
    model.allowNewVersionUpload  = (isEntity == false);
    model.widgets[i].options.allowNewVersionUpload  = model.allowNewVersionUpload;    
  }
}
