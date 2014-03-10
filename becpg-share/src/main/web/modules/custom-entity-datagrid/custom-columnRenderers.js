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
if (beCPG.module.EntityDataGridRenderers) {

 

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "text_bcpg:lkvValue",
      renderer : function(oRecord, data, label, scope) {
         if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
            var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
            return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">' + Alfresco.util
                  .encodeHTML(data.displayValue) + '</span>';
         }
         return Alfresco.util.encodeHTML(data.displayValue);

      }

   });
   
   
   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : "fm:commentCount",
                  renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

                     if (data.value != null && data.value != "" && data.value != "0") {

                        if (oColumn.hidden) {
                           scope.widgets.dataTable.showColumn(oColumn);
                           Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
                        }
                        Dom.setStyle(elCell, "width", "32px");
                        Dom.setStyle(elCell.parentNode, "width", "32px");
                        return '<div class="onActionShowComments"><a class="' + scope.id + '-action-link action-link" title="' + scope
                              .msg("actions.comment") + '" href="" rel="edit"><span>' + data.displayValue + '</span></a></div>';

                     }

                     return "";

                  }

               });

}
