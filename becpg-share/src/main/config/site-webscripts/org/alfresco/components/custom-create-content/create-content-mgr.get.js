// Find the default DocumentList widget and replace it with the custom widget
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "CreateContentMgr")
  {
    model.widgets[i].options.isEntity == ((page.url.args.isEntity != null) ? page.url.args.isEntity : "false");
  }
}
