<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/action/action.lib.js">


/**
 * Duplicate multiple items action
 * @method POST
 */

function getCompoList(product) {

	var res = null;
	var entityListsAssoc = product.childAssocs["bcpg:entityLists"];

	if (entityListsAssoc != null) {
		var entityLists = entityListsAssoc[0];

		for (var entityList in entityLists.children) {

			var currentChild = entityLists.children[entityList];

			if ("compoList" == currentChild.properties["cm:name"]) {
				res = currentChild;
			}
		}
	}


	return res;
}

/**
 * Gives the copies of one line keys strictly between its sort and the next integer sort,
 * in the order of the source list, so that they take the place of the replaced line.
 * Keys are kept in memory: the source list is never written.
 *
 * @param copiedChilds {array} copies of one line, as { nodeRef, oldSort }
 * @param parentSort {number} sort of the replaced line in the destination list
 * @param sortKeys {object} sort keys by copy nodeRef, filled by this function
 */
function assignSortKeys(copiedChilds, parentSort, sortKeys) {
	copiedChilds.sort(function(a, b) {
		return (a.oldSort || 0) - (b.oldSort || 0);
	});

	for (var i = 0; i < copiedChilds.length; i++) {
		sortKeys[copiedChilds[i].nodeRef] = (parentSort || 0) + (i + 1) / (copiedChilds.length + 1);
	}
}

/**
 * @param node {ScriptNode} line of the destination list
 * @param sortKeys {object} sort keys of the copies by nodeRef
 * @return {number} the key used to order the line in the destination list
 */
function getSortKey(node, sortKeys) {
	var key = sortKeys[node.nodeRef.toString()];
	if (key != null) {
		return key;
	}
	return node.properties["bcpg:sort"] || 0;
}

/**
 * @param results {array} action results
 * @param oldNodeRef {string} nodeRef of a source line
 * @return {string|null} nodeRef of the copy of this source line, null if it was not copied
 */
function findCopyNodeRef(results, oldNodeRef) {
	for (var i = 0; i < results.length; i++) {
		if (results[i].success && String(results[i].oldNodeRef) === String(oldNodeRef)) {
			return results[i].nodeRef;
		}
	}
	return null;
}

/**
 * @param sourceLine {ScriptNode} the source line
 * @param copiedNodes {object} copies already created, by source nodeRef
 * @return {ScriptNode|null} the copy of the source parent of the line, null when the line has no
 *         parent or its parent was not copied
 */
function getParentCopy(sourceLine, copiedNodes) {
	var parentLevel = sourceLine.properties["bcpg:parentLevel"];
	if (parentLevel != null) {
		var parentCopy = copiedNodes[parentLevel.nodeRef.toString()];
		if (parentCopy != null) {
			return parentCopy;
		}
	}
	return null;
}

/**
 * Gives the depth of a copy in the destination list, read from the parent level the copy is given:
 * one level below the copy of its source parent, or the depth of the replaced line when the copy
 * takes its place. A source line whose own parent was not copied, being out of the default variant,
 * thus stays consistent with the parent level it falls back on.
 *
 * @param parentCopy {ScriptNode|null} the copy of the source parent of the line
 * @param replacedLine {ScriptNode} the replaced line of the destination list
 * @return {number} the depth level of the copy
 */
function getCopyDepthLevel(parentCopy, replacedLine) {
	if (parentCopy == null) {
		return replacedLine.properties["bcpg:depthLevel"] || 1;
	}
	return (parentCopy.properties["bcpg:depthLevel"] || 1) + 1;
}

/**
 * @param compoList {ScriptNode} the compoList of the source product
 * @return {array} its compoList lines of the default variant, parents before their children
 */
function getSourceLines(compoList) {
	var sourceLines = [];
	for (var subIndex in compoList.children) {
		var currentChild = compoList.children[subIndex];
		if ("{http://www.bcpg.fr/model/becpg/1.0}compoList" == currentChild.type && isInDefaultVariant(currentChild)) {
			sourceLines.push(currentChild);
		}
	}
	sourceLines.sort(function(a, b) {
		return (a.properties["bcpg:sort"] || 0) - (b.properties["bcpg:sort"] || 0);
	});
	return sourceLines;
}

/**
 * @param line {ScriptNode} a compoList line
 * @return {boolean} true when the line has no variant or belongs to the default variant
 */
function isInDefaultVariant(line) {
	var variants = line.properties["bcpg:variantIds"];
	if (variants == null || variants.length == 0) {
		return true;
	}
	for (var vIdx in variants) {
		if (true == variants[vIdx].properties["bcpg:isDefaultVariant"]) {
			return true;
		}
	}
	return false;
}

/**
 * Copies the associations of a source line, pointing to the copies when the target was copied too.
 *
 * @param oldNode {ScriptNode} the source line
 * @param newNode {ScriptNode} the copy
 * @param results {array} action results
 */
function copyAssociations(oldNode, newNode, results) {
	for (var assocName in oldNode.assocs) {
		var assocs = oldNode.assocs[assocName];
		for (var j = 0, jj = assocs.length; j < jj; j++) {
			var copyNodeRef = findCopyNodeRef(results, assocs[j].nodeRef);
			newNode.createAssociation(copyNodeRef != null ? search.findNode(copyNodeRef) : assocs[j], assocName);
		}
	}
}

/**
 * Orders the destination list on the sort keys and renumbers it by steps of 10,
 * so that sorts are unique and follow the visual order.
 *
 * @param listNode {ScriptNode} the destination list
 * @param sortKeys {object} sort keys of the copies by nodeRef
 */
function renumberSorts(listNode, sortKeys) {
	var children = listNode.children;
	children.sort(function(a, b) {
		return getSortKey(a, sortKeys) - getSortKey(b, sortKeys);
	});

	for (var i = 0; i < children.length; i++) {
		children[i].properties["bcpg:sort"] = i * 10;
		children[i].save();
	}
}

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing items array
 * @return {object|null} object representation of action results
 */
function runAction(p_params) {
	var results = [],
		parentNode = p_params.rootNode,
		items = p_params.items,
		sortKeys = {},
		index, itemNode;

	// Must have parent node and array of items
	if (!parentNode) {
		status.setCode(status.STATUS_BAD_REQUEST, "No parent node supplied on URL.");
		return;
	}
	if (!items || items.length === 0) {
		status.setCode(status.STATUS_BAD_REQUEST, "No items supplied in JSON body.");
		return;
	}

	// Properties to skip when duplicating
	var propertiesToSkip =
	{
		"cm:name": true,
		"cm:content": true,
		"cm:created": true,
		"cm:creator": true,
		"cm:modified": true,
		"cm:modifier": true,
		"bcpg:variantIds" : true,
		"bcpg:sort": true,
		"fm:commentCount": true
	};


	for (index in items) {


		itemNode  = search.findNode(items[index]);

		if (itemNode !== null) {
			if ("{http://www.bcpg.fr/model/becpg/1.0}compoList" == itemNode.type) {
				var product = itemNode.associations["bcpg:compoListProduct"][0];
				var parentQty = itemNode.properties["bcpg:compoListQtySubFormula"];
                var parentQtyInKg = itemNode.properties["bcpg:compoListQty"];
			
				var compoList = getCompoList(product);
	
				if (compoList != null) {
					var copiedChilds = [];
					var copiedNodes = {};
					var itemParentLevel = itemNode.properties["bcpg:parentLevel"];
					var sourceLines = getSourceLines(compoList);

					for (var lineIndex = 0; lineIndex < sourceLines.length; lineIndex++) {
						var currentChild = sourceLines[lineIndex];
						var result =
						{
							nodeRef: currentChild.nodeRef.toString(),
							oldNodeRef: currentChild.nodeRef.toString(),
							action: "duplicateItem",
							success: false
						};

						var duplicateProperties = new Array(),
							propNames = currentChild.getPropertyNames(true),
							propName;

						// Copy selected properties from the original node
						for (var i = 0, ii = propNames.length; i < ii; i++) {
							propName = propNames[i];
							if (propName in propertiesToSkip || propName.indexOf("sys:") == 0) {
								continue;
							}
							duplicateProperties[propName] = currentChild.properties[propName];

						}
                            
                            duplicateProperties["bcpg:compoListQtySubFormula"] = bFormulation.computeDuplicateChildQty(itemNode, currentChild);

                            if(duplicateProperties["bcpg:compoListUnit"] == "Perc"){
                                duplicateProperties["bcpg:compoListUnit"]= "kg";
                            }
                            
                            var parentLossPerc = itemNode.properties["bcpg:compoListLossPerc"];
                            if(parentLossPerc ==null){
                                parentLossPerc = product.properties["bcpg:componentLossPerc"];
                            }
                            
                           if(parentLossPerc!=null ){
                                if(duplicateProperties["bcpg:compoListLossPerc"]!=null ){
                                     duplicateProperties["bcpg:compoListLossPerc"] = parentLossPerc + duplicateProperties["bcpg:compoListLossPerc"];
                                   } else {
                                    duplicateProperties["bcpg:compoListLossPerc"] = parentLossPerc;
                                }
                            }

						// Parent level, depth and a provisional sort are set at creation, so that the sortable list
						// policy does not reorder the copies at commit: renumberSorts gives their final sort
						// A copy hangs below the copy of its source parent or, when that parent was not copied,
						// takes the place of the replaced line: never below a line of the source product
						var parentCopy = getParentCopy(currentChild, copiedNodes);
						duplicateProperties["bcpg:parentLevel"] = parentCopy != null ? parentCopy : itemParentLevel;
						duplicateProperties["bcpg:depthLevel"] = getCopyDepthLevel(parentCopy, itemNode);
						duplicateProperties["bcpg:sort"] = itemNode.properties["bcpg:sort"] || 0;

						// Duplicate the node with a new GUID cm:name
						var newNode = parentNode.createNode(null, currentChild.type, duplicateProperties);
						if (newNode !== null) {
							result.nodeRef = newNode.nodeRef.toString();
							result.success = true;
							copiedChilds.push({ nodeRef: result.nodeRef, oldSort: currentChild.properties["bcpg:sort"] });
							copiedNodes[currentChild.nodeRef.toString()] = newNode;
						}


						results.push(result);
					}
					assignSortKeys(copiedChilds, itemNode.properties["bcpg:sort"], sortKeys);
					itemNode.remove();
					
				}

			}
		}



	}


	for (index in results) {
		if (results[index].success) {
			try {
				var oldNode = search.findNode(results[index].oldNodeRef);
				var newNode = search.findNode(results[index].nodeRef);
				if (newNode !== null && oldNode !== null) {
					copyAssociations(oldNode, newNode, results);
				}
			}
			catch (e) {
				results[index].success = false;
				logger.warn("duplicateChilds: cannot complete the copy of " + results[index].oldNodeRef + ": " + e);
			}
		}
	}

	renumberSorts(parentNode, sortKeys);

	return results;
}

/* Bootstrap action script */
main();
