<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">


function main() {

	// System properties
	model.sysBeCPGAttributes = {};
	
	[
		"beCPG.multilinguale.supportedLocales",
		"beCPG.currency.supported",
		"beCPG.multilinguale.disabledMLTextFields",
		"beCPG.datalist.effectiveFilterEnabled",
		"beCPG.spel.security.authorizedTypes",
		"beCPG.copyOrBranch.propertiesToReset",
		"beCPG.copyOrBranch.typesToReset",
		"mail.logo.url",
		"beCPG.version.cleaner.maxProcessedNodes",
		"beCPG.remote.rateLimiter.capacity",
        "beCPG.remote.rateLimiter.refillRate",
        "beCPG.remote.maxResults.limit",
		"beCPG.security.supplierPermission",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "system" }
			});
				
	[
        "beCPG.charact.name.format",
		"beCPG.project.name.format",
		"beCPG.comparison.name.format",
		"beCPG.product.name.format",
		"beCPG.quality.sampleId.format",
		"beCPG.sendToSupplier.entityName.format",
		"beCPG.sendToSupplier.projectName.format",
		"beCPG.report.name.format",
		"beCPG.connector.channel.register.activity",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "format"  }
			});
			
	[
		"beCPG.defaultSearchTemplate",
		"beCPG.product.searchTemplate",
		"beCPG.report.includeReportInSearch",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "search"  }
			});
			
	[
		"beCPG.multilinguale.shouldExtractMLText",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "export"  }
			});
				
	[
		"beCPG.report.image.maxSizeInBytes",
		"beCPG.report.datasource.maxSizeInBytes",
		"beCPG.product.report.componentDatalistsToExtract",
		"beCPG.product.report.assocsToExtract",
		"beCPG.product.report.assocsToExtractWithImage",
		"beCPG.product.report.assocsToExtractWithDataList",
		"beCPG.product.report.assocsToExtractInDataList",
		"beCPG.product.report.multilineProperties",
		"beCPG.product.report.priceBreaks",
		"beCPG.product.report.extractRawMaterial",
		"beCPG.product.report.multiLevel",
		"beCPG.product.report.nonEffectiveComponent",
		"beCPG.product.report.extraImagePaths",
		"beCPG.product.report.nutList.localesToExtract",
		"beCPG.entity.report.mltext.fields",
		"beCPG.entity.report.mltext.locales",
		"beCPG.product.report.showDeprecatedXml",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "report"  }
			});
				
	[
		"beCPG.formulation.reqCtrlList.maxRclSourcesToKeep",
		"beCPG.formulation.reqCtrlList.addChildRclSources",
		"beCPG.formulation.specification.addInfoReqCtrl",
		"beCPG.formulation.nutList.propagateUpEnable",
		"beCPG.formulation.ingsCalculatingWithYield",
		"beCPG.formulation.costList.keepProductUnit",
		"beCPG.formulation.score.nutriscore.regulatoryClass",
		"beCPG.formulation.security.enforceACL",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "formulation"  }
			});
				
	[
		"beCPG.eco.automatic.enable",
		"beCPG.eco.automatic.states",
		"beCPG.eco.automatic.apply",
		"beCPG.eco.automatic.revision.type",
		"beCPG.eco.automatic.record.version.type",
		"beCPG.eco.automatic.deleteOnApply",
		"beCPG.eco.automatic.withoutRecord",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "change-order"  }
			});
				
	[
		"project.subProject.propsToCopyFromParent",
		"project.subProject.propsToCopyToParent",
		"project.extractor.myProjectAttributes",
	].forEach(function(p) {
				model.sysBeCPGAttributes[p] = { "type": "java.lang.String", "readonly": false, "qname": p, "name": p, "value": bSys.confValue(p), "set": "project"  }
			});

	model.tools = Admin.getConsoleTools("system-configuration");
	model.metadata = Admin.getServerMetaData();
}

main();