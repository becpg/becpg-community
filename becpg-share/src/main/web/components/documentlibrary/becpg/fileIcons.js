
      
                  
beCPG.util.getFileIcon = function(p_fileName, p_record ,p_isContainer,p_isSimpleView)
{
   
   var  node = p_record.jsNode,
		 type = node.type;
		 
   var iconSize = p_isSimpleView ? 32 :64;
   
   if(p_isContainer && !(type == "{http://www.bcpg.fr/model/becpg/1.0}entityFolder" || type == "bcpg:entityFolder")){
   	return Alfresco.constants.URL_RESCONTEXT 
      + 'components/documentlibrary/images/folder-'+iconSize+'.png';
   }
   
   if(p_isSimpleView || type == "{http://www.bcpg.fr/model/becpg/1.0}entityFolder" || type == "bcpg:entityFolder"){
   	return Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/'+Alfresco.util.getFileIcon(p_fileName,type,iconSize);
   }

   return  Alfresco.DocumentList.generateThumbnailUrl(p_record);
   
};
                  
                  
/**
 * Override file icon types
 */
Alfresco.util.getFileIcon.types =
{
   "{http://www.alfresco.org/model/content/1.0}cmobject": "file",
   "cm:cmobject": "file",
   "{http://www.alfresco.org/model/content/1.0}content": "file",
   "cm:content": "file",
   "{http://www.alfresco.org/model/content/1.0}thumbnail": "file",
   "cm:thumbnail": "file",
   "{http://www.alfresco.org/model/content/1.0}folder": "folder",
   "cm:folder": "folder",
   "{http://www.alfresco.org/model/content/1.0}category": "category",
   "cm:category": "category",
   "{http://www.alfresco.org/model/content/1.0}person": "user",
   "cm:person": "user",
   "{http://www.alfresco.org/model/content/1.0}authorityContainer": "group",
   "cm:authorityContainer": "group",
   "tag": "tag",
   "{http://www.alfresco.org/model/site/1.0}sites": "site",
   "st:sites": "site",
   "{http://www.alfresco.org/model/site/1.0}site": "site",
   "st:site": "site",
   "{http://www.alfresco.org/model/transfer/1.0}transferGroup": "server-group",
   "trx:transferGroup": "server-group",
   "{http://www.alfresco.org/model/transfer/1.0}transferTarget": "server",
   "trx:transferTarget": "server",
   "{http://www.bcpg.fr/model/security/1.0}aclGroup" : "aclGroup",
   "sec:aclGroup":"acl",
   "{http://www.bcpg.fr/model/becpg/1.0}cost":"cost",
   "bcpg:cost":"cost",
   "{http://www.bcpg.fr/model/becpg/1.0}microbio":"microbio",
   "bcpg:microbio":"microbio",
   "{http://www.bcpg.fr/model/becpg/1.0}physicoChem":"physicoChem",
   "bcpg:physicoChem":"physicoChem",
   "{http://www.bcpg.fr/model/becpg/1.0}allergen":"allergen",
   "bcpg:allergen":"allergen",
  "{http://www.bcpg.fr/model/becpg/1.0}organo":"organo",
   "bcpg:organo":"organo",
   "{http://www.bcpg.fr/model/becpg/1.0}ing":"ing",
   "bcpg:ing":"ing",
   "{http://www.bcpg.fr/model/becpg/1.0}nut":"nut",
   "bcpg:nut":"nut",
   "{http://www.bcpg.fr/model/becpg/1.0}geoOrigin":"geoOrigin",
   "bcpg:geoOrigin":"geoOrigin",
   "{http://www.bcpg.fr/model/becpg/1.0}bioOrigin":"bioOrigin",
   "bcpg:bioOrigin":"bioOrigin",
   "{http://www.bcpg.fr/model/becpg/1.0}client":"client",
   "bcpg:client":"client",
   "{http://www.bcpg.fr/model/becpg/1.0}supplier":"supplier",
   "bcpg:supplier":"supplier",
   "{http://www.bcpg.fr/model/becpg/1.0}product":"product",
   "bcpg:product":"product",
   "{http://www.bcpg.fr/model/quality/1.0}controlPlan":"controlPlan",
   "qa:controlPlan":"controlPlan",
   "{http://www.bcpg.fr/model/quality/1.0}nc":"nc",
   "qa:nc":"nc",
   "{http://www.bcpg.fr/model/quality/1.0}controlPoint":"controlPoint",
   "qa:controlPoint":"controlPoint",
   "{http://www.bcpg.fr/model/quality/1.0}qualityControl":"qualityControl",
   "qa:qualityControl":"qualityControl",
   "{http://www.bcpg.fr/model/becpg/1.0}entityFolder":"entityFolder",
   "bcpg:entityFolder":"entityFolder"
		   
};
