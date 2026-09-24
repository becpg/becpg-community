<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

/**
 * Reads from the repository whether the embedded regulatory view is enabled, and which origin serves it.
 */
function getRegulatoryViewConfig()
{
   var result = remote.connect("alfresco").get("/becpg/regulatory/view/config");
   if (result.status == 200)
   {
      return jsonUtils.toObject("" + result.response);
   }
   return { enabled : false };
}

function main()
{
   var config = getRegulatoryViewConfig();
   model.enabled = config.enabled === true;

   var regulatoryView = {
      id : "RegulatoryView",
      name : "beCPG.component.RegulatoryView",
      options : {
         entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
         uiUrl : model.enabled ? config.uiUrl : ""
      }
   };

   model.widgets = [ regulatoryView ];
}

main();
