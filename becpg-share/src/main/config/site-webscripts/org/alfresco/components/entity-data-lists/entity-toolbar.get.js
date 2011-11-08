/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Main entrypoint for component webscript logic
 *
 * @method main
 */
function main()
{
   // Actions
   var myConfig = new XML(config.script),
      xmlActionSet = myConfig.actionSet,
      actionSet = [];
   
   for each (var xmlAction in xmlActionSet.action)
   {
      actionSet.push(
      {
         id: xmlAction.@id.toString(),
         type: xmlAction.@type.toString(),
         permission: xmlAction.@permission.toString(),
         asset: xmlAction.@asset.toString(),
         href: xmlAction.@href.toString(),
         label: xmlAction.@label.toString()
      });
   }
   var entityNodeRef = page.url.args.nodeRef;
   
   model.showFormulate = showFormulate(entityNodeRef);
   model.actionSet = actionSet;
}

/**
 * Call backend to retrieve aspect and see if it should show the formulate button
 * @param nodeRef
 * @returns
 */
function showFormulate(nodeRef)
{
   var result = remote.call("/slingshot/doclib/aspects/node/"+nodeRef.replace(":/",""));
   
   if (result.status == 200)
   {
      var aspects = eval('(' + result + ')'),
      aspect;
      
      for (var i = 0, ii = aspects.current.length; i < ii; i++)
      {
         aspect = aspects.current[i];
         if (aspect == "bcpg:transformationAspect")
         {
            return true;
         }
      }
   }

   return false;
}


main();