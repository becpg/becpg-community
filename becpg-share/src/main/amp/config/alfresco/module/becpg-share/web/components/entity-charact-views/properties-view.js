/*******************************************************************************
 *  Copyright (C) 2010-2015 beCPG. 
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
 * Document and Folder header component.
 * 
 * @namespace beCPG
 * @class beCPG.component.Properties
 */
(function()
{
    

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
    formatDate = Alfresco.util.formatDate,
    fromISO8601 = Alfresco.util.fromISO8601;


    /**
     * Properties constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.component.Properties} The new Properties instance
     * @constructor
     */
    beCPG.component.Properties = function Properties_constructor(htmlId)
    {
        beCPG.component.Properties.superclass.constructor.call(this, htmlId);
        
        
        // Decoupled event listeners
        YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
        
        YAHOO.Bubbling.on("versionChangeFilter", this.onVersionChanged,this);
        return this;
    };

    YAHOO
            .extend(
                    beCPG.component.Properties,
                    Alfresco.CommentsList,
                    { 
                 
                        options : {
                            currVersionNodeRef : null,
                            /**
                             * The nodeRefs to load the form for.
                             *
                             * @property nodeRef
                             * @type string
                             * @required
                             */
                            nodeRef: null,

                            /**
                             * The current site (if any)
                             *
                             * @property site
                             * @type string
                             */
                            site: null,

                            /**
                             * The form id for the form to use.
                             *
                             * @property destination
                             * @type string
                             */
                            formId: null
                            
                        },
                        
                        fileUpload : null,
                        /**
                         * Fired by YUI when parent element is available for
                         * scripting. Initial History Manager event registration
                         * 
                         * @method onReady
                         */
                        onReady : function Properties_onReady()
                        {
                            // Upload logo event 
                            YAHOO.util.Event.addListener(this.id + "-uploadLogo-button", "click", this.doUploadLogo, this, true);
                            
                            // Parse the date
                            var dateEl = Dom.get(this.id + '-modifyDate');
                            dateEl.innerHTML = Alfresco.util.formatDate(Alfresco.util.fromISO8601(dateEl.innerHTML),
                                  Alfresco.util.message("date-format.default"));
                            
                            //Favourite
                            new Alfresco.Favourite(this.id + '-favourite').setOptions({
                                nodeRef : this.options.nodeRef,
                                type : "folder"
                             }).display(this.options.isFavourite);
                            
                            if(Dom.get(this.id+'-properties-tabview')!=null){
                            	this.widgets.tabView = new YAHOO.widget.TabView(this.id+'-properties-tabview');
                            }
                            
                            // Load the form
                            Alfresco.util.Ajax.request(
                            {
                               url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
                               dataObj:
                               {
                                  htmlid: this.id + "-formContainer",
                                  itemKind: "node",
                                  itemId: this.options.nodeRef,
                                  formId: this.options.formId,
                                  mode: "view"
                               },
                               successCallback:
                               {
                                  fn: this.onFormLoaded,
                                  scope: this
                               },
                               failureMessage: this.msg("message.failure"),
                               scope: this,
                               execScripts: true
                            });
                            
                            
                            beCPG.component.Properties.superclass.onReady.call(this);
                        },
                        
                        /**
                         * Helper function to position DOM elements
                         *
                         * @method synchronizeElements
                         */
                        synchronizeElements: function synchronizeElements(syncEl, sourceEl)
                        {
                           var sourceYuiEl = new YAHOO.util.Element(sourceEl),
                              syncYuiEl = new YAHOO.util.Element(syncEl),
                              region = YAHOO.util.Dom.getRegion(sourceYuiEl.get("id"));

                           syncYuiEl.setStyle("position", "absolute");
                           if(Dom.get(this.id+'-properties-tabview')==null){
                        	   syncYuiEl.setStyle("left", region.left + "px");
                        	   syncYuiEl.setStyle("top", region.top + "px");
                           } else {
                        	   var tabRegion = YAHOO.util.Dom.getRegion(this.id+'-properties-tabview');
                        	   
                        	   syncYuiEl.setStyle("left", (region.left - tabRegion.left)+"px");
                        	   syncYuiEl.setStyle("top", (region.top - tabRegion.top)+ "px");
                           }
                           syncYuiEl.setStyle("width", region.width + "px");
                           syncYuiEl.setStyle("height", region.height + "px");
                        },


                        /**
                         * Called when a workflow form has been loaded.
                         * Will insert the form in the Dom.
                         *
                         * @method onFormLoaded
                         * @param response {Object}
                         */
                        onFormLoaded: function Properties_onFormLoaded(response)
                        {
                           var formEl = Dom.get(this.id + "-formContainer"),
                              me = this;
                           formEl.innerHTML = response.serverResponse.responseText;
                           Dom.getElementsByClassName("viewmode-value-date", "span", formEl, function()
                           {
                              var showTime = Dom.getAttribute(this, "data-show-time"),
                                  fieldValue = Dom.getAttribute(this, "data-date-iso8601"),
                                  dateFormat = (showTime=='false') ? me.msg("date-format.defaultDateOnly") : me.msg("date-format.default"),
                                  // MNT-9693 - Pass the ignoreTime flag
                                  ignoreTime = showTime == 'false',
                                  theDate = fromISO8601(fieldValue, ignoreTime);
                              
                              this.innerHTML = formatDate(theDate, dateFormat);
                           });
                        },
                        
                        doUploadLogo : function Properties_doUploadLogo(e) {

                            if (this.fileUpload === null)
                            {
                                this.fileUpload = Alfresco.getFileUploadInstance();
                            }
                            
                            var me = this;
                            
                            var uploadConfig =
                            {
                               flashUploadURL: "becpg/entity/uploadlogo" ,
                               htmlUploadURL: "becpg/entity/uploadlogo.html" ,
                               updateNodeRef: this.options.nodeRef,
                               mode: this.fileUpload.MODE_SINGLE_UPLOAD,
                               suppressRefreshEvent : true,
                               onFileUploadComplete:
                               {
                                  fn: function onFileUploadComplete(complete)
                                  {
                                      var success = complete.successful.length;
                                      if (success != 0)
                                      {
                                    	   var productLogo =  Dom.get(me.id+"-productLogo");
                                    	  setTimeout(function(){  productLogo.src =  productLogo.src+"&"+ new Date().getTime(); }, 3000);
                                      }
                                   },
                                   scope: this
                               }
                            };
                            this.fileUpload.show(uploadConfig);
                            YAHOO.util.Event.preventDefault(e);
                        
                    } ,

                    doRefresh: function Properties_doRefresh()
                    {
                        YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
                        this.refresh('components/entity-charact-views/properties-view?nodeRef={nodeRef}' + (this.options.siteId ? '&site={siteId}' :  '') + (this.options.formId ? '&formId={formId}' :  ''));
                    },

                    onVersionChanged : function Properties_onVersionChanged(layer, args)
                    {
                    	YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
                        YAHOO.Bubbling.unsubscribe("versionChangeFilter", this.onVersionChanged, this);
                        var obj = args[1];
                        if ((obj !== null) && obj.filterId !== null &&  obj.filterId === "version" && obj.filterData !== null)
                        {
                           this.refresh('components/entity-charact-views/properties-view?currVersionNodeRef='+(this.options.currVersionNodeRef!=null ? this.options.currVersionNodeRef : '{nodeRef}')+'&nodeRef='+ obj.filterData+ (this.options.siteId ? '&site={siteId}' :  '') + (this.options.formId ? '&formId={formId}' :  ''));   
                         } else if(this.options.currVersionNodeRef!=null){
                            this.refresh('components/entity-charact-views/properties-view?nodeRef='+ this.options.currVersionNodeRef+ (this.options.siteId ? '&site={siteId}' :  '') + (this.options.formId ? '&formId={formId}' :  ''));   
                         }
                    }

               });
})();
