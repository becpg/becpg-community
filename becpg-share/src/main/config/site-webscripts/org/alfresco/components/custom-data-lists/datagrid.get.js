// Find the default DataGrid widget and replace it with the custom widget
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "DataGrid")
  {
    model.widgets[i].name = "beCPG.component.DataGrid";
  }
}