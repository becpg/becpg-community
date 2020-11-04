<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/datalists/action/action.lib.js">

function runAction(p_params) {
	var results = [],
		parentNode = p_params.rootNode,
		items = p_params.items,
		index, itemNode, result, nodeRef;

	// Must have parent node and array of items
	if (!parentNode) {
		status.setCode(status.STATUS_BAD_REQUEST, "No parent node supplied on URL.");
		return;
	}
	if (!items || items.length === 0) {
		status.setCode(status.STATUS_BAD_REQUEST, "No items supplied in JSON body.");
		return;
	}


	var toGroupItem = null;

	for (index in items) {

		nodeRef = items[index];
		result =
		{
			nodeRef: nodeRef,
			oldNodeRef: items[index],
			action: "groupItems",
			success: false
		};


		try {
			itemNode = search.findNode(nodeRef);
			if (itemNode !== null) {
				if (toGroupItem == null) {
					toGroupItem = itemNode;
					result.success = true;
				} else {
					if ("{http://www.bcpg.fr/model/becpg/1.0}compoList" == itemNode.type) {
						result.nodeRef = toGroupItem.nodeRef.toString();
						var qty1 = toGroupItem.properties["bcpg:compoListQtySubFormula"];
						var qty2 = itemNode.properties["bcpg:compoListQtySubFormula"];
						if(qty1!=null && qty2!=null){
							toGroupItem.properties["bcpg:compoListQtySubFormula"]= qty1+qty2;
						}
						itemNode.remove();
						result.success = true;
					}
				}

		}

}
		catch (e) {
			result.success = false;
		}

		results.push(result);
	}

	toGroupItem.save();

	return results;
}

/* Bootstrap action script */
main();
