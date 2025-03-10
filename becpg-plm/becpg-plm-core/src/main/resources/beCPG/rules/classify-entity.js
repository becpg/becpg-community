<import resource="classpath:/beCPG/rules/helpers.js">

function main() {

	if (document.hasPermission("Read") && !document.hasAspect("bcpg:entityTplAspect") && !document.hasAspect("cm:workingcopy") && !isInUserFolder(document)) {
		var state = "";
		
		var date = null;
		
		var site = null;
		
		var path = "";
		
		if (document.isSubType("bcpg:product")) {
			state = document.properties["bcpg:productState"];
		} else if (document.isSubType("bcpg:client")) {
			state = document.properties["bcpg:clientState"];
		} else if (document.isSubType("bcpg:supplier")) {
			state = document.properties["bcpg:supplierState"];
			if (isInSite(document, SUPPLIER_PORTAL_SITE_ID) && !(document.parent.hasPermission("Read") && document.parent.parent.hasPermission("Read") && document.parent.parent.isSubType("bcpg:supplier"))) {
				site = getDocumentLibraryNodeRef(SUPPLIER_PORTAL_SITE_ID);	
			}
			if (state == "Archived") {
				var supplierAccounts = document.assocs["bcpg:supplierAccountRef"];
				if (supplierAccounts) {
					for (var i = 0; i < supplierAccounts.length; i++) {
						var supplierAccount = supplierAccounts[i];
						deleteExternalUser(supplierAccount, document);
					}
				}
			}
		} else if (document.isSubType("bcpg:productCollection")) {
			state = document.properties["bcpg:productCollectionState"];
		} else if (document.isSubType("pjt:project")) {
			if (!isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
				if(document.properties["pjt:projectState"] == "Completed" || document.properties["pjt:projectState"] == "Cancelled"){
					site = getDocumentLibraryNodeRef(ARCHIVED_SITE_ID);
				} else {
					site = getDocumentLibraryNodeRef(SIMULATION_SITE_ID);
				}
			} else {
				site = getDocumentLibraryNodeRef(SUPPLIER_PORTAL_SITE_ID);
			}
		} else if (document.isSubType("qa:qualityControl")) {
			state = document.properties["qa:qcState"];
			if (!isEmpty(state)) {
				site = getDocumentLibraryNodeRef(ARCHIVED_SITE_ID);	
				path = state;
				date = document.properties["qa:batchStart"];
			} else {
				site = getDocumentLibraryNodeRef(SIMULATION_SITE_ID);					
			}
		} else if (document.isSubType("qa:batch")) {
			state = document.properties["qa:batchState"];
			date = document.properties["cm:created"];
		} else if (document.isSubType("ecm:changeOrder")) {
			state = document.properties["ecm:ecoState"];
			
	        if (state == "Applied") {
				site = getDocumentLibraryNodeRef(ARCHIVED_SITE_ID);
	        } else {
				site = getDocumentLibraryNodeRef(SIMULATION_SITE_ID);
			}
				
			date = document.properties["cm:created"];
		}
		
		if (isEmpty(site)) {
			if (state == "Valid") {
				site = getDocumentLibraryNodeRef(VALID_SITE_ID);
			} else if ((state == "Simulation" || state == "ToValidate") && !isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
				site = getDocumentLibraryNodeRef(SIMULATION_SITE_ID);
			} else if (state == "Archived") {
				site = getDocumentLibraryNodeRef(ARCHIVED_SITE_ID);
			}
		}
		
		var wasMoved = false;
			
		if (!isEmpty(date)) {
			wasMoved = classifyByDate(document, path, date, "YYYY/MM - MMMM", site);
		} else if (!isEmpty(site)) {
			wasMoved = classifyByHierarchy(document, site);
		}
		
		// formulate if the document was moved
		if (wasMoved && document.isSubType("bcpg:product")) {
			formulate(document);
		}
		
		//Rename Sample
		//rename(document);
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






