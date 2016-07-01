
function main(){

	var uri = "/becpg/product/nutdatabasecompare?base="+args.base+"&supplier="+args.supplier+"&entities="+args.entities;
	var connector = remote.connect("alfresco");
	var result = connector.get(uri);
	
	var statusCode = result.status.code;
	if (result.status.code == status.STATUS_OK)
	{
		
			var jsonResponse = JSON.parse(result.response);
			
			if(jsonResponse.hasOwnProperty("nuts") && jsonResponse.hasOwnProperty("headers")){
				
				model.nuts = jsonResponse.nuts;
				model.nutHeaders = jsonResponse.headers;
			}
	}


}



main();

