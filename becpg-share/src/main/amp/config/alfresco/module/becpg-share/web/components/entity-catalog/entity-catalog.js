/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {

	/**
	 * EntityCatalog constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.EntityCatalog} The new EntityCatalog
	 *         instance
	 * @constructor
	 */
	beCPG.component.EntityCatalog = function(htmlId) {
		beCPG.component.EntityCatalog.superclass.constructor.call(this, "beCPG.component.EntityCatalog", htmlId, [ "button", "container" ]);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.EntityCatalog, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.EntityCatalog.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
			/**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
			entityNodeRef : ""
		},

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function EntityCatalog_onReady() {
			var instance=this;
			// Appel AJax entity-catalog 
			// /becpg/entity/catalog/node/{store_type}/{store_id}/{id}
			// ne rien affiché si pas de catalogues

			function parseJsonToHTML(json){
				//displays things if there are any catalogs
				if(json !== undefined && json != null && Object.keys(json).length > 0){
					
					var html="<div class=\"entity-catalog\">";				
					for(var key in json){
						var score = json[key].score;
						var locales = json[key].locales;
						var label = json[key].label;
						var catalogId = json[key].id;
						
						if(locales !== undefined && locales != null && locales.length > 0){
							catalogId = catalogId+"_"+locales[0];
						}
						
						html+="<div id="+instance.id+"_catalog_"+ catalogId +" class=\"catalog "+(key==0?"first-catalog":"")+"\">";
						html+="<div class=\"catalog-header set-bordered-panel-heading\">";
							html+="<span class=\"catalog-name\">"+instance.msg("label.catalog")+" \""+replaceLocaleWithFlag(label, locales)+"\"</span>";
							
							html+="<progress value=\""+score+"\">"							
								//IE fix
								html+="<div class=\"progress-bar\">";
									html+="<span style=\"width: "+score+"%;\">"+score+"%</span>";
								html+="</div>";					        	
							html+="</progress>"
							html+="<span class=\"score-info\">"+Math.floor(score*100)+" % "+instance.msg("label.completed")+"</span>";
						html+="</div>";

						html+="<div class=\"catalog-details\">";
						html+="<h3 id=\""+instance.id+"_"+catalogId+"_missingPropLabel\">"+instance.msg("label.missing_properties")+"</h3>";

						//display missing props, if any
						if(json[key].missingFields !== undefined){
							html+="<ul class=\"catalog-missing-propList\">"
								if(json[key].missingFields.length > 0){
									for(var field in json[key].missingFields){
										html+="<li class=\"missing-field\">"+replaceLocaleWithFlag(json[key].missingFields[field].localized)+"</li>";								
									}
								} else {
									html+="<li class=\"no-missing-prop\">"+instance.msg("label.no_missing_prop")+"</li>";
								}
							html+="</ul>";
						} 
						html+="</div>";
						html+="</div>";
					}
					html+="</div>";	
					return html;
				} else {
					return null;
				}
			}

			//Replaces locale in formatted label by img markup with corresponding flag
			// eg: "legal name_en" would be replaced by "legal name <img>" with img tag having english flag as src
			function replaceLocaleWithFlag(field, locales){
				//localized field
				if(/_([a-z]{2})+/.test(field)){
					var match = field.match(/_([a-z]{2})+/g)[0];
					var locale = match.substring(1,3);

					var imgMarkup = "<img src=\"/share/res/components/images/flags/"+locale+".png\">";
					var replaced = field.replace(match, imgMarkup);
					return replaced;
				} else if(locales !== undefined){
					//localized catalog with only one locale set
					if(locales.length == 1){
						
						var locale = locales[0];
						var imgMarkup = "<img src=\"/share/res/components/images/flags/"+locale+".png\">";
						var replaced = field+imgMarkup;
						return replaced;
					} else {
						
						return field;
					}
				} else {
					return field;
				}
			}

			/**
			 * Colorizes input fields using a color palette per catalog in json
			 * json : catalogs
			 * id : radical id of inputs (eg : $id_prop_bcpg_legalName)
			 */ 
			function colorizeMissingFields(json, id){
				var i=0;
				var step=5; //value of hue between each catalog
				
				for(var key in json){
					var color = "hsl("+(i*360/7)+", "+(70+(i%20)*(i%2==0?1:-1))+"%, 50%)";
					
					var colorTipElement = document.createElement("SPAN");
					colorTipElement.style.backgroundColor=color;
					colorTipElement.className+="catalog-color";	
					colorTipElement.title=instance.msg("label.catalog")+" '"+json[key].label+(json[key].locales !== undefined && json[key].locales.length == 1 ? "("+json[key].locales[0]+")'": "'");
					
					if(json[key].missingFields !== undefined){
						
						//put a color tip for this catalog
						var catalogId = json[key].id;
						var locales = json[key].locales;
						
						if(locales !== undefined && locales != null && locales.length > 0){
							catalogId = catalogId+"_"+locales[0];
						}
						var labelId = instance.id+"_"+catalogId+"_missingPropLabel";
						
						if(json[key].missingFields.length > 0){
							var label = YAHOO.util.Dom.get(labelId);
							
							if(label !== undefined && label != null){
								label.parentNode.insertBefore(colorTipElement.cloneNode(false), label.nextSibling);
							}
						}
						
						//put color tip next to each non validated field according to the catalog
						for(var field in json[key].missingFields){							
							//try to find a prop or assoc with this field
							
							var fieldCode = json[key].missingFields[field].code.replace(":", "_");
							var fieldId="";
							
							var found = YAHOO.util.Dom.get(id+"_assoc_"+fieldCode);
							fieldId=id+"_assoc_"+fieldCode+"-cntrl";
							
							if(found === undefined || found == null){
								found = YAHOO.util.Dom.get(id+"_prop_"+fieldCode);
								fieldId=id+"_prop_"+fieldCode;
							}

							if(found !== undefined && found != null){
								if(found.className.contains("multi-assoc")){
									found = found.parentNode;
								}
								
								//put color tip
								var parent = found.parentNode;
								var labels = document.getElementsByTagName("label");
								

								for(var labelIndex = 0; labelIndex < labels.length; labelIndex++){
									var currentLabel = labels[labelIndex];									
									
									//checks if we're on the right label, and the catalog is not already labelled
									if(currentLabel.htmlFor.contains(fieldId) && currentLabel.parentNode.innerHTML.indexOf(colorTipElement.style.backgroundColor) == -1){
										currentLabel.parentNode.insertBefore(colorTipElement.cloneNode(false), currentLabel.nextSibling);										
									}
								}							
							} else {
								console.log("can't find any prop or assoc for field "+fieldCode);
								console.log("prop id would be "+id+"_prop_"+fieldCode);
								console.log("assoc id would be "+id+"_assoc_"+fieldCode);
							}							
						}
					}
					i++;
				}
			}

			//Affichage des notes et ajouts de tags dans les forms
			//init
			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "becpg/entity/catalog/node/" + instance.options.entityNodeRef.replace(":/",""),
				method : Alfresco.util.Ajax.GET,
				responseContentType : Alfresco.util.Ajax.JSON,
				successCallback : {
					fn : function (response){

						if(response.json !== undefined){
							var html = parseJsonToHTML(response.json);
							//console.log("html: "+html);
							YAHOO.util.Dom.get(this.id+"-entity-catalog").innerHTML=html;
														
							var insertId = this.id.replace("-mgr", "");							
							var formId = insertId+"-form";
							
							var form = YAHOO.util.Dom.get(formId);
							var pageContent = YAHOO.util.Dom.get(insertId);
							if(form !== undefined && form != null){
								
								var catalogs = YAHOO.util.Dom.get(this.id+"-entity-catalog");
								
								pageContent.className+="inline-block";
								catalogs.className+="inline-block ";
								catalogs.className+="catalogs ";
								YAHOO.util.Dom.insertAfter(catalogs,pageContent);
								
								colorizeMissingFields(response.json, insertId);
							}
						}

					},
					scope : instance
				},
				failureMessage : "Could not load entity catalog",
				execScripts : true
			}); 
		}
	});
})();
