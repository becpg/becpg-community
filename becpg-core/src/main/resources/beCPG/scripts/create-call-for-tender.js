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
	var project = document.parent.parent.parent;
	//var project = search.findNode("workspace://SpacesStore/6c48fb62-5ebd-47ba-ac8e-48e0af3b1d56");
	var documentLibrary = project.parent.parent;
	var suppliersAssocName = "bcpg:suppliers";

	if (project != null && documentLibrary != null) {
		
		var suppliers = project.assocs[suppliersAssocName];
		var productTemplate = search.findNode("workspace://SpacesStore/9e528d08-fe3a-4ecf-af8c-7b2077acd980");
		var projectEntities = project.assocs["pjt:projectEntity"];

		if (suppliers != null && productTemplate != null && projectEntities == null) {
			
//			var productTemplate = productTemplate[0]; 
//			project.removeAssociation(productTemplate, "pjt:projectEntity");
			
			for ( var k = 0; k < suppliers.length; k++) {
				
				logger.log("supplier " + suppliers[k].name);
				var supplierFolder = documentLibrary.childByNamePath(suppliers[k].name + "/Produits");

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
					newProduct.createAssociation(productTemplate, "bcpg:entityTplRef");
					
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
					
					project.createAssociation(newProduct, "pjt:projectEntity");
				}
			}
		}
	}
}

createCallForTender();