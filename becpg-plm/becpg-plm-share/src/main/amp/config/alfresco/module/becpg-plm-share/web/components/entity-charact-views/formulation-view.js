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
	 * FormulationView constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.FormulationView} The new FormulationView
	 *         instance
	 * @constructor
	 */
	beCPG.component.FormulationView = function(htmlId) {

		beCPG.component.FormulationView.superclass.constructor.call(this, "beCPG.component.FormulationView", htmlId, [ "button", "container" ]);

		var dataGridModuleCount = 1;

		YAHOO.Bubbling.on("dataGridReady", function(layer, args) {
			if (dataGridModuleCount == 3) {
				// Initialize the browser history management library
				try {
					YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
				} catch (e2) {
					/*
					 * The only exception that gets thrown here is when the
					 * browser is not supported (Opera, or not A-grade)
					 */
					Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
					var obj = args[1];
					if ((obj !== null) && (obj.entityDataGridModule !== null)) {
						obj.entityDataGridModule.onHistoryManagerReady();
					}
				}
			}
			dataGridModuleCount++;
		});

		YAHOO.Bubbling.on("dirtyDataTable", this.formulate, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.FormulationView, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.FormulationView.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
			/**
			 * Current siteId.
			 * 
			 * @property siteId
			 * @type string
			 * @default ""
			 */
			siteId : "",

			/**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
			entityNodeRef : "",

			/**
			 * Current list
			 */
			list : ""
		},

		formulationLock : false,

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function FormulationView_onReady() {
			var instance = this;
			YAHOO.util.Event.addListener("dynamicCharactList-" + this.id + "-colCheckbox", "click", function(e) {
				YAHOO.Bubbling.fire("dynamicCharactList-" + instance.id + "refreshDataGrid");
			});

			//creates a new id
			var REQFILTER_EVENTCLASS = Alfresco.util.generateDomId(null, "reqType");

			//creates html divs out of json get
			function parseJsonToHTML(object){

				if(Object.keys(object).length > 0){

					var html="<div id=\"scoresDiv\" class=\"ctrlSumPreview dashlet datagrid\" style=\"visibility: visible;\">";

					//put score div
					if(object.scores !== undefined){
						var scores = object.scores;					
						var intScore = parseInt(scores.global);					
						var spriteIndex=(intScore/5>>0);

						html+="<ul><li class=\"title\">"+instance.msg("label.product.scores")+"</li><li id=\"scoreLi\" class=\"score-"+spriteIndex+"\" " +
						"title=\""+instance.msg("tooltip.components.validation")+": "+Math.floor(scores.details.componentsValidation)+
						"%\n"+instance.msg("tooltip.mandatory.completion")+": "+Math.floor(scores.details.mandatoryFields)+
						"%\n"+instance.msg("tooltip.specification.respect")+": "+Math.floor(scores.details.specifications)+"%\">";

						html+="<span>"+Math.floor(scores.global)+"%</span>";
						html+="</li></ul>";
					}

					//Parses each array mapped to dataType
					html+="<div class=\"dataTypeList\"><div class=\"title\">"+instance.msg("label.constraints.violations")+"<span class=\"req-all-all rclFilterSelected\"><a class=\"req-filter "+REQFILTER_EVENTCLASS + " href=\"#\">"+instance.msg("label.constraints.view-all")+"</a></span></div>";

					//if we have some constraints in res
					if((Object.keys(object).length > 1 && object.scores !== undefined) || (Object.keys(object).length > 0 && object.scores === undefined)){
						html+="<div class=\"rclFilterElt\"><div>";
						for(var dataType in object){

							if(dataType !== "scores"){
								var scoreInfo = "";

								html+="<div class=\"div-"+dataType.toLowerCase()+"\"><span class=\"span-"+dataType.toLowerCase()+"\"><a class=\"req-filter "+REQFILTER_EVENTCLASS+"\" href=\"#\">"+instance.msg("label.constraints."+dataType.toLowerCase())+scoreInfo+"</a></span><ul>";
								var types = object[dataType];
								for(var type in types){
									var value = types[type];
									html+="<li><span class=\"req-"+dataType.toLowerCase()+"-"+type+"\" title=\""+instance.msg("reqTypes."+type)+"\"><a class=\"req-filter "+REQFILTER_EVENTCLASS+ "\" href=\"#\"><span class=\"reqType"+type+"\"></span>"+value+"</a></li>";

								}        		  
								html+="</ul></div>";
							}
						}
						html+="</div></div></div>";
					}

					html+="</div>";

					return html;
				} else {
					return null;
				}
			} 

			//handles filtering on click
			var fnOnTypeFilterHandler = function PL__fnOnShowTaskHandler(layer, args) {
				try {
					var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");  
					var selectedItems = document.getElementsByClassName("rclFilterSelected");

					//sets clicked item to selected
					for(var i = 0; i < selectedItems.length; i++){
						selectedItems[i].classList.remove("rclFilterSelected");
					}
					var chgClass = owner;
					if(owner.parentNode.nodeName == "LI"){
						chgClass = owner.parentNode;
					}
					YAHOO.util.Dom.addClass(chgClass, "rclFilterSelected");

					//refreshes view by calling filter
					var splits = owner.className.split("-");  
					var type = (splits.length > 2 ? splits[2].split(" ")[0] : undefined); 
					var dataType = splits[1].charAt(0).toUpperCase()+splits[1].slice(1);                 
					YAHOO.Bubbling.fire("constraintsList-"+instance.id+"changeFilter",
							{
						filterOwner : "constraintsList-"+instance.id,
						filterId : (type === "all" && dataType === "All" ? "all": "filterform"),
						filterData : (type === "all" && dataType === "All" ? undefined : "{"+(type!==undefined ? ("\"prop_bcpg_rclReqType\":"+type) : "") + ( dataType !== null ? (type !== undefined ? "," : "") + ("\"prop_bcpg_rclDataType\":"+dataType) : "") +"}")
							});

					args[0].stopPropagation();
					args[1].decrepitate = true;
				} catch(e){
					alert(e);
				};
				return true;
			};          

			YAHOO.Bubbling.addDefaultAction(REQFILTER_EVENTCLASS, fnOnTypeFilterHandler);

			var recallWebScript = function FormulationView_recallWebScript(layer, args){
				//init
				var view = args[1].list;
				if(view !== undefined){
					instance.options.list = view;
				}
				
				Alfresco.util.Ajax.request({
					url : Alfresco.constants.PROXY_URI + "becpg/product/reqctrllist/node/" + instance.options.entityNodeRef.replace(":/","")+"?view="+instance.options.list,
					method : Alfresco.util.Ajax.GET,
					responseContentType : Alfresco.util.Ajax.JSON,
					successCallback : {
						fn : function (response){
							YAHOO.util.Dom.get("constraintsList-"+instance.id+"-scores").innerHTML= parseJsonToHTML(response.json, instance);

							var scoreDiv = YAHOO.util.Dom.get("scoreLi");
							if(scoreDiv !== undefined && scoreDiv != null){
								var scoreDivClassName = scoreDiv.className;
								var spriteIndex = scoreDivClassName.split("-")[1];
								var imgWidth = 74;
								var widthRatio = imgWidth/166;

								var	rightPos = (((spriteIndex-1)*(166+14)+2+(spriteIndex>4?10:0))*widthRatio)+"px";
								scoreDiv.style.backgroundPosition = "-"+rightPos+" 0px";
								scoreDiv.style.width=imgWidth+"px";
								scoreDiv.style.height=imgWidth+"px";

								var backgroundSize= Math.floor(3629*widthRatio)+"px "+Math.floor(396*widthRatio)+"px";
								scoreDiv.childNodes[0].style.lineHeight=imgWidth+"px";
								scoreDiv.childNodes[0].style.fontSize=3*widthRatio+"em";
								scoreDiv.style.backgroundSize=backgroundSize;
							} 
							
							var createdDiv = YAHOO.util.Dom.get("constraintsList-"+instance.id+"-scores");
							var constraintsListGrid = YAHOO.util.Dom.get("constraintsList-"+instance.id+"-grid");
							YAHOO.util.Dom.insertBefore(createdDiv, constraintsListGrid.parentNode);
						},
						scope : instance
					},
					failureMessage : "Could not load html template for version graph",
					execScripts : true
				});    
			}

			//automatic refresh on formulation
			YAHOO.Bubbling.on( "refreshDataGrids", recallWebScript, this);
			YAHOO.Bubbling.on( "activeDataListChanged", recallWebScript, this);
			
			//init
			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "becpg/product/reqctrllist/node/" + instance.options.entityNodeRef.replace(":/","")+"?view="+instance.options.list,
				method : Alfresco.util.Ajax.GET,
				responseContentType : Alfresco.util.Ajax.JSON,
				successCallback : {
					fn : function (response){

						YAHOO.util.Dom.get("constraintsList-"+instance.id+"-scores").innerHTML= parseJsonToHTML(response.json, instance);

						var scoreDiv = YAHOO.util.Dom.get("scoreLi");
						if(scoreDiv !== undefined && scoreDiv != null){
							var scoreDivClassName = scoreDiv.className;
							var spriteIndex = scoreDivClassName.split("-")[1];

							var imgWidth = 74;
							var widthRatio = imgWidth/166;

							var	rightPos = (((spriteIndex-1)*(166+14)+2+(spriteIndex>4?10:0))*widthRatio)+"px";

							scoreDiv.style.backgroundPosition = "-"+rightPos+" 0px";
							scoreDiv.style.width=imgWidth+"px";
							scoreDiv.style.height=imgWidth+"px";


							var backgroundSize= Math.floor(3629*widthRatio)+"px "+Math.floor(396*widthRatio)+"px";
							scoreDiv.childNodes[0].style.lineHeight=imgWidth+"px";
							scoreDiv.childNodes[0].style.fontSize=3*widthRatio+"em";
							scoreDiv.style.backgroundSize=backgroundSize;

							
						}
						//move dom elts			
						var createdDiv = YAHOO.util.Dom.get("constraintsList-"+instance.id+"-scores");
						var constraintsListGrid = YAHOO.util.Dom.get("constraintsList-"+instance.id+"-grid");
						YAHOO.util.Dom.insertBefore(createdDiv, constraintsListGrid.parentNode);
						
					},
					scope : instance
				},
				failureMessage : "Could not load html template for version graph",
				execScripts : true
			});    

		},

		formulate : function FormulationView_formulate() {
			if (!this.formulationLock) {

				var formulateButton = YAHOO.util.Selector.query('div.formulate'), me = this;

				Dom.addClass(formulateButton, "loading");

				me.formulationLock = true;
				Alfresco.util.Ajax.request({
					method : Alfresco.util.Ajax.GET,
					url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef.replace(":/", ""),
					successCallback : {
						fn : function(response) {
							YAHOO.Bubbling.fire("refreshDataGrids", {
								updateOnly : true,
								callback : function() {
									me.formulationLock  = false;
									Dom.removeClass(formulateButton, "loading");
								}
							});
						},
						scope : this
					}
				});
			}

		}
	});

})();
