/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
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
 * 
 * @namespace  beCPG.util
 */
(function()
{

	var lockCount = 0;
	

    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL, $html = Alfresco.util.encodeHTML, $links = Alfresco.util.activateLinks;

    beCPG.util.incLockCount = function (){
    	return ++lockCount;
    };
    
    beCPG.util.lockCount = function (){
    	return lockCount;
    };
    
    
    beCPG.util.entityURL = function(siteId, pNodeRef, type, context, list)
    {

        var nodeRef = new Alfresco.util.NodeRef(pNodeRef);

        var redirect = "entity-data-lists?nodeRef=" + nodeRef.toString();
        
        if (type == "document" || type == "folder")
        {
            redirect = type+"-details?nodeRef=" + nodeRef.toString();
        }

        if (context && context != null)
        {
            redirect = "context/" + context + "/" + redirect;
        }

        redirect = $siteURL(redirect,
        {
            site : siteId
        });

        if(list && list !=null){
            redirect += "&list="+list;
        } else {
        
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
            } else if(!( type == "document" || type == "folder")){
                
                redirect +="&list=View-properties";
            }
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

        if (isFullPath && Alfresco.constants.PAGECONTEXT != "mine")
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
    
    beCPG.util.sigFigs = function sigFigs(n, sig){
        if(n && n != 0){
         var fact = 1;
         if(n<0){
        	 n = Math.abs(n);
        	 fact = -1;
         }
        	
      	  if(n >= Math.pow(10,sig)){
      		  return fact * Math.round(n);
      	  } else{
      		  var mult = Math.pow(10,
                    sig - Math.floor(Math.log(n) / Math.LN10) - 1);
      		  return fact * (Math.round(n * mult) / mult);
      	  }            
        }
        else{
            return n;   
        }       
    };
    
    beCPG.util.formatNumber = function formatNumber(format, value){
    	return '<span title='+value+'>'+(new Intl.NumberFormat(Alfresco.constants.JS_LOCALE.replace("_","-"), format).format(value))+'</span>';	
    	
    };
    

    beCPG.util.convertUnit = function convertUnit(val, fromUnit, toUnit){
    	// by Default toUnit is kg or m or L or perc
    	if(val!=null && val!="" && val!=0){
    		switch (fromUnit) {
    		case "mo":
    			val=  val * 30;
    			break;
    		case "ppm":
    			val=   val / 10000;
    			break;
    		case "pp":
    			val=   val / 10;
    			break;
    		case "g":
    		case "milli":
    		case "mL":
    		case "mm":
    			val=    val / 1000;	
    			break;
    		case "cL":	
    		case "cm":
    			val=   val / 100; 
    			break;
    		case "gal":	
    			val=   val / 0.264172; 
    			break;
    		case "fl_oz":	
    			val=   val / 33.814; 	
    			break;
    		case "cp":	
    			val=   val / 4.16667; 	
    			break;
    		case "mg":	
    		case "micro":		
    			val=   val / 1000000; 
    			break;
    		case "mega":		
    			val=   val * 1000000; 
    			break;
    		case "oz":		
    			val=   val / 35.27396195; 	
    			break;
    		case "lb":
    			val=   val / 2.204622622;
    			break;
    		case "ft":
    			val=   val / 3.28084;
    			break;
    		case "in":
    			val=   val / 39.37008;
    			break;
    		case "mil":
    			val=   val / 39370.079;
    			break;
    		default:
    			break;
    		}
    		if(toUnit =="mm" || toUnit =="g" || toUnit =="mL"){
    			val = val *1000;
    		}
    	}
    	return val;
    };

    
	
    beCPG.util.createTextTooltip = function (msg,size){
		 var text = msg;
    	  if(text!=null && (text.length > size || text.split(/\r\n|\r|\n/).length > (size/25))){
    		  var length = 0;
    		  for(var i = 0; i < text.length; ++i){
    		      if(text[i] == '\n') {
    		    	  length = length+25;
    		      } else if(text[i] == '\r') {
    		    	  length = length+4;
    		      }
    		      length++;
    		      
    		      if(length>size){
    		    	  text = text.substring(0,i-1).trim()+"...";
    		    	  break;
    		      }
    		  }
    		  return '<div class="dt-tooltip">'+$links($html(text))+'<span class="dt-tooltip-content">'+$links($html(msg))+'</span></div>';
    	  }
    	 return $links($html(msg));
	}
	
    
    beCPG.util.encodeAttr = function(text, justified)
    {
       if (text === null || typeof text == "undefined")
       {
          return "";
       }

       var indent = justified === true ? "" : "&nbsp;&nbsp;&nbsp;";

       if (YAHOO.env.ua.ie > 0)
       {
          text = "" + text;
          return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "&#10;" + indent).replace(/"/g, "&quot;");
       }
       var me = arguments.callee;
       me.text.data = text;
       return me.div.innerHTML.replace(/\n/g, "&#10;" + indent).replace(/"/g, "&quot;");
    };
    beCPG.util.encodeAttr.div = document.createElement("div");
    beCPG.util.encodeAttr.text = document.createTextNode("");
    beCPG.util.encodeAttr.div.appendChild(beCPG.util.encodeAttr.text);
    
   
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
    
    
    beCPG.util.updateMultiCheckboxesValue = function(checkboxesName, hiddenField, signalChange)
    {
       var listElement = document.getElementsByName(checkboxesName);
       
       if (listElement !== null)
       {
    	   var values = new Array();
      	   for(var k = 0; k < listElement.length; k++)  
      	   {  
      	        if(listElement[k].checked) {
      	          values.push(listElement[k].value);
      	        } 
      	    }   
          
          YUIDom.get(hiddenField).value = values.join(",");
          
          if (signalChange)
          {
             YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
          }
       }
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
