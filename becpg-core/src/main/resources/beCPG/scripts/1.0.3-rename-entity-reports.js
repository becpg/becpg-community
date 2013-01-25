
var nodes = search.luceneSearch('+TYPE:"bcpg:product"');	

for(i in nodes){
	
	var product = nodes[i];
	var entityFolder = product.parent;
	if(entityFolder!=null)
	{
		var documentsFolder = entityFolder.childByNamePath("Documents");
		if(documentsFolder!=null)
		{
			var productDoc = documentsFolder.childByNamePath("Produit fini - fiche atelier");
			if(productDoc!=null)
			{
				productDoc.properties["cm:name"] = product.properties["cm:name"] + " - " + "Fiche atelier.pdf"
				productDoc.save();
				
				product.createAssociation(productDoc, "rep:reports");
			}	
		}
	}

}