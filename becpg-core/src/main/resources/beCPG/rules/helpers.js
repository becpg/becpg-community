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
 * sourceAssocValues(node, assocName) assoc nodeRef arrays
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
 * setValue(node, propName, value) Set property value checking if property changed returns if property has changed
 * 
 * setExtraValue(entity, propName, value) Set property value on repositoryEntity
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
 * classifyByPropAndHierarchy(productNode, folderNode, propHierarchy?, propPathName?, locale?) classify node by prop and hierarchy 
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
 * getAvailableName(dest, name) Get an available name adding (n) if same name exists in destination
 * 
 * generateEAN13Code(prefix) returns generate EAN13 code with autonum corresponding to prefix
 * 
 * setPermissionAsSystem(node, permission, authority) Set permissions as system, node can be node or nodeRef
 * 
 * allowWrite(node, authority) Set write permissions as system bypassing rights
 * 
 * allowRead(node, authority) Set read permissions as system bypassing rights
 *  
 * deleteGroupPermission(node, group) Remove specific group permissions
 *  
 * clearPermissions(node, inherit) Remove permissions as system set on node, node can be node or nodeRef
 * 
 * copyList( sourceNode, destNode ,  listQname)  Copy one list from sourceNode to destNode
 * 
 * listExist( node,  listQname) returns true if list exists and non empty
 * 
 * listItems( node,  listQname) returns list items
 * 
 * toISO8601(dateObject, options) convert date toISO8601
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
	return value == null || value == "";
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
	return value!=null ? value : (defaultValue != null? defaultValue : "");
}


/**
 * 
 * @param node or nodeRef
 * @param propName
 * @returns node property value or empty
 */
function getProp(node, propName) {
	return isEmpty(node) ?  "" : orEmpty(getNode(node).properties[propName]) ;
}

function propValue(node, propName){
	return getProp(node,propName);
}


function getNode(node){
	if(node.nodeRef != null){
		return node;
	}
	return search.findNode(node);
}

/**
 * 
 * @param node or nodeRef
 * @param propName
 * @param locale
 * @returns node multilingual property value or empty for locale
 */
function getMLProp(node, propName, locale) {
	return isEmpty(node) ?  "" : orEmpty( bcpg.getMLProperty(getNode(node), propName, locale));
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
 * @returns association nodeRef array
 */
function sourceAssocValues(node, assocName) {
	return isEmpty(node) ?  new Array() : orEmpty( bcpg.sourceAssocValues(node, assocName),new Array());
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
	return isEmpty(node) ?  "" : orEmpty( bcpg.assocPropValue(node, assocName, propName));
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
function getAssoc(node, assocName, propName) {
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
	bcpg.updateAssoc(node, assocName, values);
}

/**
 * Remove Associations
 * @param node
 * @param assocName
 * @returns void
 */
function removeAssocs(node, assocName){
	if(!isEmpty(node)){
		bcpg.updateAssoc(node, assocName, null);
	}
}


/**
 * Copy association property value to node property
 * @param node
 * @param assocName
 * @param propName
 * @param nodePropName
 * @returns true if property has changed
 */
function copyAssocPropValue(node, assocName, propName, nodePropName) {
	var value = assocPropValue(node, assocName, propName);
	return setValue(node,nodePropName, value );
}

/**
 * Set property value checking if property changed
 * @param node
 * @param propName
 * @param value
 * @returns if property has changed
 */
function setValue(node, propName, value){
	node = getNode(node);
	if(isEmpty(value) && node.properties[propName]!=null){
	    delete node.properties[propName];
	    return true;
	} else if(!isEmpty(value)) {
		if(node.properties[propName] !== value){
			if(propName == "cm:name"){
				node.name = value;
			} else {
				node.properties[propName] = value;
			}
			return true;
		}
	}
	return false;
}


/**
 * Set property value on entity
 * @param entity
 * @param propName
 * @param value
 * @returns if property has changed
 */
function setExtraValue(entity, propName, value){
	bcpg.setExtraValue(entity,propName,value);
	return true
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
	return bcpg.getMessage(key);
}


/**
 * 
 * @param key
 * @param params
 * @returns i18n message for current locale
 */
function i18n(key, params){
	return bcpg.getMessage(key, params);
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
	bcpg.setMLProperty(node, propQName, locale, value);
}


/**
 * @param value
 * @returns valid cm:name value
 */
function removeForbiddenChar(value) {
	return isEmpty(value) ?  "" : value.replace(/[|"<>.*?:+\/]/g, "").replace(/ -/g, "").trim();
}

function cleanName(value) {
	return removeForbiddenChar(value);
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
	if (propHierarchy) {
		return bcpg.classifyByHierarchy(productNode, folderNode, propHierarchy);
	}
	return bcpg.classifyByHierarchy(productNode, folderNode, null);
	
}

function classifyByDate(productNode, path, dateFormat, propDate) {
	return bcpg.classifyByDate(productNode, path, dateFormat, propDate);
}

function formulate(product) {
	bcpg.formulate(product);
}


/**
* Classify node by Hierarchy
* @param productNode
* @param folderNode Classification start on this folderNode
* @param propHierarchy
* @param propPathName correspond to a property of 'productNode' or a property of a assocs of productNode, exemple: "bcpg:plant|bcpg:geoOrigine|bcpg:isoCOde".
* @param locale if 'propPathName' is a ML Property, locale specifies which language to use to naming subfolder.
* @returns void
*/
//beta
function classifyByPropAndHierarchy(productNode, folderNode, propHierarchy, propPathName, locale) {
	return bcpg.classifyByPropAndHierarchy(productNode, folderNode, propHierarchy, propPathName, locale);
}

function classifyByPropAndHierarchy_extractAssoc(node, assocName, assocsArray) {

	if (assocsArray.length == 0) {
		return node;
	}
	var nextAssocName = assocsArray.shift(), nextNode = assocValue(node, assocName);

	if (nextNode == "") {
		return "";
	}

	classifyByPropAndHierarchy_extractAssoc(nextNode, nextAssocName, assocsArray);
}

function getOrCreateFolder(folderNode, targetFolder) {

	if (folderNode.childByNamePath(targetFolder)) { 
		return folderNode.childByNamePath(targetFolder);
	} else { 
		return folderNode.createFolder(targetFolder);
	}
}

function getOrCreateFolderByPath(entity, path) {
	var folder = entity.childByNamePath(bcpg.getTranslatedPath(path));
		
	if (folder == null) {
		folder = entity.createFolderPath(bcpg.getTranslatedPath(path));
	}
	
	return folder;
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
	bcpg.mergeBranch( node, branchToNode, description, type);
}


/**
 * Get an available name adding (n) if same name exists in destination
 * @param dest
 * @param name
 * @returns void
 */
function getAvailableName(dest, name){
	return bcpg.getAvailableName(dest, name);
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
	return bcpg.getDocumentLibraryNodeRef(siteId);
}


/**
 * 
 * Set permissions as system 
 * Example : bcpg.setPermissionAsSystem(document, "Consumer","GROUP_EVERYONE");
 * @param node
 * @param authority username or group name
 * @param permission  
 * @returns void
 */
function setPermissionAsSystem(node, permission, authority) {
	bcpg.setPermissionAsSystem(node, permission, authority);
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
	bcpg.clearPermissions(node, inherit);
}

/**
 * Remove specific group permissions
 * Example:  deleteGroupPermission(document, "GROUP_SG_FTEBIC_MANAGER");
 * @param node
 * @param group
 * @returns void
 */
function deleteGroupPermission(node, group){
	bcpg.deleteGroupPermission(node, group);
}


/**
 * Copy one list from sourceNode to destNode
 * @param destNode
 * @param sourceNode
 * @param listQname
 * @returns void
 */
function copyList( sourceNode,  destNode,  listQname) {
	bcpg.copyList( sourceNode,  destNode,  listQname);
}

/**
 * 
 * @param node
 * @param listQname
 * @returns true if list exists and non empty
 */
function listExist( node,  listQname) {
	return bcpg.listExist( node,  listQname);

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



/**
 * Converts a JavaScript native Date object into a ISO8601-formatted string
 *
 * Original code:
 *    dojo.date.stamp.toISOString
 *    Copyright (c) 2005-2008, The Dojo Foundation
 *    All rights reserved.
 *    BSD license (http://trac.dojotoolkit.org/browser/dojo/trunk/LICENSE)
 *
 * @method toISO8601
 * @param dateObject {Date} JavaScript Date object
 * @param options {object} Optional conversion options
 *    zulu = true|false
 *    selector = "time|date"
 *    milliseconds = true|false
 * @return {string}
 * @static
 */

function  toISO8601(dateObject, options) {
		 var _ = function(n){ return (n < 10) ? "0" + n : n; };
		
         options = options || {};
         var formattedDate = [];
         var getter = options.zulu ? "getUTC" : "get";
         var date = "";
         if (options.selector != "time")
         {
            var year = dateObject[getter+"FullYear"]();
            date = ["0000".substr((year+"").length)+year, _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
         }
         formattedDate.push(date);
         if (options.selector != "date")
         {
            var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
            var millis = dateObject[getter+"Milliseconds"]();
            if (options.milliseconds === undefined || options.milliseconds)
            {
               time += "."+ (millis < 100 ? "0" : "") + _(millis);
            }
            if (options.zulu)
            {
               time += "Z";
            }
            else if (options.selector != "time")
            {
               var timezoneOffset = dateObject.getTimezoneOffset();
               var absOffset = Math.abs(timezoneOffset);
               time += (timezoneOffset > 0 ? "-" : "+") +
                     _(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
            }
            formattedDate.push(time);
         }
         return formattedDate.join('T'); // String
}

/**
 * Applies formulary data (properties and associations) to an entity
 *
 * @method submitForm
 * @param entity {ScriptNode}
 * @param formDataJson {json object}
 * @static
 */
function submitForm(entity, formDataJson) {
	for (var key in formDataJson) {
		if (key.startsWith("prop_")) {
			var prop = key.split("prop_")[1].replace("_", ":");
			entity.properties[prop] = formDataJson[key];
			entity.save();
		} else if (key.startsWith("assoc_")) {
			var assoc = key.split("assoc_")[1];

			if (assoc.endsWith("_added")) {
				assoc = assoc.split("_added")[0].replace("_", ":");

				if (formDataJson[key] != "") {
					
					var splitted = formDataJson[key].split(",");
					
					for (var value in splitted) {
						var sNode = search.findNode(splitted[value]);
						entity.createAssociation(sNode, assoc);
					}
				}
			} else if (assoc.endsWith("_removed")) {
				assoc = assoc.split("_removed")[0].replace("_", ":");

				if (formDataJson[key] != "") {
					
					var splitted = formDataJson[key].split(",");
					
					for (var value in splitted) {
						var sNode = search.findNode(splitted[value]);
						entity.removeAssociation(sNode, assoc);
					}
				}
			}
		}
	}
}

function getEntityListFromNode(product, listName) {
    var entityList = null;
    if (product && product.childAssocs["bcpg:entityLists"]) {
        var entityLists = product.childAssocs["bcpg:entityLists"][0];
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


