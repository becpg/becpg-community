/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;
	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	var REQFILTER_EVENTCLASS = Alfresco.util.generateDomId(null, "reqType");

	/**
	 * ProductNotifications constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.ProductNotifications} The new
	 *         ProductNotifications instance
	 * @constructor
	 */
	beCPG.component.ProductNotifications = function(htmlId) {

		beCPG.component.ProductNotifications.superclass.constructor.call(this, "beCPG.component.ProductNotifications", htmlId, [ "button",
				"container" ]);

		// message
		this.name = "beCPG.component.EntityDataListToolbar";

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.ProductNotifications, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.ProductNotifications.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {

			entityNodeRef : "",

			reqCtrlListNodeRef : "",

			entity : null,

			containerDiv : null,

			toolBarInstance : null

		},

		currentPage : 1,

		queryExecutionId : null,

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function ProductNotifications_onReady() {

			var instance = this;
			
//			TODO
//			new YAHOO.widget.Panel(this, "show-notifications", {
//				width : "240px",
//				fixedcenter : false,
//				close : false,
//				draggable : false,
//				zindex : 99,
//				modal : false,
//				visible : false
//			});

			this.widgets.showNotificationsButton = this.createYUIButton(this, "show-notifications", function() {

				instance.reloadDataTable();
				instance.widgets.panel.show();
				
			});
			
			Dom.addClass(instance.id +"-show-notifications", "loading");

			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "becpg/product/reqctrllist/node/" + instance.options.entityNodeRef.replace(":/", "") + "?view="
						+ instance.options.list,
				method : Alfresco.util.Ajax.GET,
				responseContentType : Alfresco.util.Ajax.JSON,
				successCallback : {
					fn : function(response) {

						if (response.json) {

							instance.options.reqCtrlListNodeRef = response.json.reqCtrlListNodeRef != null ? response.json.reqCtrlListNodeRef : "";

							// Inject the template from the XHR request into a
							// new DIV
							// element
							var containerDiv = document.createElement("div");

							containerDiv.innerHTML = instance.createPanel(response.json);

							// The panel is created from the HTML returned in
							// the XHR
							// request, not the container
							var panelDiv = Dom.getFirstChild(containerDiv);
							this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
								draggable : false,
								fixedcenter: false,
								width : "50em"
							});

							instance.widgets.dataSource = new YAHOO.util.DataSource(this.getWebscriptUrl(), {
								connMethodPost : true,
								responseType : YAHOO.util.DataSource.TYPE_JSON,
								responseSchema : {
									resultsList : "items",
									metaFields : {
										startIndex : "startIndex",
										totalRecords : "totalRecords",
										queryExecutionId : "queryExecutionId"
									}
								}
							});
							var columDefs = [ {
								key : "detail",
								sortable : false,
								formatter : this.bind(this.renderCellDetail)
							} ];

							instance.widgets.notificationsDataTable = new YAHOO.widget.DataTable(instance.id + "-notificationTable", columDefs,
									instance.widgets.dataSource, {
										initialLoad : false,
										dynamicData : false,
										"MSG_EMPTY" : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
										"MSG_ERROR" : this.msg("message.error"),
										className : "alfresco-datatable simple-doclist",
										renderLoopSize : 4,
										paginator : null
									});

							instance.widgets.notificationsDataTable.getDataTable = function() {
								return this;
							};

							instance.widgets.notificationsDataTable.getData = function(recordId) {
								return this.getRecord(recordId).getData();
							};

							instance.widgets.notificationsDataTable.loadDataTable = function DataTable_loadDataTable(parameters) {

								instance.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

								if (Alfresco.util.CSRFPolicy.isFilterEnabled()) {
									instance.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(), Alfresco.util.CSRFPolicy
											.getToken(), false);
								}

								instance.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(parameters), {
									success : function DataTable_loadDataTable_success(oRequest, oResponse, oPayload) {
										instance.widgets.notificationsDataTable.onDataReturnReplaceRows(oRequest, oResponse, oPayload);

										if (instance.widgets.paginator) {
											instance.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
											instance.widgets.paginator.setPage(oResponse.meta.startIndex, true);
										}
										instance.queryExecutionId = oResponse.meta.queryExecutionId;

									},
									// success :
									// me.widgets.alfrescoDataTable.onDataReturnSetRows,
									failure : instance.widgets.notificationsDataTable.onDataReturnReplaceRows,
									scope : instance.widgets.notificationsDataTable,
									argument : {}
								});
							};
							// Override DataTable function to set custom empty
							// message
							var dataTable = instance.widgets.notificationsDataTable, original_doBeforeLoadData = dataTable.doBeforeLoadData;

							dataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(sRequest, oResponse, oPayload) {
								if (oResponse.results && oResponse.results.length === 0) {
									oResponse.results.unshift({
										isInfo : true,
										title : instance.msg("empty.notifications.title"),
										description : instance.msg("empty.notifications.description")
									});
								}

								return original_doBeforeLoadData.apply(this, arguments);
							};

							
							Dom.removeClass(instance.id +"-show-notifications", "loading");
							
							YAHOO.Bubbling.addDefaultAction(REQFILTER_EVENTCLASS, instance.onFilterChange);

						}

					},
					scope : instance
				},
				failureMessage : "Could not load html template for version graph",
				execScripts : true
			});

		},

		createYUIButton : function(instance, actionName, fn) {

			var template = Dom.get(this.options.toolBarInstance.id + "-toolBar-template-button"), buttonWidget = null;

			var spanEl = Dom.getFirstChild(template).cloneNode(true);

			Dom.addClass(spanEl, actionName);

			Dom.setAttribute(spanEl, "id", this.id + "-" + actionName + "Button");

			this.options.containerDiv.appendChild(spanEl);

			buttonWidget = Alfresco.util.createYUIButton(this, actionName + "Button", fn);

			buttonWidget.set("label", this.msg("button." + actionName));
			buttonWidget.set("title", this.msg("button." + actionName + ".description"));

			return buttonWidget;

		},

		createPanel : function(json) {

			var instance = this;

			var html = "<div><div class=\"hd\"></div><div class=\"bd\"><div id=\""+instance.id+"-scoresDiv\" class=\"ctrlSumPreview  \" >";

			// put score div
			if (json.scores !== undefined) {
				var scores = json.scores;
				var intScore = parseInt(scores.global);
				var spriteIndex = (intScore / 5 >> 0);

				html += "<ul><li class=\"title\">" + instance.msg("label.product.scores") + "</li>" + "<li id=\"scoreLi\" class=\"score-"
						+ spriteIndex + "\" " + "title=\"" + instance.msg("tooltip.components.validation") + ": "
						+ Math.floor(scores.details.componentsValidation) + "%\n" + instance.msg("tooltip.mandatory.completion") + ": "
						+ Math.floor(scores.details.mandatoryFields) + "%\n" + instance.msg("tooltip.specification.respect") + ": "
						+ Math.floor(scores.details.specifications) + "%\">";

				html += "<span>" + Math.floor(scores.global) + "%</span>";
				html += "</li></ul>";
			}

			// TODO Merge;

			var scoreDiv = YAHOO.util.Dom.get("scoreLi");
			if (scoreDiv !== undefined && scoreDiv != null) {
				var scoreDivClassName = scoreDiv.className;
				var spriteIndex = scoreDivClassName.split("-")[1];
				var imgWidth = 74;
				var widthRatio = imgWidth / 166;

				var rightPos = (((spriteIndex - 1) * (166 + 14) + 2 + (spriteIndex > 4 ? 10 : 0)) * widthRatio) + "px";
				scoreDiv.style.backgroundPosition = "-" + rightPos + " 0px";
				scoreDiv.style.width = imgWidth + "px";
				scoreDiv.style.height = imgWidth + "px";

				var backgroundSize = Math.floor(3629 * widthRatio) + "px " + Math.floor(396 * widthRatio) + "px";
				scoreDiv.childNodes[0].style.lineHeight = imgWidth + "px";
				scoreDiv.childNodes[0].style.fontSize = 3 * widthRatio + "em";
				scoreDiv.style.backgroundSize = backgroundSize;
			}

			// if we have some constraints in res
			if (json.rclNumber !== undefined && json.rclNumber != null && json.rclNumber.length > 0) {
				// Parses each array mapped to dataType
				html += "<div class=\"dataTypeList\"><div class=\"title\">" + instance.msg("label.constraints.violations")
						+ "<span class=\"req-all-all rclFilterSelected\"><a class=\"req-filter " + REQFILTER_EVENTCLASS + " href=\"#\">"
						+ instance.msg("label.constraints.view-all") + "</a></span></div>";

				html += "<div class=\"rclFilterElt\"><div>";

				for ( var dataType in json.rclNumber) {
					var scoreInfo = "";
					var dataTypeName = Object.keys(json.rclNumber[dataType])[0];
					html += "<div class=\"div-" + dataTypeName.toString().toLowerCase() + "\"><span class=\"span-"
							+ dataTypeName.toString().toLowerCase() + "\"><a class=\"req-filter " + REQFILTER_EVENTCLASS + "\" href=\"#\">"
							+ instance.msg("label.constraints." + dataTypeName.toString().toLowerCase()) + scoreInfo + "</a></span><ul>";

					var types = json.rclNumber[dataType];

					for ( var type in types[dataTypeName]) {
						var value = types[dataTypeName][type];

						html += "<li><span class=\"req-" + dataTypeName.toString().toLowerCase() + "-" + type + "\" title=\""
								+ instance.msg("reqTypes." + type) + "\"><a class=\"req-filter " + REQFILTER_EVENTCLASS
								+ "\" href=\"#\"><span class=\"reqType" + type + "\"></span>" + value + "</a></li>";

					}
					html += "</ul></div>";

				}
				html += "</div></div></div>";
			}

			html += "</div><div id=\"" + instance.id + "-notificationTable\"></div></div></div>";

			return html;

		},

		/**
		 * Generate base webscript url. Can be overridden.
		 * 
		 * @method getWebscriptUrl
		 */
		getWebscriptUrl : function ProjectDashlet_getWebscriptUrl() {

			// TODO manque parentNodeRef;

			return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node/" + this.options.reqCtrlListNodeRef.replace(":/", "")
					+ "?repo=true&itemType=bcpg:reqCtrlList&pageSize=50&dataListName=reqCtrlList&entityNodeRef=" + this.options.entityNodeRef;

		},

		/**
		 * Calculate webscript parameters
		 * 
		 * @method getParameters
		 * @override
		 */
		getParameters : function ProjectDashlet_getParameters() {

			var request = {
				fields : [ "bcpg_rclReqType", "bcpg_rclReqMessage", "bcpg_rclSources", "bcpg_rclDataType" ],
				page : this.currentPage,
				queryExecutionId : this.queryExecutionId,
				filter : {
					filterId : "all",
					filterOwner : null,
					filterData : "",
					filterParams : null
				},
				extraParams : null
			};
			return request;

		},

		onFilterChange : function ProjectDashlet_onFilterChange(p_sType, p_aArgs) {

			var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
			var selectedItems = document.getElementsByClassName("rclFilterSelected");

			// sets clicked item to selected
			for (var i = 0; i < selectedItems.length; i++) {
				selectedItems[i].classList.remove("rclFilterSelected");
			}
			var chgClass = owner;
			if (owner.parentNode.nodeName == "LI") {
				chgClass = owner.parentNode;
			}
			YAHOO.util.Dom.addClass(chgClass, "rclFilterSelected");

			// refreshes view by calling filter
			var splits = owner.className.split("-");
			var type = (splits.length > 2 ? splits[2].split(" ")[0] : undefined);
			var dataType = splits[1].charAt(0).toUpperCase() + splits[1].slice(1);
			YAHOO.Bubbling.fire("constraintsList-" + instance.id + "changeFilter", {
				filterOwner : "constraintsList-" + instance.id,
				filterId : (type === "all" && dataType === "All" ? "all" : "filterform"),
				filterData : (type === "all" && dataType === "All" ? undefined : "{"
						+ (type !== undefined ? ("\"prop_bcpg_rclReqType\":" + type) : "")
						+ (dataType !== null ? (type !== undefined ? "," : "") + ("\"prop_bcpg_rclDataType\":" + dataType) : "") + "}")
			});

			args[0].stopPropagation();
			args[1].decrepitate = true;
			this.reloadDataTable();
		},

		/**
		 * Detail custom datacell formatter
		 * 
		 * @method renderCellDetail
		 * @param elCell
		 *            {object}
		 * @param oRecord
		 *            {object}
		 * @param oColumn
		 *            {object}
		 * @param oData
		 *            {object|string}
		 */
		renderCellDetail : function ProjectDashlet_renderCellDetail(elCell, oRecord, oColumn, oData) {
			var record = oRecord.getData(), desc = "", dateLine = "";

			if (record.isInfo) {
				desc += '<div class="empty"><h3>' + record.title + '</h3>';
				desc += '<span>' + record.description + '</span></div>';
			} else {

				var reqType = oRecord.getData("itemData")["prop_bcpg_rclReqType"].value;
				var reqProducts = oRecord.getData("itemData")["assoc_bcpg_rclSources"];
				desc += '<div class="rclReq-details">';
				if (reqType) {
					desc += '   <div class="icon" ><span class="reqType' + reqType + '" title="'
							+ Alfresco.util.encodeHTML(this.msg("data.reqtype." + reqType.toLowerCase())) + '">&nbsp;</span></div>';
				}
				desc += '      <div class="rclReq-title">' + Alfresco.util.encodeHTML(oRecord.getData("itemData")["prop_bcpg_rclReqMessage"].displayValue) + '</div>';
				desc += '      <div class="rclReq-content"><ul>';

				if (reqProducts) {
					for ( var i in reqProducts) {
						var product = reqProducts[i], pUrl = beCPG.util.entityURL(product.siteId, product.value);

						if (product.metadata.indexOf("finishedProduct") != -1 || product.metadata.indexOf("semiFinishedProduct") != -1) {
							pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, "compoList");
						} else if (product.metadata.indexOf("packagingKit") != -1) {
							pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, "packagingList");
						}

						if (pUrl) {
							//TODO						
							pUrl += "&bcPath=true&bcList=TODO" ;//+ this.datalistMeta.name;
						}

						desc += '<li><span class="' + product.metadata + '" ><a href="' + pUrl + '">'
								+ Alfresco.util.encodeHTML(product.displayValue) + '</a></span></li>';

					}
				}
				+'</ul></div>';
				desc += '   </div>';
				desc += '   <div class="clear"></div>';
				desc += '</div>';

			}
			elCell.innerHTML = desc;
		},
		/**
		 * Reloads the DataTable
		 * 
		 * @method reloadDataTable
		 */
		reloadDataTable : function SimpleDocList_reloadDataTable() {
			this.widgets.notificationsDataTable.loadDataTable(this.getParameters());
		}

	}, true);

})();
