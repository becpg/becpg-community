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
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector, Bubbling = YAHOO.Bubbling;


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


        return this;
    };

    YAHOO
            .extend(
                    beCPG.component.Properties,
                    Alfresco.DocumentMetadata,
                    { 
                 
                        fileUpload : null,
                        /**
                         * Fired by YUI when parent element is available for
                         * scripting. Initial History Manager event registration
                         * 
                         * @method onReady
                         */
                        onReady : function Properties_onReady()
                        {
                            var me = this;

                            // Upload logo event 
                            YAHOO.util.Event.addListener(this.id + "-uploadLogo-button", "click", this.doUploadLogo, this, true);
                            
                            // Parse the date
                            var dateEl = Dom.get(this.id + '-modifyDate');
                            dateEl.innerHTML = Alfresco.util.formatDate(Alfresco.util.fromISO8601(dateEl.innerHTML),
                                  Alfresco.util.message("date-format.default"));
                            
                            //Favourite
                            new Alfresco.Favourite(this.id + '-favourite').setOptions({
                                nodeRef : this.options.nodeRef,
                                type : "document"
                             }).display(this.options.isFavourite);
                            
                            beCPG.component.Properties.superclass.onReady.call(this);

                        },

                        doUploadLogo : function NodeHeader_doUploadLogo(e) {

                            if (this.fileUpload === null)
                            {
                                this.fileUpload = Alfresco.getFileUploadInstance();
                            }
                            
                            
                            var uploadConfig =
                            {
                               flashUploadURL: "becpg/entity/uploadlogo" ,
                               htmlUploadURL: "becpg/entity/uploadlogo.html" ,
                               updateNodeRef: this.options.nodeRef,
                               mode: this.fileUpload.MODE_SINGLE_UPLOAD,
                               onFileUploadComplete:
                               {
                                  fn: function onFileUploadComplete(complete)
                                  {
                                      var success = complete.successful.length;
                                      if (success != 0)
                                      {
                                         var noderef = complete.successful[0].nodeRef;
                                         YAHOO.Bubbling.fire("metadataRefresh");
                                      }
                                   },
                                  scope: this
                               }
                            };
                            this.fileUpload.show(uploadConfig);
                            YAHOO.util.Event.preventDefault(e);
                        
                    } ,
                    /**
                         * Refresh component in response to metadataRefresh event
                        *
                        * @method doRefresh
                        */
                       doRefresh: function DocumentMetadata_doRefresh()
                       {
                          YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
                          this.refresh('components/entity-charact-views/properties-view?nodeRef={nodeRef}' + (this.options.siteId ? '&site={siteId}' :  '') + (this.options.formId ? '&formId={formId}' :  ''));
                       }

               });
})();
