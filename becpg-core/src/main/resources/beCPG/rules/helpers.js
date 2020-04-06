/**
 * Convenient beCPG javascript Helpers for rules 
 * 
 * @author matthieu
 * 
 *  
 * Constants:
 * 
 *  SIMULATION_SITE_ID, VALID_SITE_ID, ARCHIVED_SITE_ID, SUPPLIER_PORTAL_SITE_ID 
 * 
 * Methods:
 *  
 * isEmpty(value) alias isNullOrEmpty(value) returns true if value is empty or null 
 * 
 * orEmpty(value) returns value or empty if null;
 * 
 * propValue(node, propName) alias getProp(node, propName) returns node property value or empty
 * 
 * mlPropValue(node, propName, locale) alias getMLProp(node, propName, locale)  returns node multilingual property value or empty for locale
 * 
 * mlPropConstraint(propValue, propName, locale) alias getMLConstraint(propValue, propName, locale) display value or empty for multilingual constraint value
 * 
 * assocValue(node, assocName) returns assoc nodeRef array
 * 
 * assocValues(node, assocName) assoc nodeRef arrays
 * 
 * assocPropValues(node, assocName, propName) alias getAssoc(product, assocName, propName?) returns association property array of values
 * 
 * assocPropValue(node, assocName, propName) returns association property value
 * 
 * assocAssocValues(node, assocName, assocAssocName) returns association association nodeRef array
 * 
 * assocAssocValue(node, assocName, assocAssocName) returns association association nodeRef
 * 
 * copyAssocPropValue(node, assocName, propName, nodePropName) Copy association property value to node property
 * 
 * copyAssocAssocValue(node, assocName, assocAssocName, nodeAssocName) Copy association association value to node association
 * 	
 * updateAssoc(node, assocName, values) param values can be nodeRef, nodeRef array, scriptNode, scriptNode array
 * 
 * updateMLText(node, propQName, locale, value) Update multilingual value
 * 
 * i18n(key, params?) returns i18n message for current locale
 * 
 * cleanName(value) alias removeForbiddenChar(value) returns valid cm:name value
 * 
 * concatName(name, value, separator?) param separator default is " " returns concatenated value of name + separator + value
 * 
 * classifyByHierarchy(productNode, folderNode, propHierarchy?) classify node by hierarchy 
 * 
 * isInSite(productNode, siteId) returns true if node is in siteId
 * 
 * isInUserFolder(productNode) returns true if node is in user folder
 * 
 * getDocumentLibraryNodeRef(siteId) returns document library folder for site
 * 
 * createBranch(node, dest, autoMerge?) Create new branch of entity
 * 
 * mergeBranch( node, branchToNode, description, type) branchToNode can be null if autoMerge
 * 
 * moveAndRename(node,dest) Move node and rename if same name exists in destination
 * 
 * generateEAN13Code(prefix) returns generate EAN13 code with autonum corresponding to prefix
 * 
 * allowWrite(node, authority) Set write permissions as system bypassing rights
 * 
 * allowRead(node, authority) Set read permissions as system bypassing rights
 *  
 * deleteGroupPermission(node, group) Remove specific group permissions
 *  
 * clearPermissions(node, inherit) Remove permissions set on node
 * 
 * copyList( destNode,  sourceNode,  listQname)  Copy one list from sourceNode to destNode
 * 
 * listExist( node,  listQname) returns true if list exists and non empty
 * 
 * listItems( node,  listQname) returns list items
 * 
 */


const SIMULATION_SITE_ID = "simulation";
const VALID_SITE_ID = "valid";
const ARCHIVED_SITE_ID = "archived";
const SUPPLIER_PORTAL_SITE_ID = "supplier-portal";


/**
 * 
 * @param value
 * @returns true if value is empty or null 
 */
function isNullOrEmpty(value) {
	return value == null || value == ""
}

function isEmpty(value){
	return isNullOrEmpty(value);
}

/**
 * 
 * @param value
 * @returns value or empty if null;
 */
function orEmpty(value, defaultValue) {
	return value!=null ? value : (defaultValue ! null? defaultValue : "");
}


/**
 * 
 * @param node
 * @param propName
 * @returns node property value or empty
 */
function getProp(node, propName) {
	return isEmpty(node) ?  "" : orEmpty(node.properties[propName]) ;
}

function propValue(node, propName){
	return getProp(node,propName)
}


/**
 * 
 * @param node
 * @param propName
 * @param locale
 * @returns node multilingual property value or empty for locale
 */
function getMLProp(node, propName, locale) {
	return isEmpty(node) ?  "" : orEmpty( bcpg.getMLProperty(node, propName, locale));
}

function mlPropValue(node, propName, locale) {
	return getMLProp(node, propName, locale);
}

/**
 * 
 * @param propValue
 * @param propName
 * @param locale
 * @returns display value or empty for multilingual constraint value
 */
function getMLConstraint(propValue, propName, locale) {
	return isEmpty(propValue) ?  "" : orEmpty( bcpg.getMLConstraint(propValue, propName, locale));
}

function mlPropConstraint(propValue, propName, locale) {
	return getMLConstraint(propValue, propName, locale);
}

/**
 * 
 * @param node
 * @param assocName
 * @returns association nodeRef
 */
function assocValue(node, assocName) {
	return isEmpty(node) ?  "" : orEmpty( bcpg.assocValue(node, assocName));
}

/**
 * 
 * @param node
 * @param assocName
 * @returns association nodeRef array
 */
function assocValues(node, assocName) {
	return isEmpty(node) ?  new Array() : orEmpty( bcpg.assocValues(node, assocName),new Array());
}

/**
 * 
 * @param node
 * @param assocName
 * @param propName
 * @returns association property array of values
 */
function assocPropValues(node, assocName, propName) {
	return isEmpty(node) ?  new Array() : orEmpty( bcpg.assocPropValues(node, assocName, propName), new Array());
}

/**
 * 
 * @param node
 * @param assocName
 * @param assocAssocName
 * @returns Association association nodeRef array
 */
function assocAssocValues(node, assocName, assocAssocName){
	return isEmpty(node) ?  new Array() : orEmpty( bcpg.assocAssocValues(node, assocName, assocAssocName), new Array());
}

/**
 * 
 * @param node
 * @param assocName
 * @param propName
 * @returns association property value
 */
function assocPropValue(node, assocName, propName) {
	return isEmpty(node) ?  "" : orEmpty( bcpg.assocPropValues(node, assocName, propName));
}

/**
 * 
 * @param node
 * @param assocName
 * @param assocAssocName
 * @returns Association association nodeRef
 */
function assocAssocValue(node, assocName, assocAssocName){
	 return isEmpty(node) ?  null : orEmpty( bcpg.assocPropValue(node, assocName, assocAssocName));
}


/**
 * Alias of assocPropValue
 * @param product
 * @param assocName
 * @param propName default is cm:name
 * @returns
 */
function getAssoc(product, assocName, propName) {
	if(propName){
		return assocPropValue(node, assocName, propName)
	} 
	return assocPropValue(node, assocName, "cm:name")
}


/**
 * 
 * Update assoc with values, if value is empty array or null will clear assoc
 * 
 * @param node
 * @param assocName
 * @param values can be nodeRef, nodeRef array, scriptNode, scriptNode array
 * @returns void
 */
function updateAssoc(node, assocName, values){
	if(!isEmpty(node)){
		bcpg.updateAssoc(node, assocName, values);
	}
}


/**
 * Copy association property value to node property
 * @param node
 * @param assocName
 * @param propName
 * @param nodePropName
 * @returns void
 */
function copyAssocPropValue(node, assocName, propName, nodePropName) {
	var value = assocPropValue(node, assocName, propName);
	if(isEmpty(value)){
	    delete node.properties[nodePropName];
	} else {
		node.properties[nodePropName] = value;
	}
}

/**
 * Copy association association value to node association
 * @param node
 * @param assocName
 * @param assocAssocName
 * @param nodeAssocName
 * @returns void
 */
function copyAssocAssocValue(node, assocName, assocAssocName, nodeAssocName) {
	updateAssoc(node, nodeAssocName, assocAssocValues(node, assocName, assocAssocName) );
}



/**
 * 
 * @param key
 * @returns i18n message for current locale
 */
function i18n(key){
	return bcpg.getMessage(key)
}


/**
 * 
 * @param key
 * @param params
 * @returns i18n message for current locale
 */
function i18n(key, params){
	return bcpg.getMessage(key, params)
}

/**
 * Update multilingual value
 * @param node
 * @param propQName
 * @param locale
 * @param value
 * @returns void
 */
function updateMLText(node, propQName, locale, value){
	bcpg.setMLProperty(ScriptNode sourceNode, String propQName, String locale, String value)
}


/**
 * @param value
 * @returns valid cm:name value
 */
function removeForbiddenChar(value) {
	return isEmpty(value) ?  "" : value.replace(/[|"<>.*?:+\/]/g, "").replace(/ -/g, "").trim();
}

function cleanName(value) {
	return removeForbiddenChar(value));
}

/**
 * 
 * @param name
 * @param value
 * @param separator optional default " "
 * @returns concatenated value of name + separator + value
 */
function concatName(name, value, separator) {
	if(name!=null){
		if (!isEmpty(name) && !isEmpty(value)) {
			if(separator){
				name += separator;
			} else {
				name += " ";
			}
		}
		if(value!=null){
			name += value;
		}
		return name;
	}
	return value;
}


/**
 * Classify node by Hierarchy
 * @param productNode
 * @param folderNode
 * @param propHierarchy
 * @returns void
 */
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

/**
 * 
 * @param productNode
 * @param siteId
 * @returns true if node is in siteId
 */
function isInSite(productNode, siteId) {
	if (productNode.qnamePath.indexOf("/app:company_home/st:sites/cm:" + siteId + "/") != -1) {
		return true;
	} 
	return false;
}

/**
 * 
 * @param productNode
 * @returns true if node is in user folder
 */
function isInUserFolder(productNode){
	if (productNode.qnamePath.indexOf("/app:company_home/app:user_homes") != -1) {
		return true;
	}
	return false;
	
}

/**
 * Warning not efficient at all!!
 * 
 * @param productNode
 * @returns true if node is in user folder
 */
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


/**
 * Create new branch of entity
 * @param node
 * @param dest
 * @param autoMerge optional default false
 * @returns void
 */
function createBranch(node, dest, autoMerge){
	bcpg.createBranch( node, dest, autoMerge);
}

/**
 * Merge branche
 * @param node
 * @param branchToNode can be null if autoMerge
 * @param description
 * @param type
 * @returns void
 */
function mergeBranch( node, branchToNode, description, type){
	bcpg.createBranch( node, branchToNode, description, type);
}


/**
 * Move node and rename if same name exists in destination
 * @param node
 * @param dest
 * @returns void
 */
function moveAndRename(node,dest){
	bcpg.moveAndRename(node,dest);
}


/**
 * @param prefix
 * @returns generate EAN13 code with autonum corresponding to prefix
 */
function generateEAN13Code(prefix){
	bcpg.generateEAN13Code(prefix);
}


/**
 * 
 * @param siteId
 * @returns document library folder for site
 */
function getDocumentLibraryNodeRef(siteId) {
	var site = siteService.getSite(siteId);
	return site.getContainer("documentLibrary");
}


/**
 * 
 * Set write permissions as system 
 * Example : bcpg.allowWrite(document,"GROUP_EVERYONE");
 * @param node
 * @param authority username or group name 
 * @returns void
 */
function allowWrite(node, authority) {
	bcpg.allowWrite(node, authority);
}


/**
 * 
 * Set read permissions as system 
 * Example : bcpg.allowRead(document,"GROUP_EVERYONE");
 * @param node
 * @param authority username or group name 
 * @returns void
 */
function allowRead(node, authority) {
	bcpg.allowRead(node, authority);
}



/**
 * Remove permissions set on node
 * @param document
 * @param inherit also remove inherit permission
 * @returns void
 */
function clearPermissions(node, inherit){
	node.setInheritsPermissions(inherit);
	for each(var permission in node.getDirectPermissions()) {
		node.removePermission(permission.split(';')[2],permission.split(';')[1]);
	}
}

/**
 * Remove specific group permissions
 * Example:  deleteGroupPermission(document, "GROUP_SG_FTEBIC_MANAGER");
 * @param node
 * @param group
 * @returns void
 */
function deleteGroupPermission(node, group){
	var inheritedPermissions = [];
	for each(var perm in node.getFullPermissions()){
		if (perm.split(';')[3] == "INHERITED"){
			inheritedPermissions.push(perm);
		}
	}
	node.setInheritsPermissions(false);
	for each(var perm in inheritedPermissions){
		if(perm.split(';')[1] != group){
			node.setPermission(perm.split(';')[2],perm.split(';')[1]);
		}
	}
}


/**
 * Copy one list from sourceNode to destNode
 * @param destNode
 * @param sourceNode
 * @param listQname
 * @returns void
 */
function copyList( destNode,  sourceNode,  listQname) {
	bcpg.copyList( destNode,  sourceNode,  listQname);
}

/**
 * 
 * @param node
 * @param listQname
 * @returns true if list exists and non empty
 */
function listExist( node,  listQname) {
	return bcpg.listExist( node,  listQname) ;

}

/**
 * 
 * @param node
 * @param listQname
 * @returns list items
 */
function listItems( node,  listQname) {
	return bcpg.getListItems( node,  listQname);
}




