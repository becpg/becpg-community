<import resource="classpath:/beCPG/rules/helpers.js">


const SIMULATION_SITE_ID = "simulation";
const VALID_SITE_ID = "valid";
const ARCHIVED_SITE_ID = "archived";
const SUPPLIER_PORTAL_SITE = "supplier-portal";


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
		} else if ((state == "Simulation" || state == "ToValidate") && !isInSite(document, SUPPLIER_PORTAL_SITE)) {
			classifyByHierarchy(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
		} else if (state == "Archived") {
			classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
		}

	}
}

main();

//
//Sample method
//
function rename(product) {
	var name = getProp(product, "cm:title");

	if (name != null && name != "") {

		name = removeForbiddenChar(name.trim().toUpperCase());
		name = concatName(getProp(product, "bcpg:erpCode"), name);

		if (name != "" && product.properties["cm:name"] != name && product.parent.childByNamePath(name) == null) {
			product.properties["cm:name"] = name;
			product.save();
		}
	}
}

