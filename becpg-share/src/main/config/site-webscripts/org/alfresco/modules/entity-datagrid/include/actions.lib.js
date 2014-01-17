function parseActions(list)
{
   // Actions
   var myConfig = new XML(config.script),
      actionSetToolbar = [],
      actionSetDataGrid = [];
   
   for each (var xmlAction in myConfig.actionSetToolbar.action)
   {
    if( !list || xmlAction.@list.toString().length < 1 || xmlAction.@list.toString() == list){
      	actionSetToolbar.push(
         {
            id: xmlAction.@id.toString(),
            type: xmlAction.@type.toString(),
            permission: xmlAction.@permission.toString(),
            asset: xmlAction.@asset.toString(),
            href: xmlAction.@href.toString(),
            label: xmlAction.@label.toString()
         });
      }
   }
   
   for each (var xmlAction in myConfig.actionSetDataGrid.action)
   {
      if( !list || xmlAction.@list.toString().length < 1 || xmlAction.@list.toString() == list){
      	actionSetDataGrid.push(
         {
            id: xmlAction.@id.toString(),
            type: xmlAction.@type.toString(),
            permission: xmlAction.@permission.toString(),
            asset: xmlAction.@asset.toString(),
            href: xmlAction.@href.toString(),
            label: xmlAction.@label.toString()
         });
      }
   }
   
   model.actionSetToolbar = actionSetToolbar;
   model.actionSet = actionSetDataGrid;
}
