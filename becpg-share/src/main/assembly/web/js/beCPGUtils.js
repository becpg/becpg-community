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
(function() {

    var lockCount = 0;


    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL, $html = Alfresco.util.encodeHTML, $links = Alfresco.util.activateLinks;

    beCPG.util.incLockCount = function() {
        return ++lockCount;
    };

    beCPG.util.lockCount = function() {
        return lockCount;
    };


    beCPG.util.entityURL = function(siteId, pNodeRef, type, context, list) {

        var nodeRef = new Alfresco.util.NodeRef(pNodeRef);

        var redirect = "entity-data-lists?nodeRef=" + nodeRef.toString();

        if (type == "document" || type == "folder") {
            redirect = type + "-details?nodeRef=" + nodeRef.toString();
        }

        if (list == "wizard") {
            redirect = "wizard?nodeRef=" + nodeRef.toString() + "&id=supplier-" + type.replace("bcpg:", "");
        }


        if (context && context != null) {
            redirect = "context/" + context + "/" + redirect;
        }

        redirect = $siteURL(redirect,
            {
                site: siteId
            });

        if (list != "wizard") {
            if (list && list != null) {
                redirect += "&list=" + list;
            } else {

                if (type == "bcpg:finishedProduct" || type == "bcpg:semiFinishedProduct") {
                    redirect += "&list=compoList";
                }
                else if (type == "bcpg:packagingKit") {
                    redirect += "&list=packagingList";
                }
                else if (type == "pjt:project") {
                    redirect += "&list=taskList";
                } else if (!(type == "document" || type == "folder")) {
                    redirect += "&list=View-properties";
                }
            }
        }


        return redirect;

    };



    beCPG.util.entityDocumentsURL = function(siteId, path, name, isFullPath) {
        var url = null;
        if (path != null) {
            if (Alfresco.constants.PAGECONTEXT == "mine") {
                url = "/myfiles";
            }
            else if (Alfresco.constants.PAGECONTEXT == "shared") {
                url = "/sharedfiles";
            }
            else {
                url = Alfresco.util.isValueSet(siteId) ? "/documentlibrary" : "/repository";
            }

            if (isFullPath && Alfresco.constants.PAGECONTEXT != "mine") {
                url += '?path=' + encodeURIComponent(path + '/' + name);
            }
            else {
                if (url.indexOf("repository") > 0 || url.indexOf("sharedfiles") > 0) {
                    url += '?path=' + encodeURIComponent('/' + path.split('/').slice(2).join('/') + '/' + name);
                } else if (url.indexOf("myfiles") > 0) {
                    url += '?path=' + encodeURIComponent('/' + path.split('/').slice(4).join('/') + '/' + name);
                }
                else {
                    url += '?path=' + encodeURIComponent('/' + path + '/' + name);
                }

            }

            if (url !== null) {
                url = $siteURL(url,
                    {
                        site: siteId
                    });
            }
        }
        return url;

    };

    beCPG.util.sigFigs = function sigFigs(n, sig) {
        if (n && n != 0) {
            var fact = 1;
            if (n < 0) {
                n = Math.abs(n);
                fact = -1;
            }

            if (n >= Math.pow(10, sig)) {
                return fact * Math.round(n);
            } else {
                var mult = Math.pow(10,
                    sig - Math.floor(Math.log(n) / Math.LN10) - 1);
                return fact * (Math.round(n * mult) / mult);
            }
        }
        else {
            return n;
        }
    };

    beCPG.util.getJSLocale = function getJSLocale() {
        return Alfresco.constants.JS_LOCALE.replace("_", "-");
    };

    beCPG.util.exp = function exp(val) {
        if (val == 0) {
            return "0";
        } else if (Math.abs(val) < 0.000001) {
            return beCPG.util.sigFigs(val * 1000000, 3).toLocaleString(beCPG.util.getJSLocale()) + "×10<sup>-6</sup>";
        } else if (Math.abs(val) < 0.01) {
            return beCPG.util.sigFigs(val * 1000, 3).toLocaleString(beCPG.util.getJSLocale()) + "×10<sup>-3</sup>";
        } else if (Math.abs(val) >= 1000000) {
            return beCPG.util.sigFigs(val / 1000000, 3).toLocaleString(beCPG.util.getJSLocale()) + "×10<sup>6</sup>";
        }
        return beCPG.util.sigFigs(val, 3).toLocaleString(beCPG.util.getJSLocale());
    };


    beCPG.util.formatNumber = function formatNumber(format, value) {
        if (isNaN(value)) {
            return value != null ? value : "";
        } else {
            return '<span title=' + value + '>' + (new Intl.NumberFormat(beCPG.util.getJSLocale(), format).format(value)) + '</span>';
        }
    };


    beCPG.util.convertUnit = function convertUnit(val, fromUnit, toUnit) {
        // by Default toUnit is kg or m or L or perc
        if (val != null && val != "" && val != 0) {
            switch (fromUnit) {
                case "mo":
                    val = val * 30;
                    break;
                case "y":
                    val = val * 365;
                    break;
                case "ppm":
                    val = val / 10000;
                    break;
                case "pp":
                    val = val / 10;
                    break;
                case "g":
                case "milli":
                case "mL":
                case "mm":
                    val = val / 1000;
                    break;
                case "micro_m":
                    val = val / 1000000;
                    break;
                case "cL":
                case "cm":
                    val = val / 100;
                    break;
                case "gal":
                    val = val / 0.264172;
                    break;
                case "fl_oz":
                    val = val / 33.814;
                    break;
                case "cp":
                    val = val / 4.16667;
                    break;
                case "mg":
                case "micro":
                    val = val / 1000000;
                    break;
                case "micro_g":
                    val = val / 1000000000;
                    break;
                case "mega":
                    val = val * 1000000;
                    break;
                case "oz":
                    val = val / 35.27396195;
                    break;
                case "lb":
                    val = val / 2.204622622;
                    break;
                case "ft":
                    val = val / 3.28084;
                    break;
                case "in":
                    val = val / 39.37008;
                    break;
                case "mil":
                    val = val / 39370.079;
                    break;
                default:
                    break;
            }
            if (toUnit == "mm" || toUnit == "g" || toUnit == "mL") {
                val = val * 1000;
            }
        }
        return val;
    };



    beCPG.util.createTextTooltip = function(msg, size) {
        var text = msg;
        if (text != null && (text.length > size || text.split(/\r\n|\r|\n/).length > (size / 25))) {
            var length = 0;
            for (var i = 0; i < text.length; ++i) {
                if (text[i] == '\n') {
                    length = length + 25;
                } else if (text[i] == '\r') {
                    length = length + 4;
                }
                length++;

                if (length > size) {
                    text = text.substring(0, i - 1).trim() + "...";
                    break;
                }
            }
            return '<div class="dt-tooltip">' + $links($html(text)) + '<span class="dt-tooltip-content">' + $links($html(msg)) + '</span></div>';
        }
        return $links($html(msg));
    }


    beCPG.util.encodeAttr = function(text, justified) {
        if (text === null || typeof text == "undefined") {
            return "";
        }

        var indent = justified === true ? "" : "&nbsp;&nbsp;&nbsp;";

        if (YAHOO.env.ua.ie > 0) {
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


    beCPG.util.isEntity = function(record) {
        if (record && record.jsNode && beCPG.util.contains(record.jsNode.aspects, "bcpg:entityListsAspect")) {
            return true;
        }

        if (record && record.aspects !== null && beCPG.util.contains(record.aspects, "bcpg:entityListsAspect")) {
            return true;
        }
        return false;

    };

    beCPG.util.postActivity = function(siteId, activityType, title, page, data, callback) {
        // Mandatory parameter check
        if (!YAHOO.lang.isString(siteId) || siteId.length === 0 || !YAHOO.lang.isString(activityType) || activityType.length === 0 || !YAHOO.lang
            .isString(title) || title.length === 0 || !YAHOO.lang.isObject(data) === null || !(YAHOO.lang
                .isString(data.nodeRef) || YAHOO.lang.isString(data.parentNodeRef))) {
            return;
        }

        var config =
        {
            method: "POST",
            url: Alfresco.constants.PROXY_URI + "slingshot/activity/create",
            successCallback:
            {
                fn: callback,
                scope: this
            },
            failureCallback:
            {
                fn: callback,
                scope: this
            },
            dataObj: YAHOO.lang.merge(
                {
                    site: siteId,
                    type: activityType,
                    title: title,
                    page: page
                }, data)
        };

        Alfresco.util.Ajax.jsonRequest(config);

    };


    beCPG.util.updateMultiCheckboxesValue = function(checkboxesName, hiddenField, signalChange) {
        var listElement = document.getElementsByName(checkboxesName);

        if (listElement !== null) {
            var values = new Array();
            for (var k = 0; k < listElement.length; k++) {
                if (listElement[k].checked) {
                    values.push(listElement[k].value);
                }
            }

            YUIDom.get(hiddenField).value = values.join(",");

            if (signalChange) {
                YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
            }
        }
    };

    beCPG.util.getRegulatoryCountryKey = function(loc) {
        var language = loc.split("_")[0];
        var country = loc.split("_").length > 1 ? loc.split("_")[1] : language.toUpperCase();
        if (country === "US" || country === "CA" || country === "AU" || country === "ID" || country === "HK" || country === "MY"
            || country === "IL" || country === "IN" || country === "KR"
            || country === "MA" || country === "MX" || country === "DZ"
            || country === "TR" || country === "SG" || country === "TH"
            || country === "PK" || country === "ZA" || country === "TN"
            || country === "EG" || country === "CL" || country === "UY"
            || country === "BR" || country === "TT" || country === "DO"
            || country === "PE") {
            return country;
        } else if (language === "zh") {
            return "CN";
        } else if (language === "ru") {
            return "RU";
        } else if (country === "NZ") {
            return "AU";
        } else if (country === "PR") {
            return "US";
        } else if (country === "PY") {
            return "BR";
        } else if (country === "GT" || country === "PA" || country === "SV") {
            return "CTA";
        } else if (country === "AE" || country === "BH" || country === "SA"
            || country === "QA" || country === "OM" || country === "KW") {
            return "GSO";
        } else if (country === "KE" || country === "NG" || country === "GH"
            || country === "CI" || country === "UG" || country === "MZ"
            || country === "MW" || country === "TZ" || country === "ZM"
            || country === "ZW" || country === "KH" || country === "MM"
            || country === "JO" || country === "IQ" || country === "PS") {
            return "CODEX";
        } else if (country === "CO") {
            return "CO";
        }

        return "EU";
    };

    beCPG.util.renderHttpLink = function(fieldValue) {
        if (fieldValue) {
            var regex = /"([^"]+)":(https?:\/\/[^\s]+)|(?:https?:\/\/[^\s]+)/g;
            var match = regex.exec(fieldValue);
            if (match) {
                var anchorText = "";
                var href = "";

                if (match[1] && match[2]) {
                    anchorText = match[1];
                    href = match[2];
                } else {
                    anchorText = match[0];
                    href = match[0];
                }
                var a = document.createElement('a');
                a.textContent = anchorText;
                a.href = href;
                a.target = "_blank";
                a.rel = "noopener noreferrer";
                return a.outerHTML;
            }
        }
        return fieldValue;
    };

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
        "{http://www.bcpg.fr/model/security/1.0}aclGroup": "aclGroup",
        "sec:aclGroup": "aclGroup",
        "{http://www.bcpg.fr/model/becpg/1.0}cost": "cost",
        "bcpg:cost": "cost",
        "{http://www.bcpg.fr/model/becpg/1.0}microbio": "microbio",
        "bcpg:microbio": "microbio",
        "{http://www.bcpg.fr/model/becpg/1.0}physicoChem": "physicoChem",
        "bcpg:physicoChem": "physicoChem",
        "{http://www.bcpg.fr/model/becpg/1.0}allergen": "allergen",
        "bcpg:allergen": "allergen",
        "{http://www.bcpg.fr/model/becpg/1.0}organo": "organo",
        "bcpg:organo": "organo",
        "{http://www.bcpg.fr/model/becpg/1.0}ing": "ing",
        "bcpg:ing": "ing",
        "{http://www.bcpg.fr/model/becpg/1.0}nut": "nut",
        "bcpg:nut": "nut",
        "{http://www.bcpg.fr/model/becpg/1.0}geoOrigin": "geoOrigin",
        "bcpg:geoOrigin": "geoOrigin",
        "{http://www.bcpg.fr/model/becpg/1.0}bioOrigin": "bioOrigin",
        "bcpg:bioOrigin": "bioOrigin",
        "{http://www.bcpg.fr/model/becpg/1.0}client": "client",
        "bcpg:client": "client",
        "{http://www.bcpg.fr/model/becpg/1.0}supplier": "supplier",
        "bcpg:supplier": "supplier",
        "{http://www.bcpg.fr/model/becpg/1.0}product": "product",
        "bcpg:product": "product",
        "{http://www.bcpg.fr/model/quality/1.0}controlPlan": "controlPlan",
        "qa:controlPlan": "controlPlan",
        "{http://www.bcpg.fr/model/quality/1.0}nc": "nc",
        "qa:nc": "nc",
        "{http://www.bcpg.fr/model/quality/1.0}controlPoint": "controlPoint",
        "qa:controlPoint": "controlPoint",
        "{http://www.bcpg.fr/model/quality/1.0}qualityControl": "qualityControl",
        "qa:qualityControl": "qualityControl",
        "{http://www.bcpg.fr/model/quality/1.0}workItemAnalysis": "workItemAnalysis",
        "qa:workItemAnalysis": "workItemAnalysis",
        "{http://www.bcpg.fr/model/becpg/1.0}systemEntity": "systemEntity",
        "bcpg:systemEntity": "systemEntity",
        "{http://www.bcpg.fr/model/becpg/1.0}finishedProduct": "finishedProduct",
        "bcpg:finishedProduct": "finishedProduct",
        "{http://www.bcpg.fr/model/becpg/1.0}semiFinishedProduct": "semiFinishedProduct",
        "bcpg:semiFinishedProduct": "semiFinishedProduct",
        "{http://www.bcpg.fr/model/becpg/1.0}rawMaterial": "rawMaterial",
        "bcpg:rawMaterial": "rawMaterial",
        "{http://www.bcpg.fr/model/becpg/1.0}localSemiFinishedProduct": "localSemiFinishedProduct",
        "bcpg:localSemiFinishedProduct": "localSemiFinishedProduct",
        "{http://www.bcpg.fr/model/becpg/1.0}packagingKit": "packagingKit",
        "bcpg:packagingKit": "packagingKit",
        "{http://www.bcpg.fr/model/becpg/1.0}packagingMaterial": "packagingMaterial",
        "bcpg:packagingMaterial": "packagingMaterial",
        "{http://www.bcpg.fr/model/becpg/1.0}resourceProduct": "resourceProduct",
        "bcpg:resourceProduct": "resourceProduct",
        "{http://www.bcpg.fr/model/ecm/1.0}changeOrder": "changeOrder",
        "ecm:changeOrder": "changeOrder",
        "{http://www.bcpg.fr/model/publication/1.0}productCatalog": "productCatalog",
        "bp:productCatalog": "productCatalog",
        "{http://www.bcpg.fr/model/becpg/1.0}productSpecification": "productSpecification",
        "bcpg:productSpecification": "productSpecification",
        "{http://www.bcpg.fr/model/becpg/1.0}productCollection": "productCollection",
        "bcpg:productCollection": "productCollection",
        "{http://www.bcpg.fr/model/quality/1.0}batch": "batch",
        "qa:batch": "batch",
        "{http://www.bcpg.fr/model/becpg/1.0}productMicrobioCriteria": "productMicrobioCriteria",
        "bcpg:productMicrobioCriteria": "productMicrobioCriteria",
        "{http://www.bcpg.fr/model/project/1.0}project": "project",
        "pjt:project": "project",
        "rep:reportTpl": "rptdesign",
        "{http://www.bcpg.fr/model/report/1.0}reportTpl": "rptdesign"
    };

})();

Alfresco.util.message = function(p_messageId, p_messageScope)
{
   var msg = p_messageId;

   if (typeof p_messageId != "string")
   {
      throw new Error("Missing or invalid argument: messageId");
   }

   var globalMsg = Alfresco.messages.global[p_messageId];
   if (typeof globalMsg == "string")
   {
      msg = globalMsg;
   }

   if ((typeof p_messageScope == "string") && (typeof Alfresco.messages.scope[p_messageScope] == "object"))
   {
      var scopeMsg = Alfresco.messages.scope[p_messageScope][p_messageId];
      if (typeof scopeMsg == "string")
      {
         msg = scopeMsg;
      }
   }
   
   var globalMsgForce = Alfresco.messages.global[p_messageId + ".force"];
   if (typeof globalMsgForce == "string")
   {
      msg = globalMsgForce;
   }

   // Search/replace tokens
   var tokens = [];
   if ((arguments.length == 3) && (typeof arguments[2] == "object"))
   {
      tokens = arguments[2];
   }
   else
   {
      tokens = Array.prototype.slice.call(arguments).slice(2);
   }

   // Emulate server-side I18NUtils implementation
   if (YAHOO.lang.isArray(tokens) && tokens.length > 0)
   {
      msg = msg.replace(/''/g, "'");
   }
   msg = YAHOO.lang.substitute(msg, tokens);

   return msg;
};
