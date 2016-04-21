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
			
			function parseJsonToHTML(json){
				//displays things if there are any catalogs
				if(json !== undefined && json != null && Object.keys(json).length > 0){
					
					var html="<div class=\"entity-catalog\">";				
					for(var key in json){
						var score = json[key].score;
						var locale = json[key].locale;
						var label = json[key].label;
						var catalogId = json[key].id;
						
						if(locale !== undefined && locale != null ){
							catalogId = catalogId+"_"+locale;
						}
						
						html+="<div class=\"catalog "+(key==0?"first-catalog":"")+"\">";
						html+="<div class=\"catalog-header set-bordered-panel-heading\">";
							html+="<span class=\"catalog-name\">"+instance.msg("label.catalog")+" \""+label+
							(locale!=null?"<img src=\"/share/res/components/images/flags/"+locale+".png\">":"")+"\"</span>";
							
							html+="<progress value=\""+(score/100)+"\">";							
								//IE fix
								html+="<div class=\"progress-bar\">";
									html+="<span style=\"width: "+score+"%;\">"+score+"%</span>";
								html+="</div>";					        	
							html+="</progress>";
							html+="<span class=\"score-info\">"+Math.floor(score)+" % "+instance.msg("label.completed")+"</span>";
						html+="</div>";

						html+="<div class=\"catalog-details\">";
						html+="<h3 >"+instance.msg("label.missing_properties")+"</h3>";

						//display missing props, if any
						if(json[key].missingFields !== undefined){
							html+="<ul class=\"catalog-missing-propList\">";
								for(var field in json[key].missingFields){
									html+="<li class=\"missing-field\" >"
											+json[key].missingFields[field].displayName+
											(json[key].missingFields[field].locale!=null?"<img src=\"/share/res/components/images/flags/"
													+json[key].missingFields[field].locale+".png\">":"")+"</li>";	
								}
								
							html+="</ul>";
						} 
						html+="</div>";
						html+="</div>";
					}
					html+="</div>";	
					return html;
				} else {
					return "<span class=\"no-missing-prop\">"+instance.msg("label.no_missing_prop")+"</span>";
				};
				
			}

			/**
			 * Colorizes input fields using a color palette per catalog in json
			 * json : catalogs
			 * id : radical id of inputs (eg : $id_prop_bcpg_legalName)
			 */ 
			function colorizeMissingFields(json, id){
				var i=0;
				
				for(var key in json){
					var color = json[key].color;
					
					var colorTipElement = document.createElement("SPAN");
					colorTipElement.style.backgroundColor=color;
					colorTipElement.className+="catalog-color";	
					colorTipElement.title=instance.msg("label.catalog")+" '"+json[key].label+(json[key].locale !== undefined && json[key].locale.length == 1 ? "("+json[key].locale+")'": "'");
					
					if(json[key].missingFields !== undefined){
						
						//put a color tip for this catalog
						var catalogId = json[key].id;
						var locale = json[key].locale;
						
						if(locale !== undefined && locale != null ){
							catalogId = catalogId+"_"+locale;
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
							
							var fieldCode = json[key].missingFields[field].id.replace(":", "_");
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
								var labels = document.getElementsByTagName("label");
								

								for(var labelIndex = 0; labelIndex < labels.length; labelIndex++){
									var currentLabel = labels[labelIndex];									
									
									//checks if we're on the right label, and the catalog is not already labelled
									var hasLocaleIcon = false;
									if(currentLabel.htmlFor.contains(fieldId) && currentLabel.parentNode.innerHTML.indexOf(colorTipElement.style.backgroundColor) == -1){
										if(currentLabel.childNodes){
											for(var child in currentLabel.childNodes){
												var currentChildNode = currentLabel.childNodes[child];
												if(currentChildNode.nodeType == Node.ELEMENT_NODE && currentChildNode.className.contains("locale-icon")){
													hasLocaleIcon = true;
													break;
												}
											}
										}
										
										if(hasLocaleIcon){
											currentLabel.parentNode.insertBefore(colorTipElement.cloneNode(false), currentLabel.nextSibling);	
										} else {
											currentLabel.innerHTML+=colorTipElement.outerHTML;
										}
									}
								}							
							} else {
								
								var absentMissingFieldId = "missing-field_"+json[key]+"_"+json[key].missingFields[field].displayName;
								
								var absentMissingFieldHTMLElement = YAHOO.util.Dom.get(absentMissingFieldId);
								
								if(absentMissingFieldHTMLElement !== undefined && absentMissingFieldHTMLElement != null){
									absentMissingFieldHTMLElement.outerHTML = "";
								}
							}							
						}
					}
					i++;
				}
			}

			YAHOO.util.Dom.get(this.id+"-entity-catalog").innerHTML='<span class="wait">' + Alfresco.util.encodeHTML(this.msg("label.loading")) + '</span>';
			
			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "becpg/entity/catalog/node/" + instance.options.entityNodeRef.replace(":/",""),
				method : Alfresco.util.Ajax.GET,
				responseContentType : Alfresco.util.Ajax.JSON,
				successCallback : {
					fn : function (response){

						if(response.json !== undefined ){
							var html = parseJsonToHTML(response.json),
							  catalogs = YAHOO.util.Dom.get(this.id+"-entity-catalog");
							
							  catalogs.innerHTML=html;

							if( response.json != null && Object.keys(response.json).length > 0){
							   var insertId = this.id.replace("_cat","").replace("-mgr", "");							
							   var form = YAHOO.util.Dom.get(insertId+"-form");
								
								if(form !== undefined && form != null){
									
									var pageContent = YAHOO.util.Dom.get(insertId);
									YAHOO.util.Dom.addClass(pageContent,"inline-block");
									YAHOO.util.Dom.addClass(catalogs,"inline-block");
									YAHOO.util.Dom.addClass(catalogs,"catalogs");
									YAHOO.util.Dom.insertAfter(catalogs,pageContent);
									YAHOO.util.Dom.removeClass(this.id+"-entity-catalog","hidden");
									
									colorizeMissingFields(response.json, insertId);
								}
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
