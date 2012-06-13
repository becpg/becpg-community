/**
 * beCPG console
 */

function getSystemEntities() {
	var json = remote.call("/becpg/admin/repository/system-entities");
	if (json.status == 200) {
		var obj = eval('(' + json + ')');
		if (obj) {
			return obj.items;
		}
	}
	return [];
}

model.systemEntities = getSystemEntities();