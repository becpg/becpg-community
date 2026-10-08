/*
 * Notification rule script, mode EACH: starts a supplier update project on the
 * entity the matched node belongs to.
 *
 * The matched node (`item`) is a document for the expiry rule and an entity for
 * the periodic review rule. The project template is the rule's
 * bcpg:nrProjectTpl. An entity that already has an open supplier project, or
 * whose last one was completed within the rule's delay (bcpg:nrTimeNumber
 * days), is left alone so that a daily rule never starts the same work twice.
 */

var DAY_MS = 24 * 60 * 60 * 1000;

function owningEntity(node) {
	if (node.isSubType("bcpg:entityV2")) {
		return node;
	}
	var entityRefs = node.assocs["bcpg:documentEntityRef"];
	if (entityRefs != null && entityRefs.length > 0) {
		return entityRefs[0];
	}
	var parent = node.parent;
	while (parent != null && !parent.isSubType("bcpg:entityV2")) {
		parent = parent.parent;
	}
	return parent;
}

function completedWithin(entity, days) {
	var projects = entity.sourceAssocs["pjt:projectEntity"];
	if (projects == null || days == null) {
		return false;
	}
	var since = new Date().getTime() - days * DAY_MS;
	for (var i = 0; i < projects.length; i++) {
		var completion = projects[i].properties["pjt:projectCompletionDate"];
		if (projects[i].properties["pjt:projectState"] == "Completed" && completion != null && completion.getTime() >= since) {
			return true;
		}
	}
	return false;
}

function main() {
	var templates = notification.assocs["bcpg:nrProjectTpl"];
	if (templates == null || templates.length == 0) {
		logger.warn("supplierUpdateProject.js: rule " + notification.name + " has no project template (bcpg:nrProjectTpl), nothing started");
		return;
	}

	var entity = owningEntity(item);
	if (entity == null || entity.hasAspect("bcpg:entityBranchAspect") || entity.hasAspect("bcpg:entityTplAspect")) {
		return;
	}
	if (bSupplier.hasOpenProject(entity) || completedWithin(entity, notification.properties["bcpg:nrTimeNumber"])) {
		return;
	}

	bSupplier.createSupplierProject([entity], templates[0], []);
}

main();
