/*******************************************************************************
 *  Copyright (C) 2010-2020 beCPG. 
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
 * @class beCPG.component.ReportViewer
 */
(function()
{
    

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom;


    /**
     * ReportViewer constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.component.ReportViewer} The new ReportViewer instance
     * @constructor
     */
    beCPG.component.ReportViewer = function ReportViewer_constructor(htmlId)
    {
        beCPG.component.ReportViewer.superclass.constructor.call(this, htmlId);
        
        YAHOO.Bubbling.on("versionChangeFilter", this.onVersionChanged,this);

        this.preferencesService = new Alfresco.service.Preferences();

        return this;
    };

    YAHOO
            .extend(
                    beCPG.component.ReportViewer,
                    Alfresco.WebPreview,
                    { 
                        
                        options :{
                            report : null,
                            itemType : null,
                            currVersionNodeRef : null,
                            reports :[]
                         },
                      

                        /**
                         * Fired by YUI when parent element is available for
                         * scripting. Initial History Manager event registration
                         * 
                         * @method onReady
                         */
                        onReady : function ReportViewer_onReady()
                        {
                            var me = this;

                            if (this.options.report !== null && Dom.get( me.id + "-entityReportPicker-button"))
                            {

                                
                                this.widgets.entityReportPicker = new YAHOO.widget.Button(
                                        me.id + "-entityReportPicker-button",
                                        {
                                            type : "split",
                                            menu : me.id + "-entityReportPicker-select",
                                            lazyloadmenu : false
                                        });

                                this.widgets.entityReportPicker.on("click", me.onEntityReportPickerClicked, me, true);

                                this.widgets.entityReportPicker.getMenu().subscribe("click", function(p_sType, p_aArgs)
                                {
                                    var menuItem = p_aArgs[1];
                                    if (menuItem)
                                    {
                                        me.widgets.entityReportPicker.set("label", menuItem.cfg.getProperty("text"));
                                        me.onEntityReportPickerClicked.call(me, menuItem);
                                    }
                                });

                                if (this.options.report.isSelected)
                                {
                                    var menuItems = me.widgets.entityReportPicker.getMenu().getItems();
                                    for ( var index in menuItems)
                                    {
                                        if (menuItems.hasOwnProperty(index))
                                        {
                                            if (menuItems[index].value === this.options.report.nodeRef)
                                            {
                                                me.widgets.entityReportPicker.set("label", menuItems[index].cfg
                                                        .getProperty("text"));
                                                me.onEntityReportPickerClicked.call(me, menuItems[index]);
                                                break;
                                            }
                                        }

                                    }

                                }

                              
                                if (Dom.get("toolbar-contribs-" + this.id))
                                {
                                    var controls = Dom.getChildren("toolbar-contribs-" + this.id);
                                    if (controls)
                                    {
                                        for ( var el in controls)
                                        {
                                            (new YAHOO.util.Element("toolbar-contribs")).appendChild(controls[el]);
                                        }
                                    }
                                }
                                
                            }
                            
                            YAHOO.Bubbling.fire("registerToolbarButtonAction", {
                                actionName : "entity-print-report",
                                evaluate : function(asset, entity) {
                                    return asset.name !== null && asset.name.indexOf("View-reports") == 0;
                                },
                                fn : function(instance){
                                	me.onPrintClick(me);
                                }
                             });


                            beCPG.component.ReportViewer.superclass.onReady.call(this);

                        },

                        onPrintClick : function (scope){
                        	var nodeRefAsLink = scope.options.nodeRef.replace(":/", ""),
                        	noCache = "noCache=" + new Date().getTime();
                        	printJS(Alfresco.constants.PROXY_URI + "api/node/"+nodeRefAsLink + "/content/"+ encodeURIComponent(scope.options.name)+ "?c=force&" + noCache + "&a=false");
                        },
                        
                        /**
                         * @param menuItem
                         */
                        onEntityReportPickerClicked : function ReportViewer_onEntityReportPickerClicked(menuItem)
                        {
                            var scope = this;
                            if (menuItem)
                            {
                                scope.widgets.entityReportPicker.value = encodeURIComponent(menuItem.value);
                                
                                scope.preferencesService
                                        .set(
                                                scope.getPickerPreference(),
                                                menuItem.cfg.getProperty("text"),
                                                {
                                                    successCallback :
                                                    {
                                                        fn : function()
                                                        {
                                                                if (encodeURIComponent(scope.options.report.nodeRef) != scope.widgets.entityReportPicker.value)
                                                                {
                                                                    scope.options.nodeRef = decodeURIComponent(scope.widgets.entityReportPicker.value);
                                                                    
                                                                    for(var i in scope.options.reports){
                                                                    	if(scope.options.reports[i].nodeRef == scope.options.nodeRef){
                                                                    		scope.options.report = scope.options.reports[i];
                                                                    		break;
                                                                    		
                                                                    	}
                                                                    }
                                                                   
                                                                   
                                                                    if(scope.options.name.split('.').pop()!=scope.options.report.name.split('.').pop()){
                                                                    	 window.location.href = window.location.href.split("#")[0];
                                                                    }
                                                                    
                                                                    scope.options.name = scope.options.report.name;
                                                                    
                                                                    
                                                                    
                                                                    YAHOO.Bubbling.fire("previewChangedEvent");
                                                                }
                                                        },
                                                        scope : this
                                                    }
                                                });
                            }

                        },

                        
                        onPreviewChanged: function WebPreview_onPreviewChanged(event)
                        {
                           if(!this.options.report){
                        	   window.location.href = window.location.href.split("#")[0];
                           }	
                        	
                           this.options.avoidCachedThumbnail = true;

                           YAHOO.util.Event.preventDefault(event);
                           YAHOO.util.Event.stopPropagation(event);
                        },
                        
                        
                        getPickerPreference : function ReportViewer_getPickerPreference()
                        {
                            return "fr.becpg.repo.report." + this.options.itemType.replace(":", "_") + ".view";

                        },
                        
                        updateCurrentVersionUrlQueryParams : function ReportViewer_updateCurrentVersionUrlQueryParams(currVersionNodeRef)
                        {
                        	var currVersionNodeRefParam = YAHOO.util.History.getQueryStringParameter('currVersionNodeRef');
                        	currVersionNodeRef = currVersionNodeRef != null ? currVersionNodeRef : YAHOO.util.History.getQueryStringParameter('nodeRef');
                        	var url = window.location.href;
                        	if(currVersionNodeRefParam != null){
                        		url = url.replace(/&currVersionNodeRef=.*/g, "&currVersionNodeRef=" + currVersionNodeRef);
                        	} else {
                        		url +=  "&currVersionNodeRef=" + currVersionNodeRef;
                        	}
                        	window.history.pushState({path:url},'',url);
                        },
                        
                        onVersionChanged : function ReportViewer_onVersionChanged(layer, args)
                        {
                            YAHOO.Bubbling.unsubscribe("versionChangeFilter", this.onVersionChanged, this);
                        	var el = new YAHOO.util.Element("toolbar-contribs");
                        	if (el.hasChildNodes()) {
                        	    el.removeChild(el.get('firstChild'));
                        	}

                            var obj = args[1];
                            if ((obj !== null) && obj.filterId !== null &&  obj.filterId === "version" && obj.filterData !== null)
                            {
                                this.refresh('components/entity-charact-views/reports-view?currVersionNodeRef='+(this.options.currVersionNodeRef!=null ? this.options.currVersionNodeRef : '{nodeRef}')+'&nodeRef='+ obj.filterData+ (this.options.siteId ? '&site={siteId}' :  ''));
                                this.updateCurrentVersionUrlQueryParams(obj.filterData);
                             } else {
	
							 var node = YAHOO.util.History.getQueryStringParameter('nodeRef');
						
							 if(node!=null){
                                 this.refresh('components/entity-charact-views/reports-view?nodeRef='+node+ (this.options.siteId ? '&site={siteId}' :  ''));   
                                 this.updateCurrentVersionUrlQueryParams(null);
								}
                             }
                            
                            
                        }

                    });
})();
