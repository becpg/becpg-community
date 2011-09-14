/**
 * beCPG root namespace.
 * 
 * @namespace beCPG
 */
// Ensure beCPG root object exists
if (typeof beCPG == "undefined" || !beCPG)
{
   var beCPG = {};
}

/**
 * beCPG top-level component namespace.
 * 
 * @namespace beCPG
 * @class beCPG.component
 */
beCPG.component = beCPG.component || {};

/**
 * beCPG top-level module namespace.
 * 
 * @namespace beCPG
 * @class beCPG.module
 */
beCPG.module = beCPG.module || {};

/**
 * beCPG top-level widget namespace.
 * 
 * @namespace beCPG
 * @class beCPG.widget
 */
beCPG.widget = beCPG.widget || {};

/**
 * beCPG top-level util namespace.
 * 
 * @namespace beCPG
 * @class beCPG.util
 */
beCPG.util = beCPG.util || {};


/**
 * Given a filename, returns either a filetype icon or generic icon file stem
 *
 * @method Alfresco.util.getFileIcon
 * @param p_fileName {string} File to find icon for
 * @param p_fileType {string} Optional: Filetype to offer further hinting
 * @param p_iconSize {int} Icon size: 32
 * @return {string} The icon name, e.g. doc-file-32.png
 * @static
 */
beCPG.util.getFileIcon = function(p_fileName, p_fileType, p_iconSize)
{
   // Mapping from extn to icon name for cm:content
   var extns = 
   {
      "doc": "doc",
      "docx": "doc",
      "ppt": "ppt",
      "pptx": "ppt",
      "xls": "xls",
      "xlsx": "xls",
      "pdf": "pdf",
      "bmp": "img",
      "gif": "img",
      "jpg": "img",
      "jpeg": "img",
      "png": "img",
      "txt": "text"
   };

   
  
   var prefix = "generic",
      fileType = typeof p_fileType === "string" ? p_fileType : "cm:content",
      iconSize = typeof p_iconSize === "number" ? p_iconSize : 32;
   
   // If type = cm:content, then use extn look-up
   var type = beCPG.util.getFileIcon.types[fileType];
   if (type === "file")
   {
      var extn = p_fileName.substring(p_fileName.lastIndexOf(".") + 1).toLowerCase();
      if (extn in extns)
      {
         prefix = extns[extn];
      }
   }
   else if (typeof type == "undefined")
   {
      type = "file";
   }
   return prefix + "-" + type + "-" + iconSize + ".png";
};
beCPG.util.getFileIcon.types =
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
   "{http://www.bcpg.fr/model/security/1.0}aclGroup" : "acl",
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
   "bcpg:bioOrigin":"bioOrigin"

};
