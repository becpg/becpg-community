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
   var REVERT_EVENTCLASS = Alfresco.util.generateDomId(null, "revert");
   
   /**
    * Preferences
    */
   var PREFERENCES_VERSIONS_GRAPH = "org.alfresco.share.versions.graph",
       PREFERENCES_VERSIONS_GRAPH_VIEW = PREFERENCES_VERSIONS_GRAPH + ".simpleView",
       PREFERENCES_VERSIONS_GRAPH_STATE = PREFERENCES_VERSIONS_GRAPH + ".stateFilter";
   
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
      Alfresco.util.YUILoaderHelper.require([ "datasource", "datatable", "button", "container" ],
            this.onComponentsLoaded, this);

      // Initialise preferences service
      this.services = this.services || {};
      this.services.preferences = new Alfresco.service.Preferences();

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
         this.widgets.filterState = Dom.get("versions-graph-filter-system-state");

         // Populate state filter based on entity type
         this._populateStateFilter(this.widgets.filterState);

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
                     if (graphPrefs.stateFilter) {
                        me.widgets.filterState.value = graphPrefs.stateFilter;
                     }
                  }
                  
                  // Initial update with preferences applied
                  me.update(me.widgets.filterName.value, me.widgets.filterState.value, !me.simpleView);
               },
               scope: this
            },
            failureCallback: {
               fn: function() {
                  // Default update if preferences fail
                  me.update(me.widgets.filterName.value, me.widgets.filterState.value, !me.simpleView);
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
               me.update(me.widgets.filterName.value, me.widgets.filterState.value, !me.simpleView);
            }, doneTypingInterval);
         });

         Event.on(this.widgets.filterName, "keydown", function() {
            clearTimeout(typingTimer);
         });

         // State filter change
         Event.on(this.widgets.filterState, "change", this.onStateFilterChange, this, true);

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
       * @param name {string} Optional name filter
       * @param systemState {string} Optional system state filter
       * @param fullView {boolean} Optional full view mode
       */
      update : function VersionsGraph_update(name, systemState, fullView) {

         var instance = this;

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
                              instance._repopulateStateFilter();
                           }
                        }

                        // Filter by name and entity state
                        var filteredResponse = oFullResponse.filter(function(entity) {
                           var nameMatch = (name || "") === "" || entity.name.toLowerCase().indexOf(name.toLowerCase()) !== -1;
                           var stateMatch = (systemState || "") === "" || entity.entityState === systemState;
                           return nameMatch && stateMatch;
                        });

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
            var entityNodeRef = this.graphData[i].entityNodeRef, entityFromBranch = this.graphData[i].entityFromBranch;
            
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

         var prec = {}, extra = 15;
         nbCols = 0;
         
         var idx = this.graphData.length - 1;


         var yOffset = Dom.getY(this.id + "-graphNodes") - extra;

         for ( var i in this.graphData) {
            var rowElement = Dom.get("graph-version-row-" + idx);
            var nextRowElement = Dom.get("graph-version-row-" + (idx - 1));
            
            // Calculate Y position at the vertical center of the row
            var rowHeight = rowElement ? rowElement.offsetHeight : 20;
            var nextRowHeight = nextRowElement ? nextRowElement.offsetHeight : 20;
            
            var nextY = nextRowElement ? (Dom.getY(nextRowElement) - yOffset + nextRowHeight / 2) : 0;
            var rowY = rowElement ? (Dom.getY(rowElement) - yOffset + rowHeight / 2) : 0;

            var entityNodeRef = this.graphData[i].entityNodeRef, entityFromBranch = this.graphData[i].entityFromBranch;

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
                  var prevRowElement = Dom.get("graph-version-row-" + prec[precNodeRef]);
                  var prevRowHeight = prevRowElement ? prevRowElement.offsetHeight : 20;
                  var prevY = prevRowElement ? (Dom.getY(prevRowElement) - yOffset + prevRowHeight / 2 - this.dot_radius) : 0;
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
       * Repopulates the state filter dropdown when entity type changes.
       * 
       * @method _repopulateStateFilter
       * @private
       */
      _repopulateStateFilter : function VersionsGraph__repopulateStateFilter() {
         var selectElement = this.widgets.filterState;
         if (!selectElement) {
            return;
         }
         
         // Clear existing options except the first one (All states)
         while (selectElement.options.length > 1) {
            selectElement.remove(1);
         }
         
         // Repopulate with new states
         this._populateStateFilter(selectElement);
      },

      /**
       * Populates the state filter dropdown based on entity type configuration.
       * 
       * @method _populateStateFilter
       * @param selectElement {HTMLElement} The select element to populate
       * @private
       */
      _populateStateFilter : function VersionsGraph__populateStateFilter(selectElement) {
         var itemType = this.options.itemType;
         var types = this.options.types || [];
         var states = this.options.states || [];
         var allowedStates = [];
         
         // Find the type configuration for this entity type
         for (var i = 0; i < types.length; i++) {
            var typeConfig = types[i];
            if (itemType && itemType === typeConfig.name) {
               allowedStates = typeConfig.states ? typeConfig.states.split(",") : [];
               break;
            }
         }
         
         // If no specific type found, use default states
         if (allowedStates.length === 0) {
            for (var j = 0; j < types.length; j++) {
               if (types[j].name === "default") {
                  allowedStates = types[j].states ? types[j].states.split(",") : [];
                  break;
               }
            }
         }
         
         // Populate the select with allowed states
         for (var k = 0; k < allowedStates.length; k++) {
            var stateValue = allowedStates[k].trim();
            var stateLabel = stateValue;
            
            // Find the label for this state
            for (var l = 0; l < states.length; l++) {
               if (states[l].value === stateValue) {
                  stateLabel = Alfresco.util.message(states[l].label, this.name) || stateValue;
                  break;
               }
            }
            
            var option = document.createElement("option");
            option.value = stateValue;
            option.text = stateLabel;
            selectElement.appendChild(option);
         }
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
         this.update(this.widgets.filterName.value, this.widgets.filterState.value, !this.simpleView);
      },

      /**
       * State filter change handler
       * 
       * @method onStateFilterChange
       * @param e {object} DomEvent
       */
      onStateFilterChange : function VersionsGraph_onStateFilterChange(e) {
         this.services.preferences.set(PREFERENCES_VERSIONS_GRAPH_STATE, this.widgets.filterState.value);
         this.update(this.widgets.filterName.value, this.widgets.filterState.value, !this.simpleView);
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
