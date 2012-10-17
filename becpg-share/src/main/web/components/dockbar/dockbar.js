/**
 * Dashboard DockBar component.
 * 
 * @namespace beCPG
 * @class beCPG.component.DockBar
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;
	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $siteURL = Alfresco.util.siteURL, $isValueSet = Alfresco.util.isValueSet;

	var CHARACT_EVENTCLASS = Alfresco.util.generateDomId(null, "charact");

	/**
	 * Dashboard DockBar constructor.
	 * 
	 * @param {String}
	 *           htmlId The HTML id of the parent element
	 * @return {beCPG.component.DockBar} The new component instance
	 * @constructor
	 */
	beCPG.component.DockBar = function DockBar_constructor(htmlId, instanceName) {
		this.instanceName = instanceName;
		return beCPG.component.DockBar.superclass.constructor.call(this, "beCPG.component.DockBar", htmlId, [ "button",
		      "container", "datasource", "datatable", "animation" ]);
	};

	YAHOO
	      .extend(
	            beCPG.component.DockBar,
	            Alfresco.component.Base,
	            {

	               slide_tempo : 0,
	               slide_x : -177,
	               slide_ismoving : false,
	               slidefirst : true,
	               ns6 : (!document.all && document.getElementById),
	               ie4 : (document.all),
	               ns4 : (document.layers),
	               Float_Time : null,
	               topMargin : 0,
	               originalY : -1,
	               slideTime : 700,
	               getScrollTop : function() {
		               if (window.pageYOffset) {
			               return window.pageYOffset;
		               } else if (document.documentElement && document.documentElement.scrollTop) {
			               return document.documentElement.scrollTop;
		               } else if (document.body) {
			               return document.body.scrollTop;
		               }
		               return 0;
	               },
	               getClientWidth : function() {
		               if (window.innerWidth) {
			               return window.innerWidth;
		               } else if (document.documentElement && document.documentElement.clientWidth) {
			               return document.documentElement.clientWidth;
		               } else if (document.body) {
			               return document.body.clientWidth;
		               }
	               },
	               floatObject : function() {
		               if (this.ns4 || this.ns6) {
			               this.findHt = this.getClientWidth();
		               } else if (this.ie4) {
			               this.findHt = this.getClientWidth();
		               }
	               },
	               main : function() {
		               this.scrollTop = this.getScrollTop();
		               if (this.ns4) {
			               this.currentY = document.floatLayer.top;
			               if (this.originalY <= 0) {
				               this.originalY = this.getLayerY(floatLayer);
			               }
		               } else if (this.ns6) {
			               this.currentY = (document.getElementById('floatLayer').style.top == "" ? 0 : parseInt(document
			                     .getElementById('floatLayer').style.top));
			               if (this.originalY <= 0) {
				               this.originalY = this.getLayerY(document.getElementById('floatLayer'));
			               }
		               } else if (this.ie4) {
			               this.currentY = floatLayer.style.pixelTop;
			               if (this.originalY <= 0) {
				               this.originalY = this.getLayerY(floatLayer);
			               }
		               }
		               this.mainTrigger();
	               },
	               mainTrigger : function() {
		               var newTargetY = 0;
		               if (this.scrollTop > this.originalY)
			               newTargetY = this.scrollTop + this.topMargin - this.originalY;
		               if (this.currentY != newTargetY) {
			               if (newTargetY != this.targetY) {
				               this.targetY = newTargetY;
				               this.floatStart();
			               }
			               this.animator();
		               }
	               },
	               floatStart : function() {
		               var now = new Date();
		               this.A = this.targetY - this.currentY;
		               this.B = Math.PI / (2 * this.slideTime);
		               this.C = now.getTime();
		               if (Math.abs(this.A) > this.findHt) {
			               this.D = this.A > 0 ? this.targetY - this.findHt : this.targetY + this.findHt;
			               this.A = this.A > 0 ? this.findHt : -this.findHt;
		               } else {
			               this.D = this.currentY;
		               }
	               },
	               animator : function() {
		               var now = new Date();
		               var newY = this.A * Math.sin(this.B * (now.getTime() - this.C)) + this.D;
		               newY = Math.round(newY);
		               if ((this.A > 0 && newY > this.currentY) || (this.A < 0 && newY < this.currentY)) {
			               if (this.ie4)
				               floatLayer.style.pixelTop = newY;
			               if (this.ns4)
				               document.floatLayer.top = newY;
			               if (this.ns6)
				               document.getElementById('floatLayer').style.top = newY + "px";
		               }
	               },
	               getLayerY : function(obj) {
		               var curtop = 0;
		               if (obj.offsetParent) {
			               while (obj.offsetParent) {
				               curtop += obj.offsetTop;
				               obj = obj.offsetParent;
			               }
		               } else if (obj.y)
			               curtop += obj.y;
		               return curtop;
	               },
	               start_float : function() {
		               this.Float_Time = window.setInterval(this.instanceName + ".main()", 10);
	               },
	               lock_float : function() {
		               if (this.Float_Time != 'Null')
			               window.clearInterval(this.Float_Time);
		               this.Float_Time = 'Null';
		               document.getElementById("lock_outils").className = 'unlock';
	               },
	               lock_unlock_float : function() {
		               if (isFinite(this.Float_Time)) {
			               if (this.Float_Time != 'Null')
				               window.clearInterval(this.Float_Time);
			               this.Float_Time = 'Null';
			               document.getElementById("lock_outils").className = 'unlock';
		               } else {
			               this.Float_Time = window.setInterval(this.instanceName + ".main()", 10);
			               document.getElementById("lock_outils").className = 'lock';
		               }
	               },
	               slide_start : function() {
		               if (this.slide_x == 0 && !this.slide_ismoving) {
			               this.slide_entree();
		               } else if (!this.slide_ismoving)
			               this.slide_sortie();
	               },
	               slide_over : function() {
		               clearTimeout(this.slide_tempo);
		               if (this.slide_x < 0 && !this.slide_ismoving)
			               this.slide_sortie();
	               },
	               slide_entree : function() {
		               if (this.slide_x > -165) {
			               this.slide_ismoving = true;
			               if (this.slide_x < -159) {
				               this.slide_x = -165;
			               } else {
				               this.slide_x -= 10;
			               }
			               Dom.setStyle("onglet_outils", "width", (210 + this.slide_x) + 'px');
			               setTimeout(this.instanceName + ".slide_entree()", 1);
		               } else {
			               this.slide_ismoving = false;
		               }
	               },
	               slide_sortie : function() {
		               clearTimeout(this.slide_tempo);
		               if (this.slide_x < 0) {
			               this.slide_ismoving = true;
			               if (this.slide_x > -10) {
				               this.slide_x = 0;
			               } else {
				               this.slide_x += 10;
			               }
			               Dom.setStyle("onglet_outils", "width", (210 + this.slide_x) + 'px');
			               setTimeout(this.instanceName + ".slide_sortie()", 1);
		               } else {
			               this.slide_ismoving = false;
		               }
	               },

	               /**
						 * Fired by YUI when parent element is available for scripting
						 * 
						 * @method onReady
						 */
	               onReady : function DockBar_onReady() {

		               // TODO ne pas utiliser ca car bubble on changeFilter
		               /**
							 * Create datatable
							 */

		               var me = this;

		               Alfresco.util.Ajax
		                     .request({
		                        url : Alfresco.constants.PROXY_URI
		                              + "slingshot/doclib/doclist/product/node/alfresco/company/home?max=5&type=product&filter=favourites",
		                        successCallback : {
		                           fn : function(response) {

			                           var data = response.json;

			                           me.dataSource = new YAHOO.util.DataSource(data.items);
			                           me.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;

			                           me.widgets.dockBarDataTable = new YAHOO.widget.DataTable(me.id + "-products", [ {
			                              key : "detail",
			                              sortable : false,
			                              formatter : me.bind(me.renderCellDetail)
			                           } ], this.dataSource, {
				                           className : "alfresco-datatable simple-doclist"
			                           });

			                           var original_doBeforeLoadData = me.widgets.dockBarDataTable.doBeforeLoadData;

			                           me.widgets.dockBarDataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(
			                                 sRequest, oResponse, oPayload) {
				                           if (oResponse.results.length === 0) {
					                           oResponse.results.unshift({
					                              isInfo : true,
					                              title : me.msg("empty.title"),
					                              description : me.msg("empty.description")
					                           });
				                           }

				                           return original_doBeforeLoadData.apply(this, arguments);
			                           };

			                           // Select the preferred filter in the ui

			                           var fnOnShowCharactHandler = function DockBar__fnOnShowCharactHandler(layer, args) {
				                           var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
				                           if (owner !== null) {
					                           me.onActionShowCharact.call(me, args[1].target.offsetParent, owner);
				                           }
				                           return true;
			                           };
			                           YAHOO.Bubbling.addDefaultAction(CHARACT_EVENTCLASS, fnOnShowCharactHandler);

		                           },
		                           scope : this
		                        },
		                        failureCallback : {
		                           fn : function() {
			                           // DO nothing
		                           },
		                           scope : this
		                        }
		                     });

		               // Override DataTable function to set custom empty message

		               me.start_float();

	               },

	               /**
						 * Detail custom datacell formatter
						 * 
						 * @method renderCellDetail
						 * @param elCell
						 *           {object}
						 * @param oRecord
						 *           {object}
						 * @param oColumn
						 *           {object}
						 * @param oData
						 *           {object|string}
						 */
	               renderCellDetail : function DockBar_renderCellDetail(elCell, oRecord, oColumn, oData) {
		               var record = oRecord.getData(), desc = "";

		               if (record.isInfo) {
			               desc += '<div class="empty"><h3>' + record.title + '</h3>';
			               desc += '<span>' + record.description + '</span></div>';
		               } else {
			               var locn = record.location, nodeRef = new Alfresco.util.NodeRef(record.nodeRef), docDetailsUrl = Alfresco.constants.URL_PAGECONTEXT
			                     + "site/" + locn.site + "/document-details?nodeRef=" + nodeRef.toString(), contentUrl = Alfresco.constants.PROXY_URI
			                     + record.contentUrl;

			               /**
								 * Simple View
								 */
			               desc += '<h3 class="filename"><a class="theme-color-1" href="' + docDetailsUrl + '">'
			                     + $html(record.displayName) + '</a></h3>';

			               /* Favourite / Charact / Download */
			               desc += '<div class="dockbar-detail">';
			               desc += '<span class="item-separator"><a class="document-download" href="' + contentUrl
			                     + '"  title="' + this.msg("actions.document.download") + '" tabindex="0">'
			                     + this.msg("actions.document.download") + '</a></span>';
			               desc += '<span class="item-separator"><a class="document-characts ' + CHARACT_EVENTCLASS
			                     + '" title="' + this.msg("actions.document.viewEntityLists") + '" tabindex="0">'
			                     + this.msg("actions.document.viewEntityLists") + '</a></span>';
			               desc += '</div>';

		               }

		               elCell.innerHTML = desc;
	               },

	               onActionShowCharact : function DockBar_onActionShowCharact(row) {

		               var p_record = this.widgets.dockBarDataTable.getData(row), nodeRef = new Alfresco.util.NodeRef(
		                     p_record.nodeRef);

		               var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site : null;
		               var redirect = $siteURL("entity-data-lists?nodeRef=" + nodeRef, {
			               site : recordSiteName
		               });

		               if (p_record.nodeType == "bcpg:finishedProduct" || p_record.nodeType == "bcpg:semiFinishedProduct") {
			               redirect += "&list=compoList";
		               } else if (p_record.nodeType == "bcpg:packagingKit") {
			               redirect += "&list=packagingList";
		               }

		               window.location.href = redirect;
	               }

	            });
})();
