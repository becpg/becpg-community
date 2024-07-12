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
 * beCPG document actions
 * 
 * @namespace Alfresco
 * @class DocumentActions
 */
(function() {

	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionPublishFile",
		
		fn: function onActionPublishFile(p_record) {

           var templateUrl = Alfresco.constants.URL_SERVICECONTEXT + "modules/designer/publish?nodeRef="
                 + p_record.nodeRef + "&writeXml=false" + "&fileName=" + p_record.displayName;
           
           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : templateUrl,
              successCallback : {
                 fn : function() {
                     Alfresco.util.Ajax.request({
						 url: Alfresco.constants.URL_SERVICECONTEXT + "components/console/config/reload",
						 method: Alfresco.util.Ajax.GET,
						 responseContentType: Alfresco.util.Ajax.JSON,
						 successCallback: {
							 fn: function() {
								  Alfresco.util.Ajax.request({
									 url: Alfresco.constants.URL_SERVICECONTEXT + "messages?reset=true&d=" + new Date().getTime(),
									 method: Alfresco.util.Ajax.GET,
									 successCallback: {
										 fn: function() {
											 location.reload();
										 },
										 scope: this
									 },
									 failureCallback: {
										 fn: function() {
											 Alfresco.util.PopupManager.displayMessage({
												 text: this.msg("message.publish.failure")
											 });
										 },
										 scope: this
									 }
								 });
							 },
							 scope: this
						 },
						 failureCallback: {
							 fn: function() {
								 Alfresco.util.PopupManager.displayMessage({
									 text: this.msg("message.publish.failure")
								 });
							 },
							 scope: this
						 }
					 });

                 },
                 scope : this
              },
              failureMessage : this.msg("message.publish.failure", p_record.displayName),
              scope : this,
              execScripts : false
           });

		}
	});
	
	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionUnpublishFile",
		
		fn: function onActionUnpublishFile(p_record) {

           var templateUrl = Alfresco.constants.URL_SERVICECONTEXT + "modules/designer/unpublish?nodeRef="
                 + p_record.nodeRef + "&fileName=" + p_record.displayName;
           
           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : templateUrl,
              successCallback : {
                 fn : function() {
                     Alfresco.util.Ajax.request({
                        url : Alfresco.constants.URL_SERVICECONTEXT + "components/console/config/reload",
                        method : Alfresco.util.Ajax.GET,
                        responseContentType : Alfresco.util.Ajax.JSON,
                        successCallback: {
							 fn: function() {
								 location.reload();
							 },
							 scope: this
						 },
						 failureCallback: {
							 fn: function() {
								 Alfresco.util.PopupManager.displayMessage({
									 text: this.msg("message.unpublish.failure")
								 });
							 },
							 scope: this
						 }
                     });

                 },
                 scope : this
              },
              failureMessage : this.msg("message.unpublish.failure", p_record.displayName),
              scope : this,
              execScripts : false
           });

		}
	});

})();




