/**
 * beCPG console
 */

function getSystemInfos() {
	var json = remote.call("/becpg/admin/repository/system-entities");
	if (json.status == 200) {
		var obj = eval('(' + json + ')');
		if (obj) {
			return obj;
		}
	}
	return [];
}

var infos = getSystemInfos();

model.systemEntities = infos.items;
model.systemInfo = infos.systemInfo;
