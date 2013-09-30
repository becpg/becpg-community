const PROPS =[ {prop:"cm:title"},
               {prop:"cm:description"},
               {prop:"bcpg:legalName"},
               {prop:"bcpg:packagingDescription"},
               {prop:"bcpg:productComments"},
               {prop:"bcpg:useByDate"},
               {prop:"bcpg:bestBeforeDate"},	
               {prop:"bcpg:preparationTips"},	
               {prop:"bcpg:productQty"},
               {prop:"bcpg:productUnit"},
               {prop:"bcpg:netWeight"},
               {prop:"bcpg:numberOfServings"},
               {prop:"bcpg:unitPrice"},
               {prop:"bcpg:priceCurrency"},
               {prop:"bcpg:projectedQty"} ];
const ASSOCS =[ {assoc:"bcpg:clients"},								
                {assoc:"bcpg:subsidiary"},
                {assoc:"bcpg:plants"},
                {assoc:"bcpg:trademark"},								
                {assoc:"bcpg:storageConditions"},
                {assoc:"bcpg:precautionOfUse"},	
                {assoc:"bcpg:productSpecifications"}];
	
	
function createCallForTender() {
	//var project = document.parent.parent;
	var project = search.findNode("workspace://SpacesStore/7a71991f-32af-44fc-aea2-3f6693c65725");
	var documentLibrary = project.parent.parent;
	var suppliersAssocName = "bcpg:suppliers";

	if (project != null && documentLibrary != null) {
		
		var suppliers = project.assocs[suppliersAssocName];
		var productTemplate = search.findNode("workspace://SpacesStore/f1af1136-4019-432b-991e-cc485cea97df");	

		if (suppliers != null && productTemplate != null) {
			for ( var k = 0; k < suppliers.length; k++) {
				
				logger.log("supplier " + suppliers[k].name);
				var supplierFolder = documentLibrary.childByNamePath(suppliers[k].name);

				if (supplierFolder != null) {
					var newProduct = productTemplate.copy(supplierFolder, true);
					newProduct.removeAspect("bcpg:entityTplAspect");
					newProduct.properties["cm:name"] = project.name + " " + suppliers[k].name;					
					// update with project ones
					for(row in PROPS){
						newProduct.properties[PROPS[row].prop] = project.properties[PROPS[row].prop];
					}					
					newProduct.save();
					newProduct.createAssociation(suppliers[k], suppliersAssocName);
					
					// update with project ones
					for(row in ASSOCS){
						var assocs = project.assocs[ASSOCS[row].assoc];
						if(assocs != null){
							for (var i = 0; i < assocs.length; i++)
							{						
								newProduct.createAssociation(assocs[i], ASSOCS[row].assoc);
							}
						}					
					}
					
					var briefDoc = project.childByNamePath("1) Brief/Brief.doc");
					if(briefDoc != null){
						briefDoc.copy(newProduct.childByNamePath("Brief"))
					}
				}
			}
		}
	}
}

createCallForTender();