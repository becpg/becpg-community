/*
 * Notification rule script, mode ALL: the daily recap of the documents a
 * supplier deposited outside a task (bcpg:documentSupplierDepositDate, set
 * for the types in "Notify" or "Validate" supplier update mode).
 *
 * The rule's own mail goes to its recipients; this script also writes to the
 * manager of the last supplier project of each entity concerned, who is the
 * person able to start the update.
 */

function owningEntity(node) {
	var entityRefs = node.assocs["bcpg:documentEntityRef"];
	return entityRefs != null && entityRefs.length > 0 ? entityRefs[0] : null;
}

function lastProjectManager(entity) {
	var projects = entity.sourceAssocs["pjt:projectEntity"];
	var last = null;
	for (var i = 0; projects != null && i < projects.length; i++) {
		var managers = projects[i].assocs["pjt:projectManager"];
		var created = projects[i].properties["cm:created"];
		if (managers != null && managers.length > 0 && (last == null || created.getTime() > last.created.getTime())) {
			last = { manager: managers[0], created: created };
		}
	}
	return last == null ? null : last.manager;
}

function groupByManager(documents) {
	var byManager = {};
	for (var i = 0; i < documents.length; i++) {
		var entity = owningEntity(documents[i]);
		var manager = entity == null ? null : lastProjectManager(entity);
		if (manager != null && manager.properties["cm:email"]) {
			var key = manager.properties["cm:email"];
			if (!byManager[key]) {
				byManager[key] = [];
			}
			byManager[key].push(entity.name + " : " + documents[i].name);
		}
	}
	return byManager;
}

function main() {
	var byManager = groupByManager(items);
	for (var email in byManager) {
		var mail = actions.create("mail");
		mail.parameters.to = email;
		mail.parameters.subject = notification.properties["bcpg:nrSubject"];
		mail.parameters.text = byManager[email].join("\n");
		mail.execute(notification);
	}
}

main();
