/**
 * Entity Version component.
 * 
 * @namespace beCPG
 * @class beCPG.module.VersionsGraph
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, KeyListener = YAHOO.util.KeyListener;

   var DOWNLOAD_EVENTCLASS = Alfresco.util.generateDomId(null, "download");
   var ALL_STATES = "allStates", MAX_LISTED_STATES = 2;
   var REVERT_EVENTCLASS = Alfresco.util.generateDomId(null, "revert");
   
   /**
    * Preferences
    */
   var PREFERENCES_VERSIONS_GRAPH = "org.alfresco.share.versions.graph",
       PREFERENCES_VERSIONS_GRAPH_VIEW = PREFERENCES_VERSIONS_GRAPH + ".simpleView",
       PREFERENCES_VERSIONS_GRAPH_HIDDEN_STATES = PREFERENCES_VERSIONS_GRAPH + ".hiddenStates";
   
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $userAvatar = Alfresco.Share.userAvatar;

   beCPG.module.VersionsGraph = function(containerId) {
      this.name = "beCPG.module.VersionsGraph";
      this.id = containerId;

      var instance = Alfresco.util.ComponentManager.get(this.id);
      if (instance !== null) {
         throw new Error("An instance of beCPG.module.VersionsGraph already exists.");
      }

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      // Load YUI Components
      Alfresco.util.YUILoaderHelper.require([ "datasource", "datatable", "button", "container", "menu" ],
            this.onComponentsLoaded, this);

      // Initialise preferences service
      this.services = this.services || {};
      this.services.preferences = new Alfresco.service.Preferences();

      this.hiddenStates = [];

      return this;

   };

   beCPG.module.VersionsGraph.prototype = {

      /**
       * Object container for initialization options
       * 
       * @property options
       * @type {object} object literal
       */
      options : {
         /**
          * Reference to the current document
          * 
          * @property nodeRef
          * @type string
          */
         nodeRef : null,
         
         /**
          * Item type of the entity
          * 
          * @property itemType
          * @type string
          */
         itemType : null,
         
         /**
          * Types configuration from XML
          * 
          * @property types
          * @type array
          */
         types : [],
         
         /**
          * States configuration from XML
          * 
          * @property states
          * @type array
          */
         states : []

      },

      graphData : [],

      /**
       * States unchecked in the state filter. Hidden states are stored rather than displayed ones so that a
       * newly configured state is displayed by default.
       * 
       * @property hiddenStates
       * @type array
       */
      hiddenStates : [],

      colors : [ [ 1.0, 0.0, 0.0 ], [ 1.0, 1.0, 0.0 ], [ 0.0, 1.0, 0.0 ], [ 0.0, 1.0, 1.0 ], [ 0.0, 0.0, 1.0 ],
            [ 1.0, 0.0, 1.0 ], [ 1.0, 1.0, 0.0 ], [ 0.0, 0.0, 0.0 ] ],

      line_width : 2.0,
      dot_radius : 5,
      widgets : {},

      /**
       * Fired by YUILoaderHelper when required component script files have been loaded into the browser.
       * 
       * @method onComponentsLoaded
       */
      onComponentsLoaded : function VersionsGraph_onComponentsLoaded() {
         // Shortcut for dummy instance
         if (this.id === null) {
            return;
         }
      },

      /**
       * Show can be called multiple times and will display the dialog in different ways depending on the config
       * parameter.
       * 
       * @method show
       * @param config
       *            {object} describes how the dialog should be displayed The config object is in the form of: {
       *            nodeRef: {string}, // the nodeRef version: {string} // the version to show properties of }
       */
      show : function VersionsGraph_show(config) {
         // Merge the supplied config with default config and check mandatory properties
         this.options = YAHOO.lang.merge(this.options, config);
         if (this.options.nodeRef === undefined) {
            throw new Error("A nodeRef, must be provided");
         }
         
         // Check if the dialog has been showed before
         if (this.widgets.panel) {
            // It'll need updating, probably.
            this.update();

            // The displaying.
            this._showPanel();
         } else {
            // If it hasn't load the gui (template) from the server
            Alfresco.util.Ajax.request({
               url : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-version/versions-graph?htmlid=" + this.id,
               successCallback : {
                  fn : this.onTemplateLoaded,
                  scope : this
               },
               failureMessage : "Could not load html template for version graph",
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
       * Called when the dialog html template has been returned from the server. Creates the YIU gui objects such as the
       * panel.
       * 
       * @method onTemplateLoaded
       * @param response
       *            {object} a Alfresco.util.Ajax.request response object
       */
      onTemplateLoaded : function VersionsGraph_onTemplateLoaded(response) {

         // Inject the template from the XHR request into a new DIV element
         var containerDiv = document.createElement("div");
         containerDiv.innerHTML = response.serverResponse.responseText;

         var dialogDiv = YAHOO.util.Dom.getFirstChild(containerDiv);

         // Create the panel from the HTML returned in the server reponse
         this.widgets.panel = Alfresco.util.createYUIPanel(dialogDiv, {
				draggable : true,
				width : "90em"
			});

         // Load types and states from global config
         if (beCPG.module.VersionsGraph.CONFIG) {
            this.options.types = beCPG.module.VersionsGraph.CONFIG.types || [];
            this.options.states = beCPG.module.VersionsGraph.CONFIG.states || [];
         }

         // Initialize filter controls
         this.widgets.filterName = Dom.get("versions-graph-filter-name");
         this.widgets.stateFilter = Alfresco.util.createYUIButton(this, "stateFilter-button", this.onStateFilterClick, {
            type : "menu",
            menu : [],
            lazyloadmenu : false
         });
         Dom.addClass(this.widgets.stateFilter.getMenu().element, "versions-graph-state-menu");
         this._keepStateFilterOpen();

         // Populate state filter based on entity type
         this._populateStateFilter();

         // Detailed/Simple List button group
         this.widgets.simpleDetailed = new YAHOO.widget.ButtonGroup("versions-graph-simpleDetailed");
         if (this.widgets.simpleDetailed !== null) {
            this.widgets.simpleDetailed.on("checkedButtonChange", this.onSimpleDetailed, this.widgets.simpleDetailed, this);
         }

         // Store current view mode (false = detailed, true = simple)
         this.simpleView = false;

         // Load preferences and apply them
         var me = this;
         this.services.preferences.request(PREFERENCES_VERSIONS_GRAPH, {
            successCallback: {
               fn: function(p_oResponse) {
                  var prefs = Alfresco.util.dotNotationToObject(p_oResponse.json);
                  if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && 
                      prefs.org.alfresco.share.versions && prefs.org.alfresco.share.versions.graph) {
                     var graphPrefs = prefs.org.alfresco.share.versions.graph;
                     
                     // Apply simple view preference
                     if (graphPrefs.simpleView === true || graphPrefs.simpleView === "true") {
                        me.simpleView = true;
                        if (me.widgets.simpleDetailed !== null) {
                           me.widgets.simpleDetailed.check(0);
                        }
                     }
                     
                     // Apply state filter preference
                     if (graphPrefs.hiddenStates) {
                        me.hiddenStates = graphPrefs.hiddenStates.split(",");
                        me._populateStateFilter();
                     }
                  }
                  
                  // Initial update with preferences applied
                  me.update();
               },
               scope: this
            },
            failureCallback: {
               fn: function() {
                  // Default update if preferences fail
                  me.update();
               },
               scope: this
            }
         });

         // Name filter - keyup with debounce
         var typingTimer;
         var doneTypingInterval = 500;
         Event.on(this.widgets.filterName, "keyup", function() {
            clearTimeout(typingTimer);
            typingTimer = setTimeout(function() {
               me.update();
            }, doneTypingInterval);
         });

         Event.on(this.widgets.filterName, "keydown", function() {
            clearTimeout(typingTimer);
         });

          YAHOO.Bubbling.addDefaultAction(DOWNLOAD_EVENTCLASS, function (layer, args) {
          	
         	 var anchor = args[1].anchor,
              name = anchor.getAttribute("name") || anchor.name,
              rel = anchor.getAttribute("rel"),
              downloadDialog = Alfresco.getArchiveAndDownloadInstance(), config = {
                  nodesToArchive : [ {
                     "nodeRef" : rel
                  } ],
                  archiveName : name
               };
         	 
              downloadDialog.show(config);
               
               return true;
           });

		  YAHOO.Bubbling.addDefaultAction(REVERT_EVENTCLASS, function() {
			  var revertUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/revert/' + this.rel.replace(":/", "");
			  document.body.classList.add("loading-cursor");
				
			  Alfresco.util.Ajax
				  .request({
					  method: Alfresco.util.Ajax.GET,
					  url: revertUrl,
					  responseContentType: Alfresco.util.Ajax.JSON,
					  successCallback: {
						  fn: function(resp) {
							  if (resp.json) {
								  window.location.href = beCPG.util.entityURL(resp.json.siteId,
									  resp.json.persistedObject, resp.json.type);
							  }
							document.body.classList.remove("loading-cursor");
						  },
						  scope: this
					  },
					  failureCallback: {
						  fn: function(response) {
							  if (response.json && response.json.message) {
								  Alfresco.util.PopupManager.displayPrompt({
									  text: response.json.message
								  });
							  }
							document.body.classList.remove("loading-cursor");
						  },
						  scope: this
					  }

				  });
		  });
         
         // Show panel
         this._showPanel();

      },

      /**
       * Fired when the user clicks the cancel button. Closes the panel.
       * 
       * @method onCancelButtonClick
       * @param event
       *            {object} a Button "click" event
       */
      onCancelButtonClick : function VersionsGraph_onCancelButtonClick() {
         // Hide the panel
         this.widgets.panel.hide();

         // Disable the Esc key listener
         this.widgets.escapeListener.disable();

      },

      /**
       * Fired by YUI when parent element is available for scripting
       * 
       * @method onReady
       */
      update : function VersionsGraph_update() {

         var instance = this, name = this.widgets.filterName.value, hiddenStates = this.hiddenStates;

         this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
               {
                  dataSource : {
                     url : Alfresco.constants.PROXY_URI + "becpg/api/entity-version?mode=graph&nodeRef=" + this.options.nodeRef,
                     doBeforeParseData : function(oRequest, oFullResponse) {

                        // Update itemType from response if not set and repopulate filter
                        if (oFullResponse.length > 0 && oFullResponse[0].itemType) {
                           var newItemType = oFullResponse[0].itemType;
                           if (instance.options.itemType !== newItemType) {
                              instance.options.itemType = newItemType;
                              instance._populateStateFilter();
                           }
                        }

                        // Filter by name and entity state
                        var filteredResponse = oFullResponse.filter(function(entity) {
                           var nameMatch = (name || "") === "" || entity.name.toLowerCase().indexOf(name.toLowerCase()) !== -1;
                           var stateMatch = hiddenStates.indexOf(entity.entityState) === -1;
                           return nameMatch && stateMatch;
                        });

                        instance._linkToDisplayedAncestors(oFullResponse, filteredResponse);

                        instance.graphData = filteredResponse;

                        return ({
                           "data" : filteredResponse
                        });
                     }
                  },
                  dataTable : {
                     container : this.id + "-graphContent",
                     columnDefinitions : [ {
                        key : "version",
                        sortable : false,
                        formatter : this.renderCellVersion,
                        width : "70%"
                     }, {
                        key : "date",
                        sortable : false,
                        formatter : this.renderCellDate
                     }, {
                        key : "author",
                        sortable : false,
                        formatter : this.renderCellAuthor
                     }

                     ],
                     config : {
                        MSG_EMPTY : Alfresco.util.message("message.noVersions", this.name)
                     }
                  }
               });

         this.widgets.alfrescoDataTable.getDataTable().subscribe("postRenderEvent", function() {

            instance.widgets.canvas = Dom.get(instance.id + "-graphCanvas");
            
            if (window.G_vmlCanvasManager) {
               instance.widgets.canvas = window.G_vmlCanvasManager.initElement(instance.widgets.canvas);
            }
            instance.ctx = instance.widgets.canvas.getContext('2d');

            instance.ctx.clearRect(0, 0, instance.widgets.canvas.width, instance.widgets.canvas.height);
            instance.ctx.strokeStyle = 'rgb(0, 0, 0)';
            instance.ctx.fillStyle = 'rgb(0, 0, 0)';

            instance.renderGraph(100);

         });

      },

      /**
       * Version renderer
       * 
       * @method renderCellVersion
       */
      renderCellVersion : function VersionsGraph_renderCellVersions(elCell, oRecord, oColumn, oData) {
         var html = "", doc = oRecord.getData(), current = (beCPG.module.getVersionsGraphInstance().options.nodeRef == doc.entityNodeRef),
          compareURL = null;
         
         if(!current || doc.label != beCPG.module.getVersionsGraphInstance().options.label){
	         if(current ){
	        	 compareURL = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + beCPG.module.getVersionsGraphInstance().options.nodeRef
	             .replace(":/", "") + '/' + encodeURIComponent(doc.label) + '/' + encodeURIComponent(doc.name) + "?noCache=" + new Date().getTime();
	         } else {
	        	 compareURL = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + beCPG.module.getVersionsGraphInstance().options.nodeRef
	        	 .replace(":/", "") + "/compare?entities="+doc.nodeRef + "&noCache=" + new Date().getTime();
	         }
			
         }
         

         html += '<div id="graph-version-row-' + this.getRecordIndex(oRecord) + '" class="entity-branches">';
         html += '   <span class="document-version">' + $html(doc.label) + '</span>';
         html += '   <span class="' + doc.metadata + (current ? " current" : "") + '" >';
		 
		if (current || !doc.clickableNode) {
			html +=  $html(doc.name);
		} else {
			html += '<a href="' + beCPG.util.entityURL(null,doc.clickableNode) + '&bcPath=true">'+$html(doc.name)+'</a>';
		}
		
		if(!doc.clickableNode){
      	       html += '   <a href="#" class="'+ REVERT_EVENTCLASS+' revert" title="' + Alfresco.util.message("label.revert") + '" rel="' + doc.nodeRef + '">&nbsp;</a>';
         }

         if(compareURL!=null){
             html += '   <a href="' + compareURL + '" class="compare" title="' + Alfresco.util.message("label.compare") + '">&nbsp;</a>';
         }
         if(current){
	         html += '   <a href="#" name="'+$html(doc.name)+'" rel="' + doc.nodeRef + '" class="'+DOWNLOAD_EVENTCLASS+' download" title="'
	           + Alfresco.util.message("label.download") + '">&nbsp;</a>';
         }

         html += '</span>';
         
         // Only show details in detailed view (not simple view)
         if (!beCPG.module.getVersionsGraphInstance().simpleView) {
            html += '<div class="version-details">';
            
            // Display product description (cm:description)
            if ((doc.description || "").length > 0) {
               html += '<div class="version-description">' + $html(doc.description, true) + '</div>';
            }
            
            // Display version comment (version description from merge)
            if ((doc.versionDescription || "").length > 0) {
               html += '<div class="version-comment"><em>' + $html(doc.versionDescription, true) + '</em></div>';
            }
            
            // Show "no comment" only if both are empty
            if ((doc.description || "").length === 0 && (doc.versionDescription || "").length === 0) {
               html += '<span class="faded">(' + Alfresco.util.message("label.noComment", beCPG.module
                     .getVersionsGraphInstance().name) + ')</span>';
            }
            
            html += '</div>';
         }
         html += '</div>';
    

         elCell.innerHTML = html;
      },

      renderCellAuthor : function VersionsGraph_renderCellDetails(elCell, oRecord, oColumn, oData) {
         var html = "", doc = oRecord.getData();

         var uri = Alfresco.util.uriTemplate("userprofilepage", {
            userid : doc.creator.userName
         });

		html += '<a href="' + uri + '" title="' + $html(doc.creator.firstName + ' ' + doc.creator.lastName) + '" style="display: flex; align-items: center; text-decoration: none;">'
          + $userAvatar(doc.creator.userName, 32)
          + '<span style="white-space: nowrap; margin-left: 5px;">' + doc.creator.firstName + ' ' + doc.creator.lastName + '</span>'
          + '</a>';

         elCell.innerHTML = html;
      },

      renderCellDate : function VersionsGraph_renderCellDate(elCell, oRecord, oColumn, oData) {
         var doc = oRecord.getData();

         elCell.innerHTML = Alfresco.util.relativeTime(Alfresco.util.fromISO8601(doc.createdDateISO))+'<span> ('+ Alfresco.util.formatDate(doc.createdDateISO, Alfresco.util.message("datetime.format")) + ')</span>';
         
      },

      setColor : function VersionsGraph_setColor(color, bg, fg) {
         var vColor = color % this.colors.length;

         var red = (this.colors[vColor][0] * fg) || bg;
         var green = (this.colors[vColor][1] * fg) || bg;
         var blue = (this.colors[vColor][2] * fg) || bg;
         red = Math.round(red * 255);
         green = Math.round(green * 255);
         blue = Math.round(blue * 255);
         var s = 'rgb(' + red + ', ' + green + ', ' + blue + ')';
         this.ctx.strokeStyle = s;
         this.ctx.fillStyle = s;
      },

      renderGraph : function VersionsGraph_renderGraph(canvasWidth) {


         this.graphData.reverse();
         
         var nbCols = 0, columnsPos = {}, shouldBranch = false, columns = {};
         for ( var i in this.graphData) {
            var entityNodeRef = this.graphData[i].entityNodeRef, entityFromBranch = this.graphData[i].displayedBranchFrom;
            
            var test = columnsPos[entityNodeRef];
            if (!test && test != 0) {
               //reorder cols
               if(entityFromBranch){
                  for(var j in columnsPos){
                     if(columnsPos[j] > columnsPos[entityFromBranch]){
                        columnsPos[j] = columnsPos[j] + 1; 
                     }
                  }
                  columnsPos[this.graphData[i].entityNodeRef] = columnsPos[entityFromBranch]+1;
               } else {
                  columnsPos[this.graphData[i].entityNodeRef] = nbCols;
               }
               nbCols++;
            }

         }

         var edge_pad = this.dot_radius + 2;
         var box_size = Math.min(18, Math.floor((canvasWidth - edge_pad * 2) / (nbCols)));
         var base_x = canvasWidth - edge_pad - 10;

         var prec = {};
         nbCols = 0;
         
         var idx = this.graphData.length - 1;


         var yOffset = Dom.getY(this.id + "-graphNodes");

         for ( var i in this.graphData) {
            var rowY = this._getRowAnchorY(idx, yOffset, 0);
            // The newest version has no next row: the line stops on its dot
            var nextY = this._getRowAnchorY(idx - 1, yOffset, rowY);

            var entityNodeRef = this.graphData[i].entityNodeRef, entityFromBranch = this.graphData[i].displayedBranchFrom;

            var test = columns[entityNodeRef];
            if (!test && test != 0) {
               columns[entityNodeRef] = nbCols++;
               shouldBranch = true;
            }

            for ( var j in columns) {
               var end = columnsPos[j], precNodeRef = (shouldBranch && j == entityNodeRef && entityFromBranch != null) ? entityFromBranch
                     : j, start = columnsPos[precNodeRef];

               this.setColor(end, 0.0, 0.65);

               x = base_x - box_size * start;

               this.ctx.lineWidth = this.line_width;
               this.ctx.beginPath();

               if (start != end) {
                  var prevY = this._getRowAnchorY(prec[precNodeRef], yOffset, this.dot_radius) - this.dot_radius;
                  this.ctx.moveTo(x, prevY);
                  var x2 = base_x - box_size * end;
                  var ymid = (rowY + prevY) / 2;
                  this.ctx.bezierCurveTo(x, ymid, x2, ymid, x2, rowY);
                  this.ctx.moveTo(x2, rowY);
                  this.ctx.lineTo(x2, nextY, 3);
               } else {
                  this.ctx.moveTo(x, rowY);
                  this.ctx.lineTo(x, nextY, 3);
               }

               this.ctx.stroke();

            }

            shouldBranch = false;

            radius = this.dot_radius;

            x = base_x - box_size * columnsPos[entityNodeRef];

            prec[entityNodeRef] = idx;

            this.ctx.beginPath();
            this.setColor(columnsPos[entityNodeRef], 0.25, 0.75);
            this.ctx.arc(x, rowY, radius, 0, Math.PI * 2, true);
            this.ctx.fill();
            idx--;
         }

      },

      /**
       * Returns the canvas ordinate of a version row, at the vertical center of its version badge
       * so that the dot lines up with the version number rather than with the whole row.
       * 
       * @method _getRowAnchorY
       * @param rowIndex {int} Index of the row in the datatable
       * @param canvasY {int} Page ordinate of the top of the canvas
       * @param defaultY {int} Ordinate returned when the row does not exist
       * @return {int} The ordinate of the row in the canvas
       * @private
       */
      _getRowAnchorY : function VersionsGraph__getRowAnchorY(rowIndex, canvasY, defaultY) {
         var rowElement = Dom.get("graph-version-row-" + rowIndex);
         if (!rowElement) {
            return defaultY;
         }
         var anchor = Dom.getElementsByClassName("document-version", "span", rowElement)[0] || rowElement;
         return Dom.getY(anchor) - canvasY + anchor.offsetHeight / 2;
      },

      /**
       * Keeps the state filter menu open when an item is clicked, so that several states can be checked in a row.
       * The menu "keepopen" option is not enough: the menu button hides its menu on every item click.
       * 
       * @method _keepStateFilterOpen
       * @private
       */
      _keepStateFilterOpen : function VersionsGraph__keepStateFilterOpen() {
         var button = this.widgets.stateFilter, menu = button.getMenu();
         menu.cfg.setProperty("keepopen", true);
         menu.unsubscribe("click", button._onMenuClick, button);
      },

      /**
       * Fills the state filter menu with the states allowed for the entity type: an "all" item, then one
       * checkable item per state, checked unless the state is hidden.
       * 
       * @method _populateStateFilter
       * @private
       */
      _populateStateFilter : function VersionsGraph__populateStateFilter() {
         var menu = this.widgets.stateFilter.getMenu(), allowedStates = this._getAllowedStates(), stateItems = [];
         for (var i = 0; i < allowedStates.length; i++) {
            stateItems.push({
               text : this._renderStateItemText(this._getStateLabel(allowedStates[i]),
                     this.hiddenStates.indexOf(allowedStates[i]) === -1),
               value : allowedStates[i]
            });
         }
         menu.clearContent();
         menu.addItems([ {
            text : this._renderStateItemText(Alfresco.util.message("label.all-states", this.name), false),
            value : ALL_STATES
         } ], 0);
         menu.addItems(stateItems, 1);
         menu.render();
         this._refreshStateFilter();
      },

      /**
       * Renders the text of a state menu item: a checkbox followed by the label, with the form field markup so that
       * the theme draws the checkbox as in forms. The checkbox only shows the state, the click is handled by the
       * menu item.
       * 
       * @method _renderStateItemText
       * @param label {string} The item label
       * @param checked {boolean} Whether the checkbox is checked
       * @return {string} The item text as HTML
       * @private
       */
      _renderStateItemText : function VersionsGraph__renderStateItemText(label, checked) {
         return '<span class="form-field"><input type="checkbox" tabindex="-1"' + (checked ? ' checked="checked"' : '')
               + ' /><label>' + $html(label) + '</label></span>';
      },

      /**
       * Returns the checkbox of a state menu item.
       * 
       * @method _getItemCheckbox
       * @param menuItem {YAHOO.widget.MenuItem} The menu item
       * @return {HTMLElement} The checkbox
       * @private
       */
      _getItemCheckbox : function VersionsGraph__getItemCheckbox(menuItem) {
         return menuItem.element.getElementsByTagName("input")[0];
      },

      /**
       * Returns the states configured for the entity type, or the default ones when the type is not configured.
       * 
       * @method _getAllowedStates
       * @return {array} The state values
       * @private
       */
      _getAllowedStates : function VersionsGraph__getAllowedStates() {
         var types = this.options.types || [], typeStates = {};
         for (var i = 0; i < types.length; i++) {
            typeStates[types[i].name] = types[i].states;
         }
         var states = typeStates[this.options.itemType] || typeStates["default"] || "", allowedStates = [];
         var values = states.split(",");
         for (var j = 0; j < values.length; j++) {
            if (values[j].trim().length > 0) {
               allowedStates.push(values[j].trim());
            }
         }
         return allowedStates;
      },

      /**
       * Returns the translated label of a state.
       * 
       * @method _getStateLabel
       * @param stateValue {string} The state value
       * @return {string} The label configured for the state, or the state value itself
       * @private
       */
      _getStateLabel : function VersionsGraph__getStateLabel(stateValue) {
         var states = this.options.states || [];
         for (var i = 0; i < states.length; i++) {
            if (states[i].value === stateValue) {
               return Alfresco.util.message(states[i].label, this.name) || stateValue;
            }
         }
         return stateValue;
      },

      /**
       * Checks the "all" item when every state is displayed, and sets the state filter button label: "all", the
       * displayed states when there are few of them, otherwise their count.
       * 
       * @method _refreshStateFilter
       * @private
       */
      _refreshStateFilter : function VersionsGraph__refreshStateFilter() {
         var itemGroups = this.widgets.stateFilter.getMenu().getItemGroups(), stateItems = itemGroups[1] || [],
               displayedLabels = [], label;
         for (var i = 0; i < stateItems.length; i++) {
            if (this._getItemCheckbox(stateItems[i]).checked) {
               displayedLabels.push(this._getStateLabel(stateItems[i].value));
            }
         }
         this._getItemCheckbox(itemGroups[0][0]).checked = displayedLabels.length === stateItems.length;
         if (displayedLabels.length === stateItems.length) {
            label = Alfresco.util.message("label.all-states", this.name);
         } else if (displayedLabels.length > MAX_LISTED_STATES) {
            label = Alfresco.util.message("label.displayed-states", this.name, displayedLabels.length);
         } else {
            label = displayedLabels.length > 0 ? displayedLabels.join(", ") : Alfresco.util.message("label.no-state", this.name);
         }
         this.widgets.stateFilter.set("label", $html(label) + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
      },

      /**
       * Sets on each displayed version the closest displayed entity it branches from, so that a branch whose
       * parent is filtered out stays attached to the graph instead of being drawn without dot nor line.
       * 
       * @method _linkToDisplayedAncestors
       * @param allVersions {array} All the versions returned by the server
       * @param displayedVersions {array} The versions kept by the filters
       * @private
       */
      _linkToDisplayedAncestors : function VersionsGraph__linkToDisplayedAncestors(allVersions, displayedVersions) {
         var branchParents = {}, displayedEntities = {}, i;
         for (i = 0; i < allVersions.length; i++) {
            if (allVersions[i].entityFromBranch && allVersions[i].entityFromBranch !== allVersions[i].entityNodeRef) {
               branchParents[allVersions[i].entityNodeRef] = allVersions[i].entityFromBranch;
            }
         }
         for (i = 0; i < displayedVersions.length; i++) {
            displayedEntities[displayedVersions[i].entityNodeRef] = true;
         }
         for (i = 0; i < displayedVersions.length; i++) {
            displayedVersions[i].displayedBranchFrom = this._findDisplayedAncestor(displayedVersions[i].entityFromBranch,
                  branchParents, displayedEntities);
         }
      },

      /**
       * Walks up the branch hierarchy from an entity until an entity kept by the filters is found.
       * 
       * @method _findDisplayedAncestor
       * @param entityNodeRef {string} The entity to start from
       * @param branchParents {object} The entity each entity branches from, by entity nodeRef
       * @param displayedEntities {object} The entities kept by the filters, by entity nodeRef
       * @return {string} The closest displayed ancestor, or null when every ancestor is filtered out
       * @private
       */
      _findDisplayedAncestor : function VersionsGraph__findDisplayedAncestor(entityNodeRef, branchParents, displayedEntities) {
         var visited = {};
         while (entityNodeRef && !displayedEntities[entityNodeRef] && !visited[entityNodeRef]) {
            visited[entityNodeRef] = true;
            entityNodeRef = branchParents[entityNodeRef];
         }
         return entityNodeRef && displayedEntities[entityNodeRef] ? entityNodeRef : null;
      },

      /**
       * Show/Hide detailed list buttongroup click handler
       * 
       * @method onSimpleDetailed
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onSimpleDetailed : function VersionsGraph_onSimpleDetailed(e, p_obj) {
         this.simpleView = e.newValue.index === 0;
         this.services.preferences.set(PREFERENCES_VERSIONS_GRAPH_VIEW, this.simpleView);
         if (e) {
            Event.preventDefault(e);
         }
         this.update();
      },

      /**
       * State filter menu click handler: shows or hides the clicked state, or every state for the "all" item.
       * 
       * @method onStateFilterClick
       * @param p_sType {string} The event type
       * @param p_aArgs {array} The event arguments, the clicked menu item at index 1
       */
      onStateFilterClick : function VersionsGraph_onStateFilterClick(p_sType, p_aArgs) {
         var menuItem = p_aArgs[1];
         if (!menuItem) {
            return;
         }
         var displayed = !this._getItemCheckbox(menuItem).checked;
         if (menuItem.value === ALL_STATES) {
            this._setAllStatesDisplayed(displayed);
         } else {
            this._setStateDisplayed(menuItem, displayed);
         }
         this._refreshStateFilter();
         this._saveHiddenStates();
      },

      /**
       * Checks or unchecks every state menu item.
       * 
       * @method _setAllStatesDisplayed
       * @param displayed {boolean} Whether every state is displayed or hidden
       * @private
       */
      _setAllStatesDisplayed : function VersionsGraph__setAllStatesDisplayed(displayed) {
         var stateItems = this.widgets.stateFilter.getMenu().getItemGroups()[1] || [];
         for (var i = 0; i < stateItems.length; i++) {
            this._setStateDisplayed(stateItems[i], displayed);
         }
      },

      /**
       * Checks or unchecks a state menu item and updates the hidden states accordingly.
       * 
       * @method _setStateDisplayed
       * @param menuItem {YAHOO.widget.MenuItem} The state menu item
       * @param displayed {boolean} Whether the state is displayed or hidden
       * @private
       */
      _setStateDisplayed : function VersionsGraph__setStateDisplayed(menuItem, displayed) {
         var index = this.hiddenStates.indexOf(menuItem.value);
         this._getItemCheckbox(menuItem).checked = displayed;
         if (displayed && index !== -1) {
            this.hiddenStates.splice(index, 1);
         } else if (!displayed && index === -1) {
            this.hiddenStates.push(menuItem.value);
         }
      },

      /**
       * Stores the hidden states in the user preferences and refreshes the graph.
       * 
       * @method _saveHiddenStates
       * @private
       */
      _saveHiddenStates : function VersionsGraph__saveHiddenStates() {
         this.services.preferences.set(PREFERENCES_VERSIONS_GRAPH_HIDDEN_STATES, this.hiddenStates.join(","));
         this.update();
      },

      /**
       * Prepares the gui and shows the panel.
       * 
       * @method _showPanel
       * @private
       */
      _showPanel : function VersionsGraph__showPanel() {

         // Enable the Esc key listener
         this.widgets.escapeListener.enable();

         // Show the panel
         this.widgets.panel.show();
      }
   };
})();

beCPG.module.getVersionsGraphInstance = function() {
   var instanceId = "becpg-versionsGraph-instance";
   return Alfresco.util.ComponentManager.get(instanceId) || new beCPG.module.VersionsGraph(instanceId);
};
