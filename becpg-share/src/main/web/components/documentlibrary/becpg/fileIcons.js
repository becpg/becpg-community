if (!beCPG.util.getFileIcon) {

	/**
	 * Override file icon types
	 */
	Alfresco.util.getFileIcon.types = {
	   "{http://www.alfresco.org/model/content/1.0}cmobject" : "file",
	   "cm:cmobject" : "file",
	   "{http://www.alfresco.org/model/content/1.0}content" : "file",
	   "cm:content" : "file",
	   "{http://www.alfresco.org/model/content/1.0}thumbnail" : "file",
	   "cm:thumbnail" : "file",
	   "{http://www.alfresco.org/model/content/1.0}folder" : "folder",
	   "cm:folder" : "folder",
	   "{http://www.alfresco.org/model/content/1.0}category" : "category",
	   "cm:category" : "category",
	   "{http://www.alfresco.org/model/content/1.0}person" : "user",
	   "cm:person" : "user",
	   "{http://www.alfresco.org/model/content/1.0}authorityContainer" : "group",
	   "cm:authorityContainer" : "group",
	   "tag" : "tag",
	   "{http://www.alfresco.org/model/site/1.0}sites" : "site",
	   "st:sites" : "site",
	   "{http://www.alfresco.org/model/site/1.0}site" : "site",
	   "st:site" : "site",
	   "{http://www.alfresco.org/model/transfer/1.0}transferGroup" : "server-group",
	   "trx:transferGroup" : "server-group",
	   "{http://www.alfresco.org/model/transfer/1.0}transferTarget" : "server",
	   "trx:transferTarget" : "server",
	   "{http://www.bcpg.fr/model/security/1.0}aclGroup" : "aclGroup",
	   "sec:aclGroup" : "acl",
	   "{http://www.bcpg.fr/model/becpg/1.0}cost" : "cost",
	   "bcpg:cost" : "cost",
	   "{http://www.bcpg.fr/model/becpg/1.0}microbio" : "microbio",
	   "bcpg:microbio" : "microbio",
	   "{http://www.bcpg.fr/model/becpg/1.0}physicoChem" : "physicoChem",
	   "bcpg:physicoChem" : "physicoChem",
	   "{http://www.bcpg.fr/model/becpg/1.0}allergen" : "allergen",
	   "bcpg:allergen" : "allergen",
	   "{http://www.bcpg.fr/model/becpg/1.0}organo" : "organo",
	   "bcpg:organo" : "organo",
	   "{http://www.bcpg.fr/model/becpg/1.0}ing" : "ing",
	   "bcpg:ing" : "ing",
	   "{http://www.bcpg.fr/model/becpg/1.0}nut" : "nut",
	   "bcpg:nut" : "nut",
	   "{http://www.bcpg.fr/model/becpg/1.0}geoOrigin" : "geoOrigin",
	   "bcpg:geoOrigin" : "geoOrigin",
	   "{http://www.bcpg.fr/model/becpg/1.0}bioOrigin" : "bioOrigin",
	   "bcpg:bioOrigin" : "bioOrigin",
	   "{http://www.bcpg.fr/model/becpg/1.0}client" : "client",
	   "bcpg:client" : "client",
	   "{http://www.bcpg.fr/model/becpg/1.0}supplier" : "supplier",
	   "bcpg:supplier" : "supplier",
	   "{http://www.bcpg.fr/model/becpg/1.0}product" : "product",
	   "bcpg:product" : "product",
	   "{http://www.bcpg.fr/model/quality/1.0}controlPlan" : "controlPlan",
	   "qa:controlPlan" : "controlPlan",
	   "{http://www.bcpg.fr/model/quality/1.0}nc" : "nc",
	   "qa:nc" : "nc",
	   "{http://www.bcpg.fr/model/quality/1.0}controlPoint" : "controlPoint",
	   "qa:controlPoint" : "controlPoint",
	   "{http://www.bcpg.fr/model/quality/1.0}qualityControl" : "qualityControl",
	   "qa:qualityControl" : "qualityControl",
	   "{http://www.bcpg.fr/model/quality/1.0}workItemAnalysis" : "workItemAnalysis",
	   "qa:workItemAnalysis" : "workItemAnalysis",	   
	   "{http://www.bcpg.fr/model/becpg/1.0}systemEntity" : "systemEntity",
	   "bcpg:systemEntity" : "systemEntity",
	   "{http://www.bcpg.fr/model/becpg/1.0}finishedProduct" : "finishedProduct",
	   "bcpg:finishedProduct" : "finishedProduct",
	   "{http://www.bcpg.fr/model/becpg/1.0}semiFinishedProduct" : "semiFinishedProduct",
	   "bcpg:semiFinishedProduct" : "semiFinishedProduct",
	   "{http://www.bcpg.fr/model/becpg/1.0}rawMaterial" : "rawMaterial",
	   "bcpg:rawMaterial" : "rawMaterial",
	   "{http://www.bcpg.fr/model/becpg/1.0}localSemiFinishedProduct" : "localSemiFinishedProduct",
	   "bcpg:localSemiFinishedProduct" : "localSemiFinishedProduct",
	   "{http://www.bcpg.fr/model/becpg/1.0}packagingKit" : "packagingKit",
	   "bcpg:packagingKit" : "packagingKit",
	   "{http://www.bcpg.fr/model/becpg/1.0}packagingMaterial" : "packagingMaterial",
	   "bcpg:packagingMaterial" : "packagingMaterial",
	   "{http://www.bcpg.fr/model/becpg/1.0}resourceProduct" : "resourceProduct",
	   "bcpg:resourceProduct" : "resourceProduct",
	   "{http://www.bcpg.fr/model/ecm/1.0}changeOrder" : "changeOrder",
	   "ecm:changeOrder" : "changeOrder",
	   "{http://www.bcpg.fr/model/publication/1.0}productCatalog" : "productCatalog",
	   "bp:productCatalog" : "productCatalog",
	   "{http://www.bcpg.fr/model/project/1.0}project" : "project",
	   "pjt:project" : "project",
	   "{http://www.bcpg.fr/model/becpg/1.0}productSpecification" : "productSpecification",
	   "bcpg:productSpecification" : "productSpecification",
	   "{http://www.bcpg.fr/model/becpg/1.0}productMicrobioCriteria" : "productMicrobioCriteria",
	   "bcpg:productMicrobioCriteria" : "productMicrobioCriteria"
	};

	Alfresco.util.getFileIcon.folders = {
			"{http://www.alfresco.org/model/content/1.0}folder" : "folder",
		   "cm:folder" : "folder",
		   "{http://www.alfresco.org/model/site/1.0}sites" : "site",
		   "st:sites" : "site",
		   "{http://www.alfresco.org/model/site/1.0}site" : "site",
		   "st:site" : "site"
	};
	
	
	beCPG.util.getFileIcon = function(p_fileName, p_record, p_isContainer, p_isSimpleView) {

		var node = p_record.jsNode, type = node.type;

		var iconSize = p_isSimpleView ? 32 : 48;

		if (p_isContainer  && (!Alfresco.util.getFileIcon.types[type] 
				|| Alfresco.util.getFileIcon.folders[type]) ) {
			return Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/images/folder-' + iconSize + '.png';
		}

		if (p_isSimpleView) {
			return Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/'
			      + Alfresco.util.getFileIcon(p_fileName, type, iconSize);
		}

		return Alfresco.DocumentList.generateThumbnailUrl(p_record);

	};

}
