<import resource="classpath:/beCPG/rules/helpers.js">

function classifyQualityControl(qualityControl) {
	var qcState = propValue(qualityControl, "qa:qcState");
	if (!isEmpty(qcState)) {
		
		var qcStateDisplayValue = null;
		
		if (qcState == "Compliant") {
			qcStateDisplayValue = bcpg.getMessage("becpg.quality.control.compliant");
		} else if (qcState == "NonCompliant") {
			qcStateDisplayValue = bcpg.getMessage("becpg.quality.control.non-compliant");
		}
		
        var batchStart = qualityControl.properties["qa:batchStart"];

        if (!isEmpty(batchStart)) {
            return classifyByDate(qualityControl, "/app:company_home/st:sites/cm:" + ARCHIVED_SITE_ID + "/cm:documentLibrary/" + getQNameTitle("qa:qualityControl") + "/" + qcStateDisplayValue, batchStart, "YYYY/MM - MMMM");
        } else {
            return classifyByHierarchy(qualityControl, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
        }
		
	} else {
		return classifyByHierarchy(qualityControl, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
	}
}

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
		
		var wasMoved = false;
		
		if (state == "Valid") {
			wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(VALID_SITE_ID));
		} else if ((state == "Simulation" || state == "ToValidate") && !isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
			wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
		} else if (state == "Archived") {
			wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
		} else if (document.isSubType("pjt:project")) {
			if (!isInSite(document, SUPPLIER_PORTAL_SITE_ID)) {
				if(document.properties["pjt:projectState"] == "Completed" || document.properties["pjt:projectState"] == "Cancelled"){
					wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
				} else {
					wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
				}
			} else {
				wasMoved = classifyByHierarchy(document, getDocumentLibraryNodeRef(SUPPLIER_PORTAL_SITE_ID));
			}
		} else if (document.isSubType("qa:qualityControl") ) {
			wasMoved = classifyQualityControl(document);
		} else if (document.isSubType("qa:batch")) {
			state = document.properties["qa:batchState"];
			var siteId;
			if (state == "Valid") {
				siteId = VALID_SITE_ID;
			} else if ((state == "Simulation" || state == "ToValidate")) {
				siteId = SIMULATION_SITE_ID;
			} else if (state == "Archived") {
				siteId = ARCHIVED_SITE_ID;
			}
			wasMoved = classifyByDate(document, "/app:company_home/st:sites/cm:" + siteId + "/cm:documentLibrary/" + getQNameTitle("qa:batch"), document.properties["cm:created"], "YYYY/MM - MMMM");
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






