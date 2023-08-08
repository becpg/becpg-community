<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">


function main() {

	// System properties
	model.sysBeCPGAttributes = {};
	["beCPG.charact.name",
		"beCPG.datalist.effectiveFilterEnabled",
		"beCPG.defaultSearchTemplate",
		"beCPG.multilinguale.disabledMLTextFields",
		"beCPG.multilinguale.shouldExtractMLText",
		"beCPG.multilinguale.supportedLocales",
		"beCPG.report.datasource.maxSizeInBytes",
		"beCPG.report.image.maxSizeInBytes",
		"beCPG.formulation.maxRclSourcesToKeep",
		"beCPG.spel.security.authorizedTypes",
		"beCPG.formulation.reqCtrlList.addChildRclSources"].forEach(function(p) {
			model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p) }

		}

		);



	model.tools = Admin.getConsoleTools("system-configuration");
	model.metadata = Admin.getServerMetaData();
}

main();