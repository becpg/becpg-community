<import resource="classpath:alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{

    var site = page.url.templateArgs.site;
	var prefs = "org.alfresco.share.product.list."+ (site!= null && site.length>0 ? site : "home")
		
	var preferences = AlfrescoUtil.getPreferences(prefs);
	var currentType = preferences.type!=null ? preferences.type: "finishedProduct";
  
    parseActions(currentType);
    
    var dataType = "bcpg:finishedProduct";

	if (currentType != null) {
		dataType = currentType.indexOf("_") > 0 ?currentType.replace("_", ":") : "bcpg:" + currentType;
	}
	

   //Widget instantiation metadata...
   var productList = {
    id : "productList", 
    name : "beCPG.component.ProductList",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       itemType: dataType,
       extraDataParams: page.url.templateArgs.site!=null ? "&repo=false&container=documentLibrary":"&repo=true",
       sortUrl :  page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node",
       itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/"
      }
   };
    
   model.widgets = [productList];

   
}

main();
