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
		index, itemNode, nodeRef;

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

					for (var subIndex in compoList.children) {
						var currentChild = compoList.children[subIndex];
						if ("{http://www.bcpg.fr/model/becpg/1.0}compoList" == currentChild.type) {

							var variants = currentChild.properties["bcpg:variantIds"];
							if(variants!=null && variants.length > 0){
								var isDefault = false;
								for(var vIdx in variants ){
									var variant = variants[vIdx];
									if(true == variant.properties["bcpg:isDefaultVariant"]){
										isDefault = true;
									}
								}	
									
								if(!isDefault){
									continue;
								}
							}
							
							var result =
							{
								nodeRef: currentChild.nodeRef.toString(),
								oldNodeRef: currentChild.nodeRef.toString(),
								parentSort:  itemNode.properties["bcpg:sort"],
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

							// Duplicate the node with a new GUID cm:name
							var newNode = parentNode.createNode(null, currentChild.type, duplicateProperties);
							if (newNode !== null) {
								result.nodeRef = newNode.nodeRef.toString();
								result.success = true;
							}


							results.push(result);
						}
					}
					itemNode.remove();
					
				}

			}
		}



	}


	for (index in results) {
		nodeRef = results[index].nodeRef;
		if (results[index].success) {
			try {
				var oldNode = search.findNode(results[index].oldNodeRef)
				itemNode = search.findNode(nodeRef);
				if (itemNode !== null && oldNode != null) {
					var parentLevel = itemNode.properties["bcpg:parentLevel"];
					if (parentLevel != null) {
						for (index2 in results) {
							var tmp = parentLevel.nodeRef;
							var tmp2 = results[index2].oldNodeRef;
							if (String(tmp) === String(tmp2)) {
								itemNode.properties["bcpg:parentLevel"] = search.findNode(results[index2].nodeRef);
								itemNode.save();
								break;
							}
						}
					}


					// set proper sorting

					var oldSort = oldNode.properties["bcpg:sort"];

					//only do it on nodes with sort attribute
					if (oldSort != null) {

						//make sure the next item hasn't got sort+1, otherwise multiply all sorts by 10
						var dataList = oldNode.parent;

						//get all compoList items with this duplicated node as a parent
						var childNodes = search.luceneSearch("+@bcpg\\:parentLevel:\"" + oldNode.nodeRef + "\"");


						//if there are any, set oldSort to biggest sort of these children
						for (var childIndex in childNodes) {
							if (childNodes[childIndex].properties["bcpg:sort"] > oldSort) {
								oldSort = childNodes[childIndex].properties["bcpg:sort"];
							}
						}

						//we have compoList, check all children's sort values and see if one has sort+1
						var shouldMultiplySortByTen = false;

						//find smallest sort > oldSort
						for (var childIndex in dataList.children) {
							if (!shouldMultiplySortByTen && dataList.children[childIndex].nodeRef != oldNode.nodeRef && dataList.children[childIndex].properties["bcpg:sort"] == oldSort + 1) {
								shouldMultiplySortByTen = true;
							}
						}

						if (shouldMultiplySortByTen) {
							for (var childIndex in dataList.children) {
								dataList.children[childIndex].properties["bcpg:sort"] = dataList.children[childIndex].properties["bcpg:sort"] * 10;
								dataList.children[childIndex].save();
							}
						}


						if(results[index].parentSort!=null ){
							itemNode.properties["bcpg:sort"] = (results[index].parentSort*1000) +oldSort;
						} else {
							itemNode.properties["bcpg:sort"] = 1 + oldSort;
						}

						delete results[index].parentSort;
						
						itemNode.save();
					}

					// Now copy any associations
					for (var idxAssoc in oldNode.assocs) {
						var assocs = oldNode.assocs[idxAssoc];
						for (var j = 0, jj = assocs.length; j < jj; j++) {
							var assocNode = assocs[j];

							for (index2 in results) {
								var tmp = assocNode.nodeRef;
								var tmp2 = results[index2].oldNodeRef;
								if (String(tmp) === String(tmp2)) {
									assocNode = search.findNode(results[index2].nodeRef);
									break;
								}

							}
							itemNode.createAssociation(assocNode, idxAssoc);
						}
					}
				}

			}
			catch (e) {
				result.success = false;
			}
		}
	}

	// Sort the list based on the sort values
	parentNode.children.sort(function(a, b) {
	    // Handle cases where bcpg:sort is null
	    var sortA = a.properties["bcpg:sort"] || 0;
	    var sortB = b.properties["bcpg:sort"] || 0;
	    return sortA - sortB;
	});
	
	// Increment the sorting values by 10
	for (var i = 0; i < parentNode.children.length; i++) {
	    var child = parentNode.children[i];
	    child.properties["bcpg:sort"] = (i * 10);
	    child.save();
	}
	
	return results;
}

/* Bootstrap action script */
main();
