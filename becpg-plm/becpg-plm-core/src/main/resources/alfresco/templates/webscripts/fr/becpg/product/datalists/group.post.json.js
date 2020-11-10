<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/action/action.lib.js">

function runAction(p_params) {
	var results = [],
		parentNode = p_params.rootNode,
		items = p_params.items,
		index, itemNode, result, nodeRef, toGroupItem = null, grouped = {};

	// Must have parent node and array of items
	if (!parentNode) {
		status.setCode(status.STATUS_BAD_REQUEST, "No parent node supplied on URL.");
		return;
	}

	if (args.allPages && args.allPages == "true" && args.queryExecutionId != null) {
		items = bcpg.getSearchResults(args.queryExecutionId);
	}


	if (!items || items.length === 0) {
		status.setCode(status.STATUS_BAD_REQUEST, "No items supplied in JSON body.");
		return;
	}

	items.forEach(item => {
		itemNode = search.findNode(item);

		if (itemNode !== null && "{http://www.bcpg.fr/model/becpg/1.0}compoList" == itemNode.type) {
			var product = itemNode.associations["bcpg:compoListProduct"][0];

			if (!grouped[product.nodeRef]) {
				grouped[product.nodeRef] = new Array();
			}
			grouped[product.nodeRef].push(itemNode);
		}
	})

	for (var prop in grouped) {
		if (grouped[prop].length >= 2) {

		    toGroupItem = null;

			for (index in grouped[prop]) {
				itemNode = grouped[prop][index];
				nodeRef = itemNode.nodeRef;
				result =
				{
					nodeRef: nodeRef.toString(),
					oldNodeRef: nodeRef.toString(),
					action: "groupItems",
					success: false
				};

				try {

					if (toGroupItem == null) {
						toGroupItem = itemNode;
						result.success = true;
					} else {
							var unit1 = itemNode.properties["bcpg:compoListUnit"];
							var unit2 = toGroupItem.properties["bcpg:compoListUnit"];
							if(unit1 == unit2){
							result.nodeRef = toGroupItem.nodeRef.toString();
							var qty1 = toGroupItem.properties["bcpg:compoListQtySubFormula"];
							var qty2 = itemNode.properties["bcpg:compoListQtySubFormula"];
							if (qty1 != null && qty2 != null) {
								toGroupItem.properties["bcpg:compoListQtySubFormula"] = qty1 + qty2;
							}
							itemNode.remove();
						}
						result.success = true;
					}

				}
				catch (e) {
					result.success = false;
				}

				results.push(result);
			}

			toGroupItem.save();

		}
	}

	return results;
}

/* Bootstrap action script */
main();
