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
   var Dom = YAHOO.util.Dom, KeyListener = YAHOO.util.KeyListener;

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
         this.widgets.panel = Alfresco.util.createYUIPanel(dialogDiv);

         // Update
         this.update();

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

         var instance = this;

         this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
               {
                  dataSource : {
                     url : Alfresco.constants.PROXY_URI + "becpg/api/entity-version?mode=graph&nodeRef=" + this.options.nodeRef,
                     doBeforeParseData : function(oRequest, oFullResponse) {

                        instance.graphData = oFullResponse;

                        return ({
                           "data" : oFullResponse
                        });
                     }
                  },
                  dataTable : {
                     container : this.id + "-graphContent",
                     columnDefinitions : [ {
                        key : "version",
                        sortable : false,
                        formatter : this.renderCellVersion
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
         var html = "", doc = oRecord.getData(), current = (beCPG.module.getVersionsGraphInstance().options.nodeRef == doc.entityNodeRef);

         html += '<div id="graph-version-row-' + this.getRecordIndex(oRecord) + '" class="entity-branches">';
         html += '   <span class="document-version">' + $html(doc.label) + '</span>';
         html += '   <span class="' + doc.metadata + (current ? " current" : "") + '" ><a href="' + beCPG.util
               .entityCharactURL(doc.siteId, doc.nodeRef, doc.itemType) + '">' + $html(doc.name) + '</a></span>';
         html += '<div class="version-details">';
         html += ((doc.description || "").length > 0) ? $html(doc.description, true)
               : '<span class="faded">(' + Alfresco.util.message("label.noComment", beCPG.module
                     .getVersionsGraphInstance().name) + ')</span>';
         html += '</div>';
         html += '</div>';

         elCell.innerHTML = html;
      },

      renderCellAuthor : function VersionsGraph_renderCellDetails(elCell, oRecord, oColumn, oData) {
         var html = "", doc = oRecord.getData();

         var uri = Alfresco.util.uriTemplate("userprofilepage", {
            userid : doc.creator.userName
         });

         html += '<a href="' + uri + '" title="' + $html(doc.creator.firstName + ' ' + doc.creator.lastName) + '">' + $userAvatar(
               doc.creator.userName, 32) + '</a>';

         elCell.innerHTML = html;
      },

      renderCellDate : function VersionsGraph_renderCellDate(elCell, oRecord, oColumn, oData) {
         var doc = oRecord.getData();

         elCell.innerHTML = Alfresco.util.relativeTime(Alfresco.util.fromISO8601(doc.createdDateISO));
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

            var nextY = Dom.getY("graph-version-row-" + (idx - 1)) - yOffset;
            var rowY = Dom.getY("graph-version-row-" + (idx)) - yOffset;

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
                  var prevY = Dom.getY("graph-version-row-" + (prec[precNodeRef])) - yOffset - this.dot_radius;
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
