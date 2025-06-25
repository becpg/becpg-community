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
 * orEmpty(value) returns value or empty if null
 * 
 * propValue(node, propName) alias getProp(node, propName) returns node property value or empty
 * 
 * getNode(node) returns node or get the node if node is a nodeRef string
 * 
 * mlPropValue(node, propName, locale, exactLocale? false) alias getMLProp(node, propName, locale, exactLocale? false) returns the node's multilingual property value for the specified locale
 * or empty if exactLocale = true else it will return the default locale
 * 
 * mlPropConstraint(propValue, propName, locale) alias getMLConstraint(propValue, propName, locale) display value or empty for multilingual constraint value
 * 
 * incrementAndGetAutoNumValue(autoNumClassName, propName) alias autoNumValue(autoNumClassName, propName) increments and return the autonum value of the property's classname provided
 * 
 * incrementAndGetAutoNumCounter(autoNumClassName, propName) alias autoNumCounter(autoNumClassName, propName) increments and return the counter value of the property's classname provided (without prefix)
 * 
 * getAutoNumNodeRef(autoNumClassName, propName) alias autoNumNodeRef(autoNumClassName, propName) returns the NodeRef of the counter for the property's classname provided
 * 
 * setAutoNumValue(autoNumClassName, propName, counter) sets the value of the counter of the autonum value for the property's classname provided
 * 
 * assocValue(node, assocName) returns first matching nodeRef for given assocName
 * 
 * assocValues(node, assocName) returns nodeRef array for given assocName
 * 
 * sourceAssocValues(node, assocName, maxResults, offset) returns nodeRef array for given source assocName
 * 
 * entitySourceAssocs(node, assocName, filter) returns entityNodeRef array for given source assocName
 * 
 * assocPropValues(node, assocName, propName) returns association property array of values
 * 
 * assocPropValue(node, assocName, propName) alias getAssoc(product, assocName, propName?) returns association property value
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
 * removeAssocs(node, assocName) remove association from given node
 * 
 * addAspectToNode(node, aspectName) Add aspect to a node
 * 
 * removeAspectToNode(node, aspectName) Remove aspect to a node
 * 
 * setValue(node, propName, value) Set property value checking if property changed, returns true if property has changed
 * 
 * setExtraValue(entity, propName, value) Set property value on repository entity
 * 
 * i18n(key, params?) returns i18n message for current locale
 * 
 * updateMLText(node, propQName, locale, value) Update multilingual value
 * 
 * cleanName(value) alias removeForbiddenChar(value) returns valid cm:name value
 * 
 * concatName(name, value, separator?) param separator default is " " returns concatenated value of name + separator + value
 * 
 * classifyByHierarchy(productNode, folderNode, propHierarchy?) classify node by hierarchy
 * 
 * classifyByDate(productNode, path, date, dateFormat, documentLibrary?) classify node by date
 * 
 * classifyByPropAndHierarchy(productNode, folderNode, propHierarchy?, propPathName?, locale?) classify node by prop and hierarchy
 * 
 * formulate(product) triggers formulation on product
 * 
 * getOrCreateFolder(folderNode, targetFolder) Get or create target folder if it doesn't exist in folderNode
 * 
 * getOrCreateFolderByPath(entity, path) Get or create folder in entity if given path is valid
 * 
 * isInSite(productNode, siteId) returns true if productNode is in siteId
 * 
 * isInUserFolder(productNode) returns true if productNode is in user folder
 * 
 * isInFolder(productNode, folderNode) true if productNode is in folderNode
 * 
 * getDocumentLibraryNodeRef(siteId) returns document library folder node for site
 * 
 * createBranch(node, dest, autoMerge?) Create new branch of entity
 * 
 * mergeBranch(node, branchToNode, description, type) branchToNode can be null if autoMerge
 * 
 * moveAndRename(node, dest) Move node and rename if same name exists in destination
 * 
 * getAvailableName(dest, name) Get an available name adding (n) if same name exists in destination
 * 
 * generateEAN13Code(prefix) returns generate EAN13 code with autonum corresponding to prefix
 * 
 * setPermissionAsSystem(node, permission, authority) Set permissions as system, node can be node, nodeRef or nodeRef string
 * 
 * allowWrite(node, authority) Set write permissions as system bypassing rights
 * 
 * getQNameTitle(qname) returns the qname title for the provided qname
 * 
 * allowRead(node, authority) Set read permissions as system bypassing rights
 * 
 * deleteGroupPermission(node, group) Remove specific group permissions
 * 
 * clearPermissions(node, inherit) Remove permissions set on node, as system, node can be node, nodeRef or nodeRef string
 * 
 * copyList(sourceNode, destNode, listQname) Copy one list from sourceNode to destNode
 * 
 * listExist(node, listQname) returns true if list exists and non empty
 * 
 * listItems(node, listQname) returns items nodeRef array
 * 
 * submitForm(entity, formDataJson) applies formulary data (properties and associations) to an entity
 * 
 * getEntityListFromNode(product, listName) returns entityList named listName from given product
 * 
 * toISO8601(dateObject, options) convert date toISO8601
 * 
 * isOnCreateEntity(node) Test we are creating entity
 * 
 * isOnMergeEntity(node) Test we are merging entity
 * 
 * isOnMergeMinorVersion(node) Test we are merging entity to minor version
 * 
 * isOnMergeMajorVersion(node) Test we are merging entity to major version
 * 
 * isOnCopyEntity(node) Test we are copying entity
 * 
 * isOnFormulateEntity(node) Test we are formulating entity
 * 
 * isOnBranchEntity(node) Test we are branching entity
 * 
 * getEntity(childNode, itemType) return first parent entity that is of type itemType
 */

const SIMULATION_SITE_ID = "simulation";
const VALID_SITE_ID = "valid";
const ARCHIVED_SITE_ID = "archived";
const SUPPLIER_PORTAL_SITE_ID = "supplier-portal";


/**
 * (alias isEmpty)
 *
 * @param {any} value
 * @returns {boolean} true if value is empty or null
 */
function isNullOrEmpty(value) {
	var strValue;
	return value == null || 
		(value === (strValue = String(value)) || 
		value instanceof String ? strValue.trim() : value).length === 0;
}

/**
 * (alias isNullOrEmpty)
 *
 * @param {any} value
 * @returns {boolean} true if value is empty or null
 */
function isEmpty(value) {
	return isNullOrEmpty(value);
}

/**
 * @param {any} value
 * @param {any} [defaultValue] the default value to use if value param is null
 * @returns {any} value or empty if null;
 */
function orEmpty(value, defaultValue) {
	return value != null ? value : (defaultValue != null ? defaultValue : "");
}

/**
 * (alias propValue)
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} propName
 * @returns {any} node property value or empty
 */
function getProp(node, propName) {
	return isEmpty(node) ? "" : orEmpty(getNode(node).properties[propName]);
}

/**
 * (alias getProp)
 *
 * @param {(ScriptNode|string)} node string
 * @param {string} propName
 * @returns {any} node property value or empty
 */
function propValue(node, propName) {
	return getProp(node, propName);
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @returns {ScriptNode} node or get the node if node param is a nodeRef string
 */
function getNode(node) {
	if (node.nodeRef != null) {
		return node;
	}
	return search.findNode(node);
}

/**
 * (alias mlPropValue)
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} propName
 * @param {string} locale
 * @param {boolean} exactLocale
 * @returns {string} node multilingual property value for given locale or empty
 */
function getMLProp(node, propName, locale) {
	return getMLProp(node, propName, locale, false);
}

function getMLProp(node, propName, locale, exactLocale) {
	if(!exactLocale){
		exactLocale = false;
	}
	return isEmpty(node) ? "" : orEmpty(bcpg.getMLProperty(getNode(node), propName, locale, exactLocale));
}

/**
 * (alias getMLProp)
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} propName
 * @param {string} locale
 * @param {boolean} exactLocale 
 * @returns {string} node multilingual property value for given locale or empty
 */
function mlPropValue(node, propName, locale) {
	return getMLProp(node, propName, locale, false);
}

function mlPropValue(node, propName, locale, exactLocale) {
	return getMLProp(node, propName, locale, exactLocale);
}

/**
 * (alias mlPropConstraint)
 *
 * @param {string} propValue
 * @param {string} propName
 * @param {string} locale
 * @returns {string} display value or empty for multilingual constraint value
 */
function getMLConstraint(propValue, propName, locale) {
	return isEmpty(propValue) ? "" : orEmpty(bcpg.getMLConstraint(propValue, propName, locale));
}

/**
 * (alias autoNumValue)
 *
 * @param {string} autoNumClassName
 * @param {string} propName
 * @returns {string} increments and return the autonum value of the property's classname provided
 */
function incrementAndGetAutoNumValue(autoNumClassName, propName) {
	return isEmpty(autoNumClassName) ? "" : orEmpty(bcpg.getAutoNumValue(autoNumClassName, propName));
}

/**
 * (alias autoNumCounter)
 *
 * @param {string} autoNumClassName
 * @param {string} propName
 * @returns {string} increments and return the counter value of the property's classname provided (without prefix)
 */
function incrementAndGetAutoNumCounter(autoNumClassName, propName) {
	return isEmpty(autoNumClassName) ? "" : orEmpty(bcpg.getAutoNumCounter(autoNumClassName, propName));
}

/**
 * (alias autoNumNodeRef)
 *
 * @param {string} autoNumClassName
 * @param {string} propName
 * @returns {string} the NodeRef of the counter for the property's classname provided
 */
function getAutoNumNodeRef(autoNumClassName, propName) {
	return isEmpty(autoNumClassName) ? "" : orEmpty(bcpg.getAutoNumNodeRef(autoNumClassName, propName));
}

/**
 *
 * @param {string} autoNumClassName
 * @param {string} propName
 * @param {number} counter
 * @returns {boolean} if the counter for the property's classname provided has been set to the desired value
 */
function setAutoNumValue(autoNumClassName, propName, counter) {
	return isEmpty(autoNumClassName) ? "" : orEmpty(bcpg.setAutoNumValue(autoNumClassName, propName, counter));
}

/**
 * (alias getMLConstraint)
 *
 * @param {string} propValue
 * @param {string} propName
 * @param {string} locale
 * @returns {string} display value or empty for multilingual constraint value
 */
function mlPropConstraint(propValue, propName, locale) {
	return getMLConstraint(propValue, propName, locale);
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @returns {NodeRef} first matching nodeRef for given assocName
 */
function assocValue(node, assocName) {
	return isEmpty(node) ? "" : orEmpty(bcpg.assocValue(node, assocName));
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @returns {NodeRef[]} nodeRef array for given assocName
 */
function assocValues(node, assocName) {
	return isEmpty(node) ? new Array() : orEmpty(bcpg.assocValues(node, assocName), new Array());
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} maxResults
 * @param {string} offset
 * @returns {NodeRef[]} nodeRef array for given source assocName
 */
function sourceAssocValues(node, assocName, maxResults, offset, includeVersions) {
	if (!maxResults) {
		maxResults = null;
	}
	if (!offset) {
		offset = null;
	}
	if (!includeVersions) {
		includeVersions = false;
	}
	return bcpg.sourceAssocValues(node, assocName, maxResults, offset, includeVersions);
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {object} filter
 * @returns boolean
 */
function hasEntitySourceAssocs(node, assocName, filter) {
	if (filter) {
		return bcpg.hasEntitySourceAssocs(node, assocName, JSON.stringify(filter));
	}
	return bcpg.hasEntitySourceAssocs(node, assocName, null);
}

function entitySourceAssocs(node, assocName, filter) {
	if (filter) {
		return bcpg.entitySourceAssocs(node, assocName, JSON.stringify(filter));
	}
	return bcpg.entitySourceAssocs(node, assocName, null);
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} propName
 * @returns {any[]} association property array of values
 */
function assocPropValues(node, assocName, propName) {
	return isEmpty(node) ? new Array() : orEmpty(bcpg.assocPropValues(node, assocName, propName), new Array());
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} assocAssocName
 * @returns {NodeRef[]} association association nodeRef array
 */
function assocAssocValues(node, assocName, assocAssocName) {
	return isEmpty(node) ? new Array() : orEmpty(bcpg.assocAssocValues(node, assocName, assocAssocName), new Array());
}

/**
 * (alias getAssoc)
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} propName
 * @returns {any} association property value
 */
function assocPropValue(node, assocName, propName) {
	return isEmpty(node) ? "" : orEmpty(bcpg.assocPropValue(node, assocName, propName));
}

/**
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} assocAssocName
 * @returns {NodeRef} association association nodeRef
 */
function assocAssocValue(node, assocName, assocAssocName) {
	return isEmpty(node) ? null : orEmpty(bcpg.assocAssocValue(node, assocName, assocAssocName));
}

/**
 * (alias assocPropValue)
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} [propName="cm:name"] optional, default propName value is "cm:name"
 * @returns {any} association property value
 */
function getAssoc(node, assocName, propName) {
	if (propName) {
		return assocPropValue(node, assocName, propName)
	}
	return assocPropValue(node, assocName, "cm:name")
}

/**
 * Update assoc with values
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {(ScriptNode|ScriptNode[]|NodeRef|NodeRef[])} values 
 *     if values is empty, array or null, assoc will be cleared before update
 * @returns {void}
 */
function updateAssoc(node, assocName, values) {
	bcpg.updateAssoc(node, assocName, values);
}

/**
 * Remove Associations
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @returns {void}
 */
function removeAssocs(node, assocName) {
	if (!isEmpty(node)) {
		bcpg.updateAssoc(node, assocName, null);
	}
}

/**
 * Add aspect to a node
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} aspectName
 */
function addAspectToNode(node, aspectName) {
    if (!getNode(node).hasAspect(aspectName)) {
        getNode(node).addAspect(aspectName);
    }
}

/**
 * Remove aspect of a node
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} aspectQName
 */
function removeAspectToNode(node, aspectName) {
    if (getNode(node).hasAspect(aspectName)) {
        getNode(node).removeAspect(aspectName);
    }
}



/**
 * Copy association property value to node property
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} propName
 * @param {string} nodePropName target node property name
 * @returns {boolean} true if property has changed
 */
function copyAssocPropValue(node, assocName, propName, nodePropName) {
	var value = assocPropValue(node, assocName, propName);
	return setValue(node, nodePropName, value);
}

/**
 * Set property value checking if property changed
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} propName
 * @param {any} value
 * @returns {boolean} true if property has changed
 */
function setValue(node, propName, value) {
	node = getNode(node);
	if (isEmpty(value) && node.properties[propName] != null) {
		delete node.properties[propName];
		return true;
	} else if (!isEmpty(value)) {
		if (node.properties[propName] !== value) {
			if (propName == "cm:name") {
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
 * Set property value on repository entity
 *
 * @param {RepositoryEntity} entity
 * @param {string} propName
 * @param {any} value
 * @returns {boolean} true
 */
function setExtraValue(entity, propName, value) {
	bcpg.setExtraValue(entity, propName, value);
	return true
}


/**
 * Copy association association value to node association
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} assocName
 * @param {string} assocAssocName
 * @param {string} nodeAssocName target node association name
 * @returns {void}
 */
function copyAssocAssocValue(node, assocName, assocAssocName, nodeAssocName) {
	updateAssoc(node, nodeAssocName, assocAssocValues(node, assocName, assocAssocName));
}

/**
 * @param {string} key
 * @param {any[]} [params] optional array of params
 * @returns {string} i18n message for current locale
 */
function i18n(key, params) {
	if (isEmpty(params)) {
		return bcpg.getMessage(key);
	}
	return bcpg.getMessage(key, params);
}

/**
 * Update multilingual value
 *
 * @param {ScriptNode} node
 * @param {string} propQName
 * @param {string} locale
 * @param {string} value
 * @returns {void}
 */
function updateMLText(node, propQName, locale, value) {
	bcpg.setMLProperty(node, propQName, locale, value);
}

/**
 * (alias cleanName)
 *
 * @param {string} value
 * @returns {string} valid cm:name value
 */
function removeForbiddenChar(value) {
	return isEmpty(value) ? "" : value.replace(/[|"<>.*?:+\/]/g, "").replace(/ -/g, "").trim();
}

/**
 * (alias removeForbiddenChar)
 *
 * @param {string} value
 * @returns {string} valid cm:name value
 */
function cleanName(value) {
	return removeForbiddenChar(value);
}

/**
 * @param {string} name
 * @param {string} value
 * @param {string} [separator=" "] optional, default separator is " "
 * @returns {string} concatenated value of name + separator + value
 */
function concatName(name, value, separator) {
	if (name != null) {
		if (!isEmpty(name) && !isEmpty(value)) {
			if (separator) {
				name += separator;
			} else {
				name += " ";
			}
		}
		if (value != null) {
			name += value;
		}
		return name;
	}
	return value;
}

/**
 * Classify node by hierarchy
 *
 * @param {ScriptNode} productNode
 * @param {ScriptNode} folderNode
 * @param {string} [?propHierarchy] optional
 * @returns {boolean} true if success
 */
function classifyByHierarchy(productNode, folderNode, propHierarchy) {
	if (propHierarchy) {
		return bcpg.classifyByHierarchy(productNode, folderNode, propHierarchy);
	}
	return bcpg.classifyByHierarchy(productNode, folderNode, null);
}

/**
 * Classify node by date
 *
 * @param {ScriptNode} productNode
 * @param {string} path
 * @param {Date} date
 * @param {string} dateFormat
 * @param {ScriptNode} [documentLibrary] optional
 * @returns {boolean} true if success
 */
function classifyByDate(productNode, path, date, dateFormat, documentLibrary) {
	if (isEmpty(documentLibrary)) {
		return bcpg.classifyByDate(productNode, path, date, dateFormat);
	}
	return bcpg.classifyByDate(productNode, documentLibrary, path, date, dateFormat);
}

/**
 * Triggers formulation on product
 *
 * @param {ScriptNode} product
 * @returns {void}
 */
function formulate(product) {
	bcpg.formulate(product);
}

/**
 * Classify node by prop and hierarchy (beta)
 *
 * @param {ScriptNode} productNode
 * @param {ScriptNode} folderNode classification start on this folderNode
 * @param {string} propHierarchy
 * @param {string} propPathName correspond to a property of 'productNode'
 *     or a property of a assocs of productNode, exemple: "bcpg:plant|bcpg:geoOrigine|bcpg:isoCode".
 * @param {string} locale if 'propPathName' is a ML Property,
 *     locale specifies which language to use to naming subfolder.
 * @returns {void}
 */
function classifyByPropAndHierarchy(productNode, folderNode, propHierarchy, propPathName, locale) {
	return bcpg.classifyByPropAndHierarchy(productNode, folderNode, propHierarchy, propPathName, locale);
}


/**
 * Get or create target folder if it doesn't exist in folderNode
 *
 * @param {ScriptNode} folderNode
 * @param {string} targetFolder target folder name
 * @returns {ScriptNode} target folder node
 */
function getOrCreateFolder(folderNode, targetFolder) {

	if (folderNode.childByNamePath(targetFolder)) {
		return folderNode.childByNamePath(targetFolder);
	} else {
		return folderNode.createFolder(targetFolder);
	}
}

/**
 * Get or create folder in entity if given path is valid
 *
 * @param {ScriptNode} entity
 * @param {string} path target folder path
 * @returns {ScriptNode} target folder node
 */
function getOrCreateFolderByPath(entity, path) {

	var index = path.indexOf("/");

	if (index == -1) {
		var folder = entity.childByNamePath(bcpg.getTranslatedPath(path));

		if (folder == null) {
			folder = entity.createFolderPath(bcpg.getTranslatedPath(path));
		}

		return folder;
	} else {
		var firstPath = path.substring(0, index);

		var lastPath = path.substring(index + 1);

		var folder = entity.childByNamePath(bcpg.getTranslatedPath(firstPath));

		if (folder == null) {
			folder = entity.createFolderPath(bcpg.getTranslatedPath(firstPath));
		}

		return getOrCreateFolderByPath(folder, lastPath);
	}
}

/**
 * @param {ScriptNode} productNode
 * @param {string} siteId
 * @returns {boolean} true if productNode is in siteId
 */
function isInSite(productNode, siteId) {
	if (productNode.qnamePath.indexOf("/app:company_home/st:sites/cm:" + siteId + "/") != -1) {
		return true;
	}
	return false;
}

/**
 * @param {ScriptNode} productNode
 * @returns {boolean} true if productNode is in user folder
 */
function isInUserFolder(productNode) {
	if (productNode.qnamePath.indexOf("/app:company_home/app:user_homes") != -1) {
		return true;
	}
	return false;

}

/**
 * Warning not efficient at all!!
 * 
 * @param {ScriptNode} productNode
 * @returns {boolean} true if productNode is in folderNode
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
 *
 * @param {ScriptNode} node
 * @param {ScriptNode} dest
 * @param {boolean} autoMerge
 * @returns {void}
 */
function createBranch(node, dest, autoMerge) {
	bcpg.createBranch(node, dest, autoMerge);
}

/**
 * Merge branch
 *
 * @param {ScriptNode} node
 * @param {ScriptNode} branchToNode can be null if autoMerge
 * @param {string} description
 * @param {string} type
 * @returns {void}
 */
function mergeBranch(node, branchToNode, description, type) {
	bcpg.mergeBranch(node, branchToNode, description, type);
}

/**
 * Get an available name adding (n) if same name exists in destination
 *
 * @param {ScriptNode} dest
 * @param {string} name
 * @returns {string} available name
 */
function getAvailableName(dest, name) {
	return bcpg.getAvailableName(dest, name);
}

/**
 * Move node and rename if same name exists in destination
 *
 * @param {ScriptNode} node
 * @param {ScriptNode} dest
 * @returns {void}
 */
function moveAndRename(node, dest) {
	bcpg.moveAndRename(node, dest);
}

/**
 * Generate EAN13 code with autonum corresponding to prefix
 *
 * @param {string} prefix
 * @returns {void}
 */
function generateEAN13Code(prefix) {
	bcpg.generateEAN13Code(prefix);
}

/**
 * @param {string} siteId
 * @returns {ScriptNode} document library folder node for site
 */
function getDocumentLibraryNodeRef(siteId) {
	return bcpg.getDocumentLibraryNodeRef(siteId);
}

/**
 * Set permissions as system
 * @example bcpg.setPermissionAsSystem(document, "Consumer", "GROUP_EVERYONE");
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} permission
 * @param {string} authority username or group name
 * @returns {void}
 */
function setPermissionAsSystem(node, permission, authority) {
	bcpg.setPermissionAsSystem(node, permission, authority);
}

/**
 * Set write permissions as system bypassing rights
 * @example bcpg.allowWrite(document, "GROUP_EVERYONE");
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} authority username or group name
 * @returns {void}
 */
function allowWrite(node, authority) {
	bcpg.allowWrite(node, authority);
}

/**
 * @param {string} qname
 * @returns {string} the qname title for the provided qname
 */
function getQNameTitle(qname) {
	return bcpg.getQNameTitle(qname);
}

/**
 * Set read permissions as system bypassing rights
 * @example bcpg.allowRead(document, "GROUP_EVERYONE");
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} authority username or group name
 * @returns {void}
 */
function allowRead(node, authority) {
	bcpg.allowRead(node, authority);
}

/**
 * Remove permissions set on node, as system
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {boolean} inherit also remove inherit permission
 * @returns {void}
 */
function clearPermissions(node, inherit) {
	bcpg.clearPermissions(node, inherit);
}

/**
 * Remove specific group permissions
 * @example deleteGroupPermission(document, "GROUP_SG_FTEBIC_MANAGER");
 *
 * @param {(ScriptNode|NodeRef|string)} node
 * @param {string} group
 * @returns {void}
 */
function deleteGroupPermission(node, group) {
	bcpg.deleteGroupPermission(node, group);
}

/**
 * Copy one list from sourceNode to destNode
 *
 * @param {ScriptNode} sourceNode
 * @param {ScriptNode} destNode
 * @param {string} listQname
 * @returns {void}
 */
function copyList(sourceNode, destNode, listQname) {
	bcpg.copyList(sourceNode, destNode, listQname);
}

/**
 * @param {ScriptNode} node
 * @param {string} listQname
 * @returns {boolean} true if list exists and non empty
 */
function listExist(node, listQname) {
	return bcpg.listExist(node, listQname);
}

/**
 * @param {ScriptNode} node
 * @param {string} listQname
 * @returns {NodeRef[]} items nodeRef array
 */
function listItems(node, listQname) {
	return bcpg.getListItems(node, listQname);
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
function toISO8601(dateObject, options) {
	var _ = function (n) { return (n < 10) ? "0" + n : n; };

	options = options || {};
	var formattedDate = [];
	var getter = options.zulu ? "getUTC" : "get";
	var date = "";
	if (options.selector != "time") {
		var year = dateObject[getter + "FullYear"]();
		date = ["0000".substr((year + "").length) + year, _(dateObject[getter + "Month"]() + 1), _(dateObject[getter + "Date"]())].join('-');
	}
	formattedDate.push(date);
	if (options.selector != "date") {
		var time = [_(dateObject[getter + "Hours"]()), _(dateObject[getter + "Minutes"]()), _(dateObject[getter + "Seconds"]())].join(':');
		var millis = dateObject[getter + "Milliseconds"]();
		if (options.milliseconds === undefined || options.milliseconds) {
			time += "." + (millis < 100 ? "0" : "") + _(millis);
		}
		if (options.zulu) {
			time += "Z";
		}
		else if (options.selector != "time") {
			var timezoneOffset = dateObject.getTimezoneOffset();
			var absOffset = Math.abs(timezoneOffset);
			time += (timezoneOffset > 0 ? "-" : "+") +
				_(Math.floor(absOffset / 60)) + ":" + _(absOffset % 60);
		}
		formattedDate.push(time);
	}
	return formattedDate.join('T'); // String
}

/**
 * Applies formulary data (properties and associations) to an entity
 *
 * @param {ScriptNode} entity
 * @param {JSON} formDataJson
 * @returns {void}
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

/**
 * @example getEntityListFromNode(product, "compoList");
 *
 * @param {ScriptNode} product
 * @param {string} listName
 * @returns {ScriptNode} entityList named listName from given product
 */
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

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are creating entity
 */
function isOnCreateEntity(node) {
	return bState.isOnCreateEntity(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are merging entity
 */
function isOnMergeEntity(node) {
	return bState.isOnMergeEntity(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are merging entity to minor version
 */
function isOnMergeMinorVersion(node) {
	return bState.isOnMergeMinorVersion(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are merging entity to major version
 */
function isOnMergeMajorVersion(node) {
	return bState.isOnMergeMajorVersion(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are copying entity
 */
function isOnCopyEntity(node) {
	return bState.isOnCopyEntity(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are formulating entity
 */
function isOnFormulateEntity(node) {
	return bState.isOnFormulateEntity(node);
}

/**
 * @param {(ScriptNode|NodeRef)} node
 * @returns {boolean} true if we are branching entity
 */
function isOnBranchEntity(node) {
	return bState.isOnBranchEntity(node);
}

function deleteExternalUser(userNode, supplierNode) {
	bSupplier.deleteExternalUser(userNode, supplierNode);
}

/**
 * @param {ScriptNode} childNode
 * @param {string} itemType
 * @returns {ScriptNode} first parent entity that is of type itemType
 */
function getEntity(childNode, itemType) {
	return bcpg.getEntity(childNode, itemType);
}

