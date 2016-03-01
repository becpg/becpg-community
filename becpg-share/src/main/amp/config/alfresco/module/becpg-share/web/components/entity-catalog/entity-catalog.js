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
					var html="<div>";

					html+="<h2>"+instance.msg("label.property_completion")+"</h2>";

					for(var key in json){
						var score = json[key].score;

						html+="<div class=\"catalog\">";
						html+="<div class=\"catalog-header set-bordered-panel-heading\">";
						html+="<span class=\"catalog-name\">"+instance.msg("label.catalog")+" \""+replaceLocaleWithFlag(key)+"\"</span>";
						html+="<span class=\"score-info\">"+Math.floor(score*100)+" % "+instance.msg("label.completed")+"</span>";
						html+="<progress value=\""+score+"\">"							
						//IE fix
						html+="<div class=\"progress-bar\">";
						html+="<span style=\"width: "+score+"%;\">"+score+"%</span>";
						html+="</div>";					        	
						html+="</progress>"

							html+="</div>";

						html+="<div class=\"catalog-details\">";
						html+="<h3>"+instance.msg("label.missing_properties")+"</h3>";

						//display missing props, if any
						if(json[key].fields !== undefined){
							html+="<ul class=\"catalog-missing-propList\">"

								if(json[key].fields.length > 0){
									for(var field in json[key].fields){
										html+="<li class=\"missing-field\">"+replaceLocaleWithFlag(json[key].fields[field])+"</li>";								
									}
								} else {
									html+="<li>"+instance.msg("label.no_missing_prop")+"</li>";
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
			// eg: "legal name (en)" would be replaced by "legal name <img>" with img tag having english flag as src
			function replaceLocaleWithFlag(field){
				if(/(\([a-z]{2}\))+/.test(field)){
					var match = field.match(/(\([a-z]{2}\))+/g)[0];
					var locale = match.substring(1,3);

					var imgMarkup = "<img src=\"/share/res/components/images/flags/"+locale+".png\">";
					var replaced = field.replace(match, imgMarkup);
					console.log("match: "+match+", locale: "+locale+", imgMarkup: "+imgMarkup+", replaced: \""+replaced+"\"");
					return replaced;
				} else {
					return field;
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
						console.log("response : "+JSON.stringify(response.json));

						if(response.json !== undefined){
							var html = parseJsonToHTML(response.json);
							//console.log("html: "+html);
							YAHOO.util.Dom.get(this.id+"-entity-catalog").innerHTML=html;
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
