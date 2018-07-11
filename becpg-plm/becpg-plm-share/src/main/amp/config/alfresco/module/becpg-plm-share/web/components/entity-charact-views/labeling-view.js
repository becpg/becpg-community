/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
	 * LabelingView constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.LabelingView} The new LabelingView
	 *         instance
	 * @constructor
	 */
	beCPG.component.LabelingView = function(htmlId) {

		beCPG.component.LabelingView.superclass.constructor.call(this, "beCPG.component.LabelingView", htmlId, [ "button", "container" ]);
		var me = this;
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
        
		var fnActionHandler = function EntityDataGrid_fnActionHandler(layer, args)
        { 
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null)
            {
                if (typeof me[owner.className] == "function")
                {
                    args[1].stop = true;
                    var fieldId  = owner.id;
                    me[owner.className].call(me, fieldId);
                }
            }
            

            YAHOO.util.Event.preventDefault(args[0]);
            return false;
        };
        
        YAHOO.Bubbling.addDefaultAction("labeling-action", fnActionHandler);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.LabelingView, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.LabelingView.prototype, {
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

		formulate : function LabelingView_formulate() {
			    
			
				var formulateButton = YAHOO.util.Selector.query('div.formulate'), me = this;

				Dom.addClass(formulateButton, "loading");

				var localCount = beCPG.util.incLockCount();
				
				Alfresco.util.Ajax.request({
					method : Alfresco.util.Ajax.GET,
					url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef.replace(":/", "") + "?fast=true",
					responseContentType : Alfresco.util.Ajax.JSON,
					successCallback : {
						fn : function(response) {
							if(beCPG.util.lockCount() == localCount){
								YAHOO.Bubbling.fire("refreshDataGrids", {
									updateOnly : true,
									callback : function() {
										Dom.removeClass(formulateButton, "loading");
									}
								});
							}
						},
						scope : this
					},
					failureCallback : {
	                    fn : function(response) {
	                       if (response.json && response.json.message) {
	                          Alfresco.util.PopupManager.displayPrompt({
	                             title : this.msg("message.formulate.failure"),
	                             text : response.json.message
	                          });
	                       } else {
	                          Alfresco.util.PopupManager.displayMessage({
	                             text : this.msg("message.formulate.failure")
	                          });
	                       }
	                       Dom.removeClass(formulateButton, "loading");
	                    },
	                    scope : this
	                 }
				});

		},
		
		onShowTranslation : function showMultiLangualForm (fieldId){
			
			var nodeRef = fieldId.split("#")[1], field=fieldId.split("#")[2];
			
			
			new Alfresco.module.SimpleDialog(nodeRef+"-multilingualForm").setOptions({
              templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/multilingual-form/multilingual-form?nodeRef=" + nodeRef + "&field=" + field + "&readonly=true",
              actionUrl : Alfresco.constants.PROXY_URI + "becpg/form/multilingual/field/" + field + "?nodeRef=" + nodeRef,
              validateOnSubmit : false,
              destroyOnHide : true,
              width: "33em"
           }).show();
            
		},
		
		onCopyToClipboard : function( fieldId) {
			
			var htmlId = fieldId.split("#")[0]+ fieldId.split("#")[2].replace(":","_");
			
	    	if (document.selection) { 	
			    var range = document.body.createTextRange();
			    range.moveToElementText(document.getElementById(htmlId));
			    range.select().createTextRange();
			    document.execCommand("copy"); 

			} else if (window.getSelection) {
			    var range = document.createRange();
			     range.selectNode(document.getElementById(htmlId));
			     window.getSelection().addRange(range);
			     document.execCommand("copy");
			}
	    }

	});

})();
