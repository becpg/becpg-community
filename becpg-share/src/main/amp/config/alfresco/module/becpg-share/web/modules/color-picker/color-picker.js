/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
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
 * @namespace beCPG.module
 * @class beCPG.module.ColorPicker
 */
(function() {

	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, KeyListener = YAHOO.util.KeyListener;

	beCPG.module.ColorPicker = function(containerId) {
		this.name = "beCPG.module.ColorPicker";
		this.id = containerId;

		var instance = Alfresco.util.ComponentManager.get(this.id);
		if (instance !== null) {
			throw new Error("An instance of beCPG.module.ColorPicker already exists.");
		}

		/* Register this component */
		Alfresco.util.ComponentManager.register(this);

		// Load YUI Components
		Alfresco.util.YUILoaderHelper.require([ "button", "container" ], this.onComponentsLoaded, this);

		return this;

	};

	beCPG.module.ColorPicker.prototype = {

		/**
		 * The default config for the gui state for the historic properties
		 * dialog. The user can override these properties in the show() method.
		 * 
		 * @property defaultShowConfig
		 * @type object
		 */
		defaultShowConfig : {
			fieldId : null,
			nodeRefs : null,
			itemType : null,
			selectedColor : null
		},

		/**
		 * The merged result of the defaultShowConfig and the config passed in
		 * to the show method.
		 * 
		 * @property showConfig
		 * @type object
		 */
		showConfig : {},

		/**
		 * Object container for storing YUI widget and HTMLElement instances.
		 * 
		 * @property widgets
		 * @type object
		 */
		widgets : {},

		/**
		 * Fired by YUILoaderHelper when required component script files have
		 * been loaded into the browser.
		 * 
		 * @method onComponentsLoaded
		 */
		onComponentsLoaded : function HPV_onComponentsLoaded() {
			// Shortcut for dummy instance
			if (this.id === null) {
				return;
			}
		},

		/**
		 * Show can be called multiple times and will display the dialog in
		 * different ways depending on the config parameter.
		 * 
		 * @method show
		 * @param config
		 *            {object} describes how the dialog should be displayed The
		 *            config object is in the form of: { nodeRef: {string}, //
		 *            the nodeRef version: {string} // the version to show
		 *            properties of }
		 */
		show : function HPV_show(config) {
			// Merge the supplied config with default config and check mandatory
			// properties
			this.showConfig = YAHOO.lang.merge(this.defaultShowConfig, config);

			// Check if the dialog has been showed before
			if (this.widgets.panel) {

				this.updateConfig();

				// The displaying.
				this._showPanel();
			} else {
				// If it hasn't load the gui (template) from the server
				Alfresco.util.Ajax.request({
					url : Alfresco.constants.URL_SERVICECONTEXT + "modules/color-picker/color-picker?htmlid=" + this.id,
					successCallback : {
						fn : this.onTemplateLoaded,
						scope : this
					},
					failureMessage : "Could not load html template for properties viewer",
					execScripts : true
				});

				// Register the ESC key to close the dialog
				this.widgets.escapeListener = new KeyListener(document, {
					keys : KeyListener.KEY.ESCAPE
				}, {
					fn : this.onCancelButtonClick,
					scope : this,
					correctScope : true
				});

			}

		},

		/**
		 * Called when the dialog html template has been returned from the
		 * server. Creates the YIU gui objects such as the panel.
		 * 
		 * @method onTemplateLoaded
		 * @param response
		 *            {object} a Alfresco.util.Ajax.request response object
		 */
		onTemplateLoaded : function HPV_onTemplateLoaded(response) {

			// Inject the template from the XHR request into a new DIV element
			var containerDiv = document.createElement("div");
			containerDiv.innerHTML = response.serverResponse.responseText;

			var dialogDiv = YAHOO.util.Dom.getFirstChild(containerDiv);

			// Create the panel from the HTML returned in the server reponse
			this.widgets.panel = Alfresco.util.createYUIPanel(dialogDiv, {
				draggable : true,
				width : "15.2em"
			});

			this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel-button", this.onCancelButtonClick);
			this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok-button", this.onOkButtonClick);

			var me = this;

			var handleClick = function(e) {

				m = YAHOO.util.Dom.getStyle(this, 'backgroundColor').match(/^rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/i);
				if (m) {

					function hexstr(number) {
						var chars = new Array("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
						var low = number & 0xf;
						var high = (number >> 4) & 0xf;
						return "" + chars[high] + chars[low];
					}

					function rgb2hex(r, g, b) {
						return "#" + hexstr(r) + hexstr(g) + hexstr(b);
					}

					rgb = rgb2hex(m[1], m[2], m[3]);

					YAHOO.util.Dom.get(me.id + "-hexValue").value = rgb;
					YAHOO.util.Dom.setStyle(me.id + "-hexValue", 'backgroundColor', rgb);
				}
			};
			var nodes = YAHOO.util.Selector.query('div.jfk-palette-colorswatch');
			YAHOO.util.Event.on(nodes, 'click', handleClick);

			this.updateConfig();

			// Show panel
			this._showPanel();

		},

		updateConfig : function() {

			var color = "#FFFFF";
			if (this.showConfig.fieldId) {
				color = YAHOO.util.Dom.get(this.showConfig.fieldId).value;
			} else if (this.showConfig.selectedColor) {
				color = this.showConfig.selectedColor;
			}
			YAHOO.util.Dom.get(this.id + "-hexValue").value = color;
			YAHOO.util.Dom.setStyle(this.id + "-hexValue", 'backgroundColor', color);
		},

		/**
		 * Fired when the user clicks the cancel button. Closes the panel.
		 * 
		 * @method onCancelButtonClick
		 * @param event
		 *            {object} a Button "click" event
		 */
		onOkButtonClick : function HPV_onOkButtonClick() {

			if (this.showConfig.fieldId) {
				YAHOO.util.Dom.get(this.showConfig.fieldId).value = YAHOO.util.Dom.get(this.id + "-hexValue").value;
			} else if (this.showConfig.nodeRefs) {
				var me = this;
				Alfresco.util.Ajax.request({
					method : Alfresco.util.Ajax.POST,
					url : Alfresco.constants.PROXY_URI + "becpg/bulkedit/type/" + this.showConfig.itemType.replace(":", "_")
										+ "/bulksave?nodeRefs=" + this.showConfig.nodeRefs.join(),
					dataObj : {"prop_bcpg_color": YAHOO.util.Dom.get(this.id + "-hexValue").value},
					requestContentType: Alfresco.util.Ajax.JSON,
					successCallback : {
						fn : function(resp) {
							if (resp.json) {
								YAHOO.Bubbling.fire( "refreshDataGrids");
							}
						},
						scope : this
					},
					failureMessage : "Cannot update color",
				});
				
			}

			this.onCancelButtonClick();

		},

		onCancelButtonClick : function HPV_onCancelButtonClick() {

			// Hide the panel
			this.widgets.panel.hide();

			// Disable the Esc key listener
			this.widgets.escapeListener.disable();

		},

		/**
		 * Prepares the gui and shows the panel.
		 * 
		 * @method _showPanel
		 * @private
		 */
		_showPanel : function HPV__showPanel() {

			// Enable the Esc key listener
			this.widgets.escapeListener.enable();

			// Show the panel
			this.widgets.panel.show();
		}
	};
})();

beCPG.module.getColorPickerInstance = function() {
	var instanceId = "becpg-color-picker-instance";
	return Alfresco.util.ComponentManager.get(instanceId) || new beCPG.module.ColorPicker(instanceId);
}
