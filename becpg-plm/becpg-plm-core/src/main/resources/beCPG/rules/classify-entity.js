/*
 * Helpers
 */

function isNullOrEmpty(value) {
	if (value == null || value == "") {
		return true;
	} else {
		return false;
	}
}

function concatName(name, value) {
	if (name != "" && value != "") {
		name += " ";
	}
	name += value;
	return name;
}

function getProp(product, propName) {
	if (product != null && product != "" && product.properties[propName] != null) {
		return product.properties[propName];
	} else {
		return "";
	}
}

function getMLProp(product, propName, locale) {
	if (product != null && product != "" && bcpg.getMLProperty(product, propName, locale) != null) {
		return bcpg.getMLProperty(product, propName, locale);
	} else {
		return "";
	}
}

function getMLConstraint(propValue, propName, locale) {
	if (propValue != null && propValue != "" && bcpg.getMLConstraint(propValue, propName, locale) != null) {
		return bcpg.getMLConstraint(propValue, propName, locale);
	} else {
		return "";
	}
}

function getAssoc(product, assocName, propName) {
	if (product != null && product != "" && product.assocs[assocName] != null) {
		if (propName != null && product.assocs[assocName][0].properties[propName] != null && product.assocs[assocName][0].properties[propName] != "") {
			return product.assocs[assocName][0].properties[propName];
		}
		return product.assocs[assocName][0].name;
	} else {
		return "";
	}
}

function removeForbiddenChar(value) {
	return value.replace(/[|"<>.*?:+\/]/g, "").replace(/ -/g, "");
}

function classifyByHierarchy(productNode, folderNode, propHierarchy) {
	if (folderNode != null) {
		var action = actions.create("classify-by-hierarchy");
		action.parameters["destination-folder"] = folderNode;
		if (propHierarchy) {
			action.parameters["prop-hierarchy"] = propHierarchy;
		}
		action.execute(productNode.nodeRef);
	}
}

function isInSite(productNode, siteId) {
	if (productNode.qnamePath.indexOf("/app:company_home/st:sites/cm:" + siteId + "/") != -1) {
		return true;
	} else {
		return false;
	}
}

function isInFolder(productNode, folderNode) {
	var i = 0;

	var folder = productNode.parent;

	while (folder.isContainer) {
		i++;
		if (i > 10) {
			break;
		}

		if (folder.parent === null) {
			return false;
		}
		if (folder.getNodeRef() == folderNode) {
			return true;
		}

		folder = folder.parent;
	}

	return false;
}

function getDocumentLibraryNodeRef(siteId) {
	var site = siteService.getSite(siteId);
	return site.getContainer("documentLibrary");
}

//
// Sample method
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

const SIMULATION_SITE_ID = "simulation";
const VALID_SITE_ID = "valid";
const ARCHIVED_SITE_ID = "archived";

function main() {

	if (!document.hasAspect("bcpg:entityTplAspect") && !document.hasAspect("cm:workingcopy")) {

		var state = "";
		if (document.isSubType("bcpg:product")) {
			state = document.properties["bcpg:productState"];
		} else if (document.isSubType("bcpg:client")) {
			state = document.properties["bcpg:clientState"];
		} else if (document.isSubType("bcpg:supplier")) {
			state = document.properties["bcpg:supplierState"];
		}

		if (state == "Valid") {
			if (!isInSite(document, VALID_SITE_ID)) {
				classifyByHierarchy(document, getDocumentLibraryNodeRef(VALID_SITE_ID));
			}
		} else if (state == "Simulation" || state == "ToValidate") {
			if (isInSite(document, VALID_SITE_ID) || isInSite(document, ARCHIVED_SITE_ID)) {
				bcpg.moveAndRename(document, getDocumentLibraryNodeRef(SIMULATION_SITE_ID));
			}
		} else if (state == "Archived") {
			if (!isInSite(document, ARCHIVED_SITE_ID)) {
				classifyByHierarchy(document, getDocumentLibraryNodeRef(ARCHIVED_SITE_ID));
			}
		}

	}
}

main();
