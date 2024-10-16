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

	var $isValueSet = Alfresco.util.isValueSet;



	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionEntityTplSynchronizeEntities",
		fn: function onActionEntityTplSynchronizeEntities(asset) {
			Alfresco.util.PopupManager.displayMessage({
				text: this.msg("message.synchronize-entities.please-wait-mail"),
				displayTime: 5
			});

			Alfresco.util.Ajax.request({
				method: Alfresco.util.Ajax.GET,
				url: Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
					+ "/synchronizeEntities"
			});
		}
	});

	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionEntityTplFormulateEntities",
		fn: function onActionEntityTplFormulateEntities(asset) {
			Alfresco.util.PopupManager.displayMessage({
				text: this.msg("message.formulate-entities.please-wait-mail"),
				displayTime: 5
			});

			Alfresco.util.Ajax.request({
				method: Alfresco.util.Ajax.GET,
				url: Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
					+ "/formulateEntities"
			});
		}
	});


	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionCompareEntity",
		fn: function onActionCompareEntity(p_record) {
			var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + p_record.nodeRef.replace(":/", "") + "/";

			// Always create a new instance
			this.modules.entityCompare = new Alfresco.module.SimpleDialog(this.id + "-entityCompare").setOptions({
				width: "33em",
				templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-compare/entity-compare?entityNodeRef=" + p_record.nodeRef,
				actionUrl: actionUrl,
				validateOnSubmit: false,
				firstFocus: this.id + "-entityCompare-entities-field",
				doBeforeFormSubmit: {
					fn: function(form) {
						this.modules.entityCompare.form.setAJAXSubmit(false);
						this.modules.entityCompare.hide();
						var reportSelect = YAHOO.util.Dom.get(this.id + "-entityCompare-reportTemplate");
						var fileName = reportSelect.options[reportSelect.selectedIndex].getAttribute("fileName");
						
						var entityList = YAHOO.util.Dom.get(this.id + "-entityCompare-entities-added").value;
						var versionList = YAHOO.util.Dom.get(this.id + "-entityCompare-versions-added").value;
						var commonList = "";
						
						if (entityList != "" && versionList != "") {
							commonList = entityList + "," + versionList;
						} else {
							commonList = entityList + versionList;
						}
						
						window.location.href = actionUrl + fileName + "?entities=" + commonList + "&tplNodeRef=" + reportSelect.value + "&noCache=" + new Date().getTime();
					},
					scope: this
				}
			});

			this.modules.entityCompare.show();
		}
	});


	YAHOO.Bubbling.fire("registerAction", {
		actionName: "onActionCreateSupplier",
		fn: function onActionCreateSupplier(p_record) {
			var nodeRef = new Alfresco.util.NodeRef(p_record.nodeRef);
			var popupKind = "supplier-account", li = '', colCount = 0;
			var html = '<div class="hd">' + this.msg("header." + popupKind + ".dialog") + '</div>';
			html += '<div class="bd">';
			html += '<form  class="form-container">';
			html += '<div class="form-fields bulk-edit">';
			html += '   <div class="set">';
			html += '        <div class="form-field">';
			html += '			<div  id="' + this.id + '-columns-list" />';

			li += '<li style="margin: 10px 0;"><input id="propEmail" type="email" required name="propEmail" placeholder="supplier@becpg.fr"/></li>';

			html += '<span>' + this.msg("label.supplier-account.title") + '</span><br/><br/><ul style="width:' + ((colCount + 1) * 20) + 'em;">' + li + '</ul>';
			html += '          </div>';
			html += '<div style="display: inline-block;"> <input id="propNotify"  type="checkbox" name="propNotify" /> <label for="propNotify" style="display: inline-block"> ' + this.msg("label.notify-supplier") + '</label></div>';
			html += '       </div>';
			html += '    </div>';
			html += '<div id="' + this.id + '-' + popupKind + '-ft" class="bdft">';
			html += '</div>';
			html += '</form></div>';

			var containerDiv = document.createElement("div");
			containerDiv.innerHTML = html;

			this.widgets.columnsListPanel = Alfresco.util.createYUIPanel(containerDiv, {
				draggable: true,
				width: "33em"
			});

			var divEl = Dom.get(this.id + '-' + popupKind + '-ft');

			divEl.innerHTML = '<input id="' + this.id   +'-bulk-edit-ok" type="submit" value="' + this.msg("button.ok") + '" />';

			this.widgets.columnsListPanel.show();

			this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function() {
				var me = this;

				var containerEl = Dom.get(this.id + '-columns-list').parentNode;
				var selectedFields = Selector.query('input[type="checkbox"]', containerEl);
				var notifySupplier = selectedFields[0].checked;
				var emailAddress = Dom.get("propEmail").value;

				var emailRegx = /\S+@\S+\.\S+/;
				if (emailRegx.test(emailAddress)) {
					me.widgets.columnsListPanel.hide();

					Alfresco.util.Ajax.jsonPost({
						url: Alfresco.constants.PROXY_URI + "becpg/supplier/create-supplier?emailAddress=" + emailAddress + "&notifySupplier=" + notifySupplier + "&nodeRef=" + nodeRef,
						successCallback: {
							fn: function(response) {
								Alfresco.util.PopupManager.displayMessage({
									text: this.msg("message.createSupplier.success", response.json.login)
								});
								location.reload();
							},
							scope: this
						},
						failureCallback: {
							fn: function(response) {
								Alfresco.util.PopupManager.displayMessage({
									text: this.msg("message.createSupplier.failure"),
									displayTime: 5
								});
							},
							scope: this
						}
					});

				} else {
					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.email.notValide")
					});
				}


			});

		}
	});

})();




