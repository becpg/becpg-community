<import resource="classpath:/beCPG/rules/helpers.js">


function main() {

	if (document.hasPermission("Read") && !document.hasAspect("bcpg:entityTplAspect") && !document.hasAspect("cm:workingcopy") && !isInUserFolder(document)) {
		var state = "";
		if (document.isSubType("bcpg:product")) {
			state = document.properties["bcpg:productState"];
		} else if (document.isSubType("bcpg:client")) {
			state = document.properties["bcpg:clientState"];
		} else if (document.isSubType("bcpg:supplier")) {
			state = document.properties["bcpg:supplierState"];
		}

		if (state == "Valid") {
			classifyByHierarchy(document, getDocumentLibraryNodeRef(VALID_SITE_ID));
		} else if ((state == "Simulation" || state == "ToValidate") && !isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
			classifyByHierarchy(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
		} else if (state == "Archived") {
			classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
		} else if (document.isSubType("pjt:project")) {
            if (!isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
                if(document.properties["pjt:projectState"] == "Completed" || document.properties["pjt:projectState"] == "Cancelled"){
                    classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
                } else {
                    classifyByHierarchy(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
                }
            } else {
                classifyByHierarchy(document, getDocumentLibraryNodeRef(SUPPLIER_PORTAL_SITE_ID));
            }
        }

		//Rename Sample
		//rename();
	}
}


main();


/////////////////////////////
// Add here you custom code
////////////////////////////

//
//Sample rename method
//
function rename(product) {
	var name = propValue(product, "cm:title");
	var erpCode = propValue(product, "bcpg:erpCode");

	if (!isEmpty(name) && (isEmpty(erpCode) || name.indexOf(erpCode) == -1)) {

		name = concatName(cleanName(erpCode), 
				cleanName(name).toUpperCase());
		
		// If you want to rename if name already exists
		//name = getAvailableName(product.parent,name);
		
		if (product.properties["cm:name"] != name && product.parent.childByNamePath(name) == null) {
			product.properties["cm:name"] = name;
			product.save();
		}
	}
}






