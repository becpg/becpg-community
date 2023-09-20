<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">


function main() {

	// System properties
	model.sysBeCPGAttributes = {};
	
	// Core properties
	[
		"beCPG.charact.name.format",
		"beCPG.datalist.effectiveFilterEnabled",
		"beCPG.defaultSearchTemplate",
		"beCPG.multilinguale.disabledMLTextFields",
		"beCPG.multilinguale.shouldExtractMLText",
		"beCPG.multilinguale.supportedLocales",
		"beCPG.report.datasource.maxSizeInBytes",
		"beCPG.report.image.maxSizeInBytes",
		"beCPG.spel.security.authorizedTypes",
		"beCPG.formulation.reqCtrlList.maxRclSourcesToKeep",
		"beCPG.formulation.reqCtrlList.addChildRclSources"
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "core" }
			});
				
	// Plm properties
	[
		"beCPG.formulation.reqCtrlList.maxRclSourcesToKeep",
		"beCPG.formulation.reqCtrlList.addChildRclSources"
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "plm"  }
			});
				
	// Project properties
	[
		
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "project"  }
			});

	model.tools = Admin.getConsoleTools("system-configuration");
	model.metadata = Admin.getServerMetaData();
}

main();