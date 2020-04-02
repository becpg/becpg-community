/*
 * Helpers


propValue
mlPropValue
mlPropConstraint
assocValue
assocValues
assocPropValues
assocPropValue
updateAssoc
updateAssocs
updateMLText

formatNumber
formatDate 
cleanName // removeForbiddenChar
classifyByHierarchy
isInSite
isInUserFolder
isInFolder

clearPermissions
deleteGroupPermission

isNullOrEmpty // isEmpty
concatName


//bcpg.
createBranch 
mergeBranch
moveAndRename
generateEAN13Code


allowWrite --> by Passing rights

//Deprecated
getEntityListFromNode(product, listName)
getItemValueFromList

//TODO
getListItems

 */

function getEntityListFromNode(product, listName) {

    var entityList = null;

    if (product != null && product.childAssociations["bcpg:entityLists"] != null) {
        var entityLists = product.childAssociations["bcpg:entityLists"][0];

        var children = entityLists.childFileFolders();
        for (var list in children) {

            if (listName === children[list].properties["cm:name"]) {
                entityList = children[list];
                break;
            }

        }
    }

    return entityList;
}

function getItemValueFromList(list, itemName, assocName, valueProp) {
    var res = null;

    if (list != null) {
        for (var item in list.children) {
            var curItem = list.children[item];

            if (curItem.assocs[assocName] != null && curItem.assocs[assocName][0].properties["bcpg:charactName"] == itemName) {
                res = curItem.properties[valueProp];
                break;
            }

        }
    }
    return res;
}



//function clearPermissions(document, inherit){
//	document.setInheritsPermissions(inherit);
//	for each(var permission in document.getDirectPermissions()) {
//		document.removePermission(permission.split(';')[2],permission.split(';')[1]);
//	}
//}
//
//function deleteGroupPermission(node, group){
//	var inheritedPermissions = [];
//	for each(var perm in node.getFullPermissions()){
//		if (perm.split(';')[3] == "INHERITED"){
//			inheritedPermissions.push(perm);
//		}
//	}
//	node.setInheritsPermissions(false);
//	for each(var perm in inheritedPermissions){
//		if(perm.split(';')[1] != group){
//			node.setPermission(perm.split(';')[2],perm.split(';')[1]);
//		}
//	}
//}


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
	} 
	return false;
}

function isInUserFolder(productNode){
	if (productNode.qnamePath.indexOf("/app:company_home/app:user_homes") != -1) {
		return true;
	}
	return false;
	
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
