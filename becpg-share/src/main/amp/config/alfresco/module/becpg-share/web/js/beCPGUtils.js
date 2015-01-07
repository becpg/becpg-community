/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * Asset location helper class.
 * 
 * @namespace Alfresco
 * @class Alfresco.Location
 */
(function()
{

    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL;

    beCPG.util.entityCharactURL = function(siteId, pNodeRef, type, context)
    {

        var nodeRef = new Alfresco.util.NodeRef(pNodeRef);

        var redirect = "entity-data-lists?nodeRef=" + nodeRef.toString();

        if (context && context != null)
        {
            redirect = "context/" + context + "/" + redirect;
        }

        redirect = $siteURL(redirect,
        {
            site : siteId
        });

        if (type == "bcpg:finishedProduct" || type == "bcpg:semiFinishedProduct")
        {
            redirect += "&list=compoList";
        }
        else if (type == "bcpg:packagingKit")
        {
            redirect += "&list=packagingList";
        }
        else if (type == "pjt:project")
        {
            redirect += "&list=taskList";
        }

        return redirect;

    };

    beCPG.util.entityDocumentsURL = function(siteId, path, name, isFullPath)
    {
        var url = null;

        if (Alfresco.constants.PAGECONTEXT == "mine")
        {
            url = "/myfiles";
        }
        else if (Alfresco.constants.PAGECONTEXT == "shared")
        {
            url = "/sharedfiles";
        }
        else
        {
            url = Alfresco.util.isValueSet(siteId) ? "/documentlibrary" : "/repository";
        }

        if (isFullPath)
        {
            url += '?path=' + encodeURIComponent(path + '/' + name);
        }
        else
        {
            if (url.indexOf("repository") > 0  || url.indexOf("sharedfiles")>0)
            {
                url += '?path=' + encodeURIComponent('/' + path.split('/').slice(2).join('/') + '/' + name);
            } else if( url.indexOf("myfiles")>0) {
                url += '?path=' + encodeURIComponent('/' + path.split('/').slice(4).join('/') + '/' + name);
            }
            else
            {
                url += '?path=' + encodeURIComponent('/' + path + '/' + name);
            }

        }

        if (url !== null)
        {
            url = $siteURL(url,
            {
                site : siteId
            });
        }
        return url;

    };

    beCPG.util.entityDetailsURL = function(siteId, pNodeRef, type, context)
    {
        var nodeRef = new Alfresco.util.NodeRef(pNodeRef);

        var containerType = "entity";
        if (type == "document" || type == "folder")
        {
            containerType = type;
        }

        var redirect = containerType + "-details?nodeRef=" + nodeRef.toString();

        if (context && context != null)
        {
            redirect = "context/" + context + "/" + redirect;
        }

        return $siteURL(redirect,
        {
            site : siteId
        });

    };

    beCPG.util.isEntity = function(record)
    {
        if (record && record.jsNode && beCPG.util.contains(record.jsNode.aspects, "bcpg:entityListsAspect"))
        {
            return true;
        }

        if (record && record.aspects !== null && beCPG.util.contains(record.aspects, "bcpg:entityListsAspect"))
        {
            return true;
        }
        return false;

    };

    beCPG.util.postActivity = function(siteId, activityType, title, page, data, callback)
    {
        // Mandatory parameter check
        if (!YAHOO.lang.isString(siteId) || siteId.length === 0 || !YAHOO.lang.isString(activityType) || activityType.length === 0 || !YAHOO.lang
                .isString(title) || title.length === 0 || !YAHOO.lang.isObject(data) === null || !(YAHOO.lang
                .isString(data.nodeRef) || YAHOO.lang.isString(data.parentNodeRef)))
        {
            return;
        }

        var config =
        {
            method : "POST",
            url : Alfresco.constants.PROXY_URI + "slingshot/activity/create",
            successCallback :
            {
                fn : callback,
                scope : this
            },
            failureCallback :
            {
                fn : callback,
                scope : this
            },
            dataObj : YAHOO.lang.merge(
            {
                site : siteId,
                type : activityType,
                title : title,
                page : page
            }, data)
        };

        Alfresco.util.Ajax.jsonRequest(config);

    };

    Alfresco.util.getFileIcon.types =
    {
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
        "sec:aclGroup" : "aclGroup",
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
        "{http://www.bcpg.fr/model/becpg/1.0}productSpecification" : "productSpecification",
        "bcpg:productSpecification" : "productSpecification",
        "{http://www.bcpg.fr/model/becpg/1.0}productMicrobioCriteria" : "productMicrobioCriteria",
        "bcpg:productMicrobioCriteria" : "productMicrobioCriteria",
        "{http://www.bcpg.fr/model/project/1.0}project" : "project",
        "pjt:project" : "project",
        "rep:reportTpl" : "rptdesign",
        "{http://www.bcpg.fr/model/report/1.0}reportTpl" : "rptdesign"
    };

})();
