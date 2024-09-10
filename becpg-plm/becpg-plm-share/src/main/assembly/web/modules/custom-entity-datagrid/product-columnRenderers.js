/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
if (beCPG.module.EntityDataGridRenderers) {


	var NUMBER_FORMAT = { "maximumFractionDigits": 4 };
	var NUTDETAILS_EVENTCLASS = Alfresco.util.generateDomId(null, "nutDetails");

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:product", "bcpg:supplier", "bcpg:client", "bcpg:entityV2", "bcpg:resourceProduct",
			"cm:content_bcpg:costDetailsListSource", "bcpg:product_bcpg:packagingListProduct", "bcpg:product_bcpg:compoListProduct", "bcpg:product_bcpg:productListProduct",
			"ecm:wulSourceItems", "ecm:rlSourceItems", "bcpg:psclSourceItem", "ecm:rlTargetItem", "ecm:culSourceItem", "ecm:culTargetItem", "ecm:cclSourceItem", "bcpg:product_bcpg:nutListSources"
			, "cm:cmobject_bcpg:lrComponents", "bcpg:product_gs1:cplProduct"],
		renderer: function(oRecord, data, label, scope, z, zz, elCell, oColumn) {

			var url = beCPG.util.entityURL(data.siteId, data.value), version = "";
			var toogleGroupButton = null;
			var padding = 0;
			var tr = scope.widgets.dataTable.getTrEl(elCell);

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
				padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;

				Dom.addClass(tr, "mtl-level-" + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value);
			}

			if (label == "mpm:plProduct" || label == "bcpg:compoListProduct" || label == "bcpg:packagingListProduct" || label == "mpm:plResource" || label == "bcpg:productListProduct") {
				// datalist
				if (data.metadata.indexOf("finishedProduct") != -1 || data.metadata.indexOf("semiFinishedProduct") != -1) {
					url = beCPG.util.entityURL(data.siteId, data.value, null, null, "compoList");
				} else if (data.metadata.indexOf("packagingKit") != -1) {
					url = beCPG.util.entityURL(data.siteId, data.value, null, null, "packagingList");
				} else if (data.metadata.indexOf("localSemiFinishedProduct") != -1) {
					url = null;
				}
				if (data.version && data.version !== "") {
					version = '<span class="document-version">' + data.version + '</span>';
				}
				if (url != null) {
					url += "&bcPath=true&bcList=" + scope.datalistMeta.name;
				}

				if (false === oRecord.getData("itemData")["isLeaf"]) {
					toogleGroupButton = '<div id="group_' + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '_' + oRecord.getData("nodeRef") + '" style="margin-left:' + padding
						+ 'px;" class="onCollapsedAndExpanded" ><a href="#" class="' + scope.id + '-action-link"><span class="gicon ggroup-'
						+ (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '"></span></a></div>';
					Dom.addClass(tr, "mtl-" + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed"));

				} else if (true === oRecord.getData("itemData")["isLeaf"]) {
					padding += 25;

					Dom.addClass(tr, "mtl-leaf");
				}
			}

			if (label == "mpm:rplResourceRef" || label == "bcpg:nutListSources") {
				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				}
			}

			return (toogleGroupButton != null ? toogleGroupButton : '') + '<span class="' + data.metadata + '" ' + (toogleGroupButton == null && padding != 0 ? 'style="margin-left:' + padding + 'px;"' : '') + '>'
				+ (url != null ? '<a href="' + url + '">' : '')
				+ Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>' + version;

		}

	});



	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:ing_bcpg:ingListIng"],
		renderer: function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
			var url = null;
			var toogleGroupButton = null;
			var padding = 0;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			var tr = scope.widgets.dataTable.getTrEl(elCell);

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
				padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;

				Dom.addClass(tr, "mtl-level-" + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value);
			}


			if (false === oRecord.getData("itemData")["isLeaf"]) {
				toogleGroupButton = '<div id="group_' + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '_' + oRecord.getData("nodeRef") + '" style="margin-left:' + padding
					+ 'px;" class="onCollapsedAndExpanded" ><a href="#" class="' + scope.id + '-action-link"><span class="gicon ggroup-'
					+ (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '"></span></a></div>';
				Dom.addClass(tr, "mtl-" + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed"));

			} else if (true === oRecord.getData("itemData")["isLeaf"]) {
				padding += 25;

				Dom.addClass(tr, "mtl-leaf");
			}


			return (toogleGroupButton != null ? toogleGroupButton : '') + '<span class="' + data.metadata + '" ' + (toogleGroupButton == null && padding != 0 ? 'style="margin-left:' + padding + 'px;"' : '') + '>'
				+ (url != null ? '<a href="' + url + '">' : '')
				+ Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["boolean_bcpg:allergenListVoluntary", "boolean_bcpg:allergenListInVoluntary",
			"boolean_bcpg:allergenListOnSite", "boolean_bcpg:allergenListOnLine", "boolean_bcpg:allergenListIsCleaned",
			"boolean_bcpg:packagingListIsMaster", "boolean_ecm:culTreated", "boolean_ecm:isWUsedImpacted"],
		renderer: function(oRecord, data, label, scope) {
			if (data.value) {
				return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:lclClaimValue"],
		renderer: function(oRecord, data, label, scope) {
			if ("true" === data.value) {
				return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			} else if ("suitable" === data.value) {
				return '<span class="blue">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			} else if ("certified" === data.value) {
				return '<span class="green">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:allergenListDecisionTree",
		renderer: function(oRecord, data, label, scope) {
			var ret = "";
			if (data.value) {
				var values = eval(data.value);

				for (var i = 0; i < values.length; i++) {
					if (ret.length > 0) {
						ret += ", ";
					}
					var msgKey = values[i].cid == "-" ? "form.control.decision-tree.empty" : "form.control.decision-tree.allergenList."
						+ values[i].qid + "." + values[i].cid;

					ret += values[i].qid.toUpperCase() + ": " + scope.msg(msgKey);
				}
			}
			return ret;
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["cm:cmobject_bcpg:allergenListVolSources", "cm:cmobject_bcpg:allergenListInVolSources", "bcpg:irlIng", "bcpg:irlSources"],
		renderer: function(oRecord, data, label, scope) {
			if (data.metadata == "ing") {
				return '<span class="' + data.metadata + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}
			return '<span class="' + data.metadata + '" ><a href="' + beCPG.util.entityURL(data.siteId, data.value) + '">'
				+ Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:rclReqType", "bcpg:filReqType", "ecm:culReqType"],
		renderer: function(oRecord, data, label, scope) {
			if (data.value != null && data.value.length > 0) {
				return '<span class="reqType' + data.value + '">' + Alfresco.util.encodeHTML(scope.msg("data.reqtype." + data.value.toLowerCase())) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["ecm:rlRevisionType", "ecm:culRevision"],
		renderer: function(oRecord, data, label, scope) {

			if (data.value != null) {
				if (oRecord.getData("itemData")["prop_ecm_culReqError"]) {
					var error = oRecord.getData("itemData")["prop_ecm_culReqError"].value;
					if (error != null) {
						return '<span class="lcl-formulated-error" title="' + Alfresco.util.encodeHTML(error) + '">'
							+ scope.msg("data.revisiontype." + data.value.toLowerCase()) + '</span>';
					}
				}
				return scope.msg("data.revisiontype." + data.value.toLowerCase());

			}
			return Alfresco.util.encodeHTML(data.displayValue);

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["qa:slControlPoint", "qa:clControlPoint"],
		renderer: function(oRecord, data, label, scope) {
			var url = beCPG.util.entityURL(data.siteId, data.value);
			return '<span class="controlPoint"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["qa:stockList"],
		renderer: function(oRecord, data, label, scope) {
			var url = scope._buildCellUrl(data);
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}
			return '<span class="' + data.metadata + '"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["qa:clCharacts"],
		renderer: function(oRecord, data, label, scope) {

			return '<span class="' + data.metadata + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["ecm:wulTargetItem"],
		renderer: function(oRecord, data, label, scope) {

			var url = beCPG.util.entityURL(data.siteId, data.value);

			return '<span class="' + data.metadata + '" ' + '>' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "cm:name",
		renderer: function(oRecord, data, label, scope) {

			var type = oRecord.getData("itemType");

			var title = null;
			if (data && data.value) {
				var itemData = oRecord.getData("itemData");
				if (type == "bcpg:labelingRuleList" && itemData && itemData["prop_cm_title"] && itemData["prop_cm_title"].value != null) {
					title = itemData["prop_cm_title"].value;
				}

				if (!title || !title.trim()) {
					title = data.value;
				}
			}


			if (type && type.split(":").length > 1) {
				var metadata = type.split(":")[1];

				if (metadata) {
					var entityUrl = beCPG.util.entityURL(oRecord.getData("siteId"), oRecord.getData("nodeRef"), type, null, "View-properties");

					if (entityUrl) {
						return '<span class="' + metadata + '" ><a class="theme-color-1" href="' + entityUrl + '">' + Alfresco.util.encodeHTML(title) + '</a></span>';
					}
				}
			}

			return Alfresco.util.encodeHTML(title);
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["qa:clState", "qa:slSampleState", "qa:qcState"],
		renderer: function(oRecord, data, label, scope) {
			if (data.value != null) {
				if (data.value == "Compliant") {
					return '<span class="green">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				} else if (data.value == "NonCompliant") {
					return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				}

			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:allergen", "bcpg:ing", "bcpg:geoOrigin", "bcpg:bioOrigin", "bcpg:geo", "bcpg:microbio", "bcpg:organo", "bcpg:listValue"],
		renderer: function(oRecord, data, label, scope) {
			var url = null;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">' + (url != null ? '<a href="' + url + '">' : '')
					+ Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
			}

			return '<span class="' + data.metadata + '" >' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});




	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:physicoChem",
		renderer: function(oRecord, data, label, scope) {

			var url = null;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			var title = "";
			var cssClass = data.metadata;
			var isFormulated = oRecord.getData("itemData")["prop_bcpg_physicoChemIsFormulated"].value;
			var error = extractErrorMessage(oRecord.getData("itemData")["dt_bcpg_reqCtrlList"], "Physicochem");
			if (error) {
				cssClass = "physicoChem-formulated-error";
				title = Alfresco.util.encodeHTML(error);
			} else if (isFormulated) {
				cssClass = "physicoChem-formulated";
			}

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="' + title + '">'
					+ (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue)
					+ (url != null ? '</a>' : '') + '</span>';
			}

			return '<span class="' + cssClass + '" title="' + title + '">' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:nut",
		renderer: function(oRecord, data, label, scope) {

			var url = null;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			var title = "";
			var cssClass = data.metadata;
			if (oRecord.getData("itemData")["prop_bcpg_nutListIsFormulated"]) {
				var message = extractErrorMessage(oRecord.getData("itemData")["dt_bcpg_reqCtrlList"], "Nutrient");
				if (message) {
					cssClass = "nut-formulated-error";
					title = Alfresco.util.encodeHTML(message);
				} else if (oRecord.getData("itemData")["prop_bcpg_nutListIsFormulated"].value) {
					cssClass = "nut-formulated";
				}
			}

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="' + title + '">'
					+ (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue)
					+ (url != null ? '</a>' : '') + '</span>';
			}

			return '<span class="' + cssClass + '" title="' + title + '">' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});



	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:nutListValue", "bcpg:nutListMini", "bcpg:nutListMaxi", "bcpg:nutListValuePrepared", "bcpg:nutListValuePerServing"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			var ret = "";


			var unit = oRecord._oData.itemData.prop_bcpg_nutListUnit ? oRecord._oData.itemData.prop_bcpg_nutListUnit.value : null
			if (oColumn.field == "prop_bcpg_nutListValuePrepared" && oRecord._oData.itemData.prop_bcpg_nutListUnitPrepared.value != null) {
				unit = oRecord._oData.itemData.prop_bcpg_nutListUnitPrepared != null ? oRecord._oData.itemData.prop_bcpg_nutListUnitPrepared.value : null;
			}

			if (unit != null && ((oColumn.label != null && oColumn.label.indexOf && (oColumn.label.indexOf("100g") > 0 || oColumn.label.indexOf("/") > 0))
				|| (oColumn.field == "prop_bcpg_nutListValuePerServing"))) {
				unit = unit.replace("/100g", "").replace("/100ml", "");
			}

			if (data.value != null) {
				if (unit != null && unit.length > 0) {
					ret += data.value.toLocaleString(beCPG.util.getJSLocale()) + " " + unit;
				} else {
					ret += data.value.toLocaleString(beCPG.util.getJSLocale());
				}
			}

			var key = "prop_bcpg_nutListFormulatedValue";

			if (oColumn.field == "prop_bcpg_nutListMini") {
				key = "prop_bcpg_nutListFormulatedMini";
			} else if (oColumn.field == "prop_bcpg_nutListMaxi") {
				key = "prop_bcpg_nutListFormulatedMaxi";
			} else if (oColumn.field == "prop_bcpg_nutListValuePrepared") {
				key = "prop_bcpg_nutListFormulatedValuePrepared";
			} else if (oColumn.field == "prop_bcpg_nutListValuePerServing") {
				key = "prop_bcpg_nutListFormulatedValuePerServing";
			}


			var formulatedValue = oRecord.getData("itemData")[key];
			if (formulatedValue != null && formulatedValue.value != null) {
				if (ret.length > 0) {
					ret += '&nbsp;&nbsp;(' + beCPG.util.exp(formulatedValue.value) + ')';
				} else {
					if (unit != null && unit.length > 0) {
						ret += beCPG.util.exp(formulatedValue.value) + " " + unit;
					} else {
						ret += beCPG.util.exp(formulatedValue.value);
					}
				}
			}

			return ret;
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:nutListRoundedValue", "bcpg:nutListRoundedValuePrepared"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			var ret = "";

			if (data.value != null) {

				var key = beCPG.util.getRegulatoryCountryKey(Alfresco.constants.JS_LOCALE);
				var jsonData = JSON.parse(data.value);
				if (jsonData && jsonData.v) {
					if (jsonData.v[key] != null) {
						var minimumFractionDigits = 0;
						if (key == "EU" && jsonData.v[key] != null && jsonData.v[key] < 10 && jsonData.v[key] >= 1) {
							minimumFractionDigits = 1;
						}
						if (jsonData.tu && jsonData.tu[key]) {
							ret += '<span class="red">' + jsonData.tu[key].toLocaleString(beCPG.util.getJSLocale(), {
								minimumFractionDigits: minimumFractionDigits,
								maximumFractionDigits: 5,
								useGrouping: true
							}) + '</span>' + "<";
						}
						if (key == "US") {
							if (jsonData.vps[key]) {
								ret += '<span class="green">' + jsonData.vps[key].toLocaleString(beCPG.util.getJSLocale()) + '</span>';
							}
						} else {
							ret += '<span class="green">' + jsonData.v[key].toLocaleString(beCPG.util.getJSLocale(), {
								minimumFractionDigits: minimumFractionDigits,
								maximumFractionDigits: 5,
								useGrouping: true
							}) + '</span>';
						}

						if (jsonData.tl && jsonData.tl[key]) {
							ret += "<" + '<span class="red">' + jsonData.tl[key].toLocaleString(beCPG.util.getJSLocale(), {
								minimumFractionDigits: minimumFractionDigits,
								maximumFractionDigits: 5,
								useGrouping: true
							}) + '</span>';
						}

						var nutName = oRecord._oData.itemData.assoc_bcpg_nutListNut[0].displayValue || '';

						var keys = Object.keys(jsonData.v);
						ret += '<div id="nut-details-' + oColumn.field + "-" + oRecord.getData("nodeRef") + '" class="nut-details hidden" ><div class="hd">' + nutName + '</div><div class="bd" >';
						for (var i = 0; i < keys.length; i++) {
							var k = keys[i];
							var value = jsonData.v[k];
							if (k && value) {
								minimumFractionDigits = 0;
								if (k == "EU" && jsonData.v[key] != null && value < 10 && value >= 1) {
									minimumFractionDigits = 1;
								}

								var toleranceMin = jsonData.tl ? jsonData.tl[k] || '' : '';
								var toleranceMax = jsonData.tu ? jsonData.tu[k] || '' : '';
								var min = jsonData.min ? jsonData.min[k] || '' : '';
								var max = jsonData.max ? jsonData.max[k] || '' : '';
								var gda = jsonData.gda ? jsonData.gda[k] || '' : '';
								var vps = jsonData.vps ? jsonData.vps[k] || '' : '';

								ret += '<div>' +
									'<h3>' + scope.msg("nutrient.details.header", k + ' <img  title="' + k + '" src="' + Alfresco.constants.URL_CONTEXT + 'res/components/images/flags/' + k.split("_")[0].toLowerCase() + '.png" />') + '</h3>' +
									'<p>' + scope.msg("nutrient.details.value", value.toLocaleString(beCPG.util.getJSLocale(), {
										minimumFractionDigits: minimumFractionDigits,
										maximumFractionDigits: 5,
										useGrouping: true
									})) + '</p>' +
									(toleranceMin ? '<p>' + scope.msg("nutrient.details.tl", toleranceMin.toLocaleString(beCPG.util.getJSLocale(), {
										minimumFractionDigits: minimumFractionDigits,
										maximumFractionDigits: 5,
										useGrouping: true
									})) + '</p>' : '') +
									(toleranceMax ? '<p>' + scope.msg("nutrient.details.tu", toleranceMax.toLocaleString(beCPG.util.getJSLocale(), {
										minimumFractionDigits: minimumFractionDigits,
										maximumFractionDigits: 5,
										useGrouping: true
									})) + '</p>' : '') +
									(min ? '<p>' + scope.msg("nutrient.details.min", min.toLocaleString(beCPG.util.getJSLocale(), {
										minimumFractionDigits: minimumFractionDigits,
										maximumFractionDigits: 5,
										useGrouping: true
									})) + '</p>' : '') +
									(max ? '<p>' + scope.msg("nutrient.details.max", max.toLocaleString(beCPG.util.getJSLocale(), {
										minimumFractionDigits: minimumFractionDigits,
										maximumFractionDigits: 5,
										useGrouping: true
									})) + '</p>' : '') +
									(gda ? '<p>' + scope.msg("nutrient.details.gda", gda.toLocaleString(beCPG.util.getJSLocale())) + '</p>' : '') +
									(vps ? '<p>' + scope.msg("nutrient.details.vps", vps.toLocaleString(beCPG.util.getJSLocale())) + '</p>' : '') +
									'</div>';
							}
						}
						ret += '</div></div>';

						ret += '<span class="node-' + oColumn.field + "-" + oRecord.getData("nodeRef") + '">';
						ret += '<a class="show-details ' + NUTDETAILS_EVENTCLASS + '" title="' + scope.msg("link.title.nut-details") + '" href="" >';
						ret += "&nbsp;";
						ret += "</a></span>";

					}
				}
			}

			return ret;

		}
	});

	var nutPanels = [];

	YAHOO.Bubbling.addDefaultAction(NUTDETAILS_EVENTCLASS, function(layer, args) {
		var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
		if (owner !== null) {
			var nodeRef = owner.className.replace("node-", "");
			if (!nutPanels["nut-details-" + nodeRef]) {
				var panelDiv = Dom.get("nut-details-" + nodeRef);
				Dom.removeClass(panelDiv, "hidden");
				nutPanels["nut-details-" + nodeRef] = Alfresco.util.createYUIPanel(panelDiv, { draggable: true, width: "auto" });
			}
			nutPanels["nut-details-" + nodeRef].show();
		}
		return true;
	});




	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:allergenListQtyPerc", "bcpg:allergenRegulatoryThreshold", "bcpg:allergenInVoluntaryRegulatoryThreshold", "bcpg:ingListQtyPerc", "bcpg:ingListQtyPercWithYield", "bcpg:ingListQtyPercWithSecondaryYield"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null) {
				var forceUnit = oColumn.forceUnit;


				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				}
				var sigFig = 5;

				var unit, qty;
				if (data.value == 0) {
					return "0";
				} else if ((Math.abs(data.value) < 0.01 && !forceUnit === "perc") || forceUnit === "ppm") {
					qty = data.value * 10000;
					unit = " ppm";
				} else {
					qty = data.value;
					unit = " %";
				}


				if (oRecord.getData("itemType") == "total") {
					return '<span class="total">' + beCPG.util.sigFigs(qty, 7).toLocaleString(beCPG.util.getJSLocale(), { maximumFractionDigits: 20 }) + unit + "</span>";
				}

				if (oColumn.numberFormat) {
					return beCPG.util.formatNumber(oColumn.numberFormat, data.value) + unit;
				}


				return Alfresco.util.encodeHTML(beCPG.util.sigFigs(qty, sigFig).toLocaleString(beCPG.util.getJSLocale(), { maximumFractionDigits: 20 }) + unit);
			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["", "bcpg:regulatoryResult"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null && data.displayValue != null) {

				var color = null;

				if (data.value == "PROHIBITED") {
					color = "rgb(255, 106, 106)";
				} else if (data.value == "PERMITTED") {
					color = "rgb(190, 229, 84)";
				} else if (data.value == "ERROR") {
					color = "orange";
				}

				if (color) {
					elCell.style = 'background-color:' + color + ';';
				}

				return Alfresco.util.encodeHTML(data.displayValue);

			}
			return "";
		}
	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:cost",
		renderer: function(oRecord, data, label, scope) {

			var url = null;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			var title = "";
			var cssClass = data.metadata;

			if (oRecord.getData("itemData")["prop_bcpg_costListIsFormulated"]) {
				var message = extractErrorMessage(oRecord.getData("itemData")["dt_bcpg_reqCtrlList"], "Cost");
				if (message) {
					cssClass = "cost-formulated-error";
					title = Alfresco.util.encodeHTML(message);
				} else if (oRecord.getData("itemData")["prop_bcpg_costListIsFormulated"].value) {
					cssClass = "cost-formulated";
				}
			}

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="' + title + '">'
					+ (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue)
					+ (url != null ? '</a>' : '') + '</span>';
			}

			return '<span class="' + cssClass + '" title="' + title + '">' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:lca",
		renderer: function(oRecord, data, label, scope) {

			var url = null;
			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				url = beCPG.util.entityURL(data.siteId, data.value);
			}

			var title = "";
			var cssClass = data.metadata;

			if (oRecord.getData("itemData")["prop_bcpg_lcaListIsFormulated"]) {
				var message = extractErrorMessage(oRecord.getData("itemData")["dt_bcpg_reqCtrlList"], "Lca");
				if (message) {
					cssClass = "lca-formulated-error";
					title = Alfresco.util.encodeHTML(message);
				} else if (oRecord.getData("itemData")["prop_bcpg_lcaListIsFormulated"].value) {
					cssClass = "lca-formulated";
				}
			}

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="' + title + '">'
					+ (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue)
					+ (url != null ? '</a>' : '') + '</span>';
			}

			return '<span class="' + cssClass + '" title="' + title + '">' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:dynamicCharactValue",
		renderer: function(oRecord, data, label, scope) {
			if (data.value != null) {
				var error = oRecord.getData("itemData")["prop_bcpg_dynamicCharactErrorLog"].value;
				if (error == null) {
					var color = oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value;
					if (!color) {
						color = "000000";
					}
					// backward compatibility
					if (color.indexOf("#") < 0) {
						color = '#' + color;
					}

					if (data.value.indexOf && data.value.indexOf("\"comp\":") > -1) {
						var json = JSON.parse(data.value);
						if (json) {


							var ret = "", i = 0, refValue = null, className, currValue = null;
							json.comp.sort(function(a, b) {
								if (a.name == null) {
									return -1;
								} else if (b.name == null) {
									return 1;
								}
								return a.name.localeCompare(b.name);
							});

							for (i = 0; i < json.comp.length; i++) {
								if (json.comp[i].value) {
									if (i == 0) {
										refValue = beCPG.util.sigFigs(parseFloat(json.comp[i].value), 4);
										ret += '<span style="color:' + color + ';">' + beCPG.util.formatNumber(NUMBER_FORMAT, json.comp[i].value)
											+ '</span>';
									} else {
										currValue = beCPG.util.sigFigs(parseFloat(json.comp[i].value), 4);
										if (currValue != Number.NaN && refValue != Number.NaN) {
											if (refValue == currValue) {
												className = "dynaCompEquals";
											} else {
												className = (refValue < currValue) ? "dynaCompIncrease" : "dynaCompDecrease";
											}
										} else {
											className = "dynaCompNone";
										}
										ret += '<span  class="' + className + '" >(<a title="' + json.comp[i].name + '" href="'
											+ beCPG.util.entityURL(json.comp[i].siteId, json.comp[i].nodeRef, json.comp[i].itemType)
											+ '">' + beCPG.util.formatNumber(NUMBER_FORMAT, json.comp[i].value) + '</a>)</span>';
									}
								}
							}
							return ret;
						}
					}

					if (data.displayValue != null && data.displayValue.indexOf("@html") == 0) {
						return data.displayValue.replace("@html", "");
					}

					return '<span style="color:' + color + ';">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				}

				return '<span class="dyna' + data.value.replace("#", "") + '" title="' + Alfresco.util.encodeHTML(error) + '">'
					+ Alfresco.util.encodeHTML(error.substring(0, 7)) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:nrErrorLog",
		renderer: function(oRecord, data, label, scope) {
			if (data.value != null) {
				return '<span class="notificationError">' + Alfresco.util.encodeHTML(data.value) + '</span>';
			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:labelClaim",
		renderer: function(oRecord, data, label, scope) {

			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				var url = beCPG.util.entityURL(data.siteId, data.value);
				return '<span class="' + data.metadata + '">' + (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue) + (url != null ? '</a>' : '') + '</span>';
			}

			var isFormulated = oRecord.getData("itemData")["prop_bcpg_lclIsFormulated"] != null
				? oRecord.getData("itemData")["prop_bcpg_lclIsFormulated"].value : false;
			if (isFormulated) {
				var error = extractErrorMessage(oRecord.getData("itemData")["dt_bcpg_reqCtrlList"], "Labelclaim");
				if (error) {
					return '<span class="lcl-formulated-error" title="' + Alfresco.util.encodeHTML(error) + '">'
						+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				}
				var description = oRecord.getData("itemData")["dt_bcpg_lclLabelClaim"][0]["itemData"]["prop_cm_description"] != null
					? oRecord.getData("itemData")["dt_bcpg_lclLabelClaim"][0]["itemData"]["prop_cm_description"].displayValue : "";

				return '<span class="lcl-formulated"  title="' + Alfresco.util.encodeHTML(description) + '">'
					+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';

			}

			return '<span>' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:rclReqMessage",
		renderer: function(oRecord, data, label, scope) {

			if (scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed") > -1) {
				return data.displayValue;
			} else {
				var reqType = oRecord.getData("itemData")["prop_bcpg_rclReqType"].value;
				var reqProducts = oRecord.getData("itemData")["prop_bcpg_rclSourcesV2"];
				var html = "";
				html += '<div class="rclReq-details">';
				if (reqType) {
					html += '   <div class="icon" ><span class="reqType' + reqType + '" title="' +
						Alfresco.util.encodeHTML(scope.msg("data.reqtype." + reqType.toLowerCase())) + '">&nbsp;</span></div>';
				}
				html += '      <div class="rclReq-title">' + Alfresco.util.encodeHTML(data.displayValue) + '</div>';
				html += '      <div class="rclReq-content"><ul>';

				if (reqProducts) {
					for (var i in reqProducts) {
						var product = reqProducts[i], pUrl = beCPG.util.entityURL(product.siteId, product.value);

						if (product.metadata.indexOf("finishedProduct") != -1 || product.metadata.indexOf("semiFinishedProduct") != -1) {
							pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, "compoList");
						} else if (product.metadata.indexOf("packagingKit") != -1) {
							pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, "packagingList");
						}

						if (pUrl) {
							pUrl += "&bcPath=true&bcList=" + scope.datalistMeta.name;
						}

						html += '<li><span class="' + product.metadata + '" ><a href="' + pUrl + '">'
							+ Alfresco.util.encodeHTML(product.displayValue) + '</a></span></li>';

					}
				}
				+ '</ul></div>';
				html += '   </div>';
				html += '   <div class="clear"></div>';
				html += '</div>';

				return html;
			}
		}

	});





	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:dynamicCharactTitle",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

			var title = null;
			if (data.displayValue != null) {
				title = oRecord.getData("itemData")["prop_cm_title"].value;
				if (title == null || title.length == 0) {
					title = data.displayValue;
				}
			}

			var column = oRecord.getData("itemData")["prop_bcpg_dynamicCharactColumn"].value;

			if (column != null && column.length > 0) {

				if (!Dom.get(scope.id + "-colCheckbox").checked) {
					Dom.addClass(elCell.parentNode.parentNode, "hidden");
				}

				return "<b>" + Alfresco.util.encodeHTML(title) + "</b>";
			}

			return Alfresco.util.encodeHTML(title);
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:lrLocales",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null) {

				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				}
				if (oColumn.label == "") {
					oColumn.showAfterRenderSize = 16;
				}

				var country = data.value[0];

				if (country.indexOf("_") > 0) {
					country = country.split("_")[1].toLowerCase();
				}

				return '<img  title="' + scope.msg('locale.name.' + data.value[0]) + '" src="' + Alfresco.constants.URL_RESCONTEXT + '/components/images/flags/' + country + '.png" />';
			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:dynamicCharactGroupColor",
		renderer: function(oRecord, data, label, scope) {
			var color = oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value;
			if (!color) {
				color = "000000";
			}
			// backward compatibility
			if (color.indexOf("#") < 0) {
				color = '#' + color;
			}
			return '<div style="background-color:' + color
				+ ';width:15px;height:15px;border: 1px solid; border-radius: 5px;margin-left:15px;"></div></div>';
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:dynamicCharactColumn",
		renderer: function(oRecord, data, label, scope) {

			if (data.value != null && data.value.length > 0) {
				var title = oRecord.getData("itemData")["prop_cm_title"].value;
				if (title == null || title.length == "") {
					title = oRecord.getData("itemData")["prop_bcpg_dynamicCharactTitle"].value;
				}

				YAHOO.Bubbling.fire("columnRenamed", {
					columnId: "prop_" + data.value,
					label: title
				});
			}

			return null;
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:variantColumn1", "bcpg:variantColumn2", "bcpg:variantColumn3", "bcpg:variantColumn4", "bcpg:variantColumn5"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

			if (data.value != null) {
				if (oColumn.label == "") {
					var columnLabel = scope.msg("becpg.forms.variant") + " " + label.charAt(label.length - 1);
					YAHOO.Bubbling.fire("columnRenamed", {
						columnId: "prop_" + label.replace(":", "_"),
						label: columnLabel
					});
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				}
				if (scope.datalistMeta.name == "nutList") {
					return beCPG.util.sigFigs(data.value, 3).toLocaleString(beCPG.util.getJSLocale());
				}
				return data.displayValue;
			}
			return "";
		}
	});

	YAHOO.Bubbling.on("onDatalistColumnsReady", function(layer, args) {
		var obj = args[1];
		if (obj != null) {
			if (obj.entityDatagrid.entity && obj.entityDatagrid.entity.compareWithEntities
				&& obj.entityDatagrid.entity.compareWithEntities.length > 0) {

				var scope = obj.entityDatagrid;
				var oColumn = scope.widgets.dataTable.getColumn("prop_bcpg_compareWithDynColumn");
				if (oColumn != null) {
					var precColumn = obj.entityDatagrid.widgets.dataTable.getColumn(oColumn.getKeyIndex() - 1);
					var ind = "";
					scope.entity.compareWithEntities.sort(function(a, b) {
						return a.name.localeCompare(b.name);
					});

					for (var j = 0, jj = scope.entity.compareWithEntities.length; j < jj; j++) {
						ind += "'";
						var compareWithEntity = scope.entity.compareWithEntities[j];
						scope.widgets.dataTable.insertColumn({
							key: "dynCompareWith-" + compareWithEntity.nodeRef,
							label: "<span title='" + compareWithEntity.name + "' >" + precColumn.label + ind + "</span>",
							className: precColumn.className ? precColumn.className : "",
							sortable: true,
							editor: scope.rendererHelper.getCellEditor(scope, {
								dataType: "double",
								mandatory: true,
								fieldRef: precColumn.getField()
							}, scope.options.saveFieldUrl),
							formatter: function(elCell, oR, oC, oData) {
								if (oData != null) {
									elCell.innerHTML = oData.displayValue;
								}
								return null;
							}
						}, oColumn.getKeyIndex() + j + 1);
					}
				}
			}
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:compareWithDynColumn",
		renderer: function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
			if (data.value != null && data.value.indexOf && data.value.indexOf("\"comp\":") > -1) {
				var json = JSON.parse(data.value);
				if (json) {

					var numberFormat = NUMBER_FORMAT;
					var qtyColumn = scope.widgets.dataTable.getColumn("prop_bcpg_compoListQtySubFormula");

					if (qtyColumn.numberFormat) {
						numberFormat = qtyColumn.numberFormat;
					}


					for (var i = 0; i < json.comp.length; i++) {
						if (json.comp[i].value) {

							var newColumn = scope.widgets.dataTable.getColumn("dynCompareWith-" + json.comp[i].nodeRef);
							scope.widgets.dataTable.updateCell(oRecord, newColumn, {
								value: json.comp[i].value,
								displayValue: beCPG.util.formatNumber(numberFormat, json.comp[i].value),
								itemNodeRef: json.comp[i].itemNodeRef
							}, false);
						}
					}
				}
			}
			return null;
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:variantIds",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			var variants = data.value, isInDefault = !variants || variants.length < 1;

			if (data.value != null && data.value.length > 0) {

				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				}
				if (oColumn.label == "") {
					oColumn.showAfterRenderSize = 16;
				}
			}

			if (isInDefault) {
				return "<span  class='variant-common'>&nbsp;</span>";
			}

			var cssClass = "variant"
			var style = "";

			if (data.metadata != null) {
				var splitted = data.metadata.split(",")[0].split("#");
				cssClass = splitted[0];

				if (splitted.length > 0) {
					var variantColor = "#" + splitted[1];
					if (cssClass != "variant-default" && variantColor != null && variantColor.length > 0) {
						style = "style=\"background-color:" + variantColor + "\"";
					}
				}
			}
			if (oColumn.label != "") {
				return "<span " + style + " title=\"" + data.displayValue + "\" class='" + cssClass + "'>&nbsp;</span>&nbsp;" + data.displayValue;
			}

			return "<span " + style + " title=\"" + data.displayValue + "\" class='" + cssClass + "'>&nbsp;</span>";
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:startEffectivity", "bcpg:endEffectivity"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {


			if (scope.options.extraDataParams != null && scope.options.extraDataParams.indexOf("effectiveFilterOn=" + (!oColumn.hidden)) > 0) {
				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				} else {
					oColumn.hideAfterRender = true;
				}
			}

			if (label == "bcpg:startEffectivity") {
				var now = new Date();
				var past = false;
				var future = false;
				var startEffectivity = data.value;
				var endEffectivity = oRecord.getData("itemData")["prop_bcpg_endEffectivity"];

				if (startEffectivity != null && now.getTime() < Alfresco.util.fromISO8601(startEffectivity).getTime()) {
					future = true;
				}

				if (!future && endEffectivity != null && endEffectivity.value != null && now.getTime() > Alfresco.util.fromISO8601(endEffectivity.value).getTime()) {
					past = true;
				}

				if (past || future) {
					var elTr = scope.widgets.dataTable.getTrEl(elCell);
					if (past) {
						Dom.setStyle(elTr, 'background-color', "#ffebee");
					} else {
						Dom.setStyle(elTr, 'background-color', "#e8f5e9");
					}
					Dom.setStyle(elTr, "opacity", "0.5");
				}
			}

			if (data.value != null) {
				return Alfresco.util.formatDate(data.value, "shortDate");
			}
			return "";
		}
	});



	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "pack:labelingPosition",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null) {
				if (oColumn.hidden) {
					oColumn.showAfterRender = true;
				}
				if (oColumn.label == "") {
					oColumn.showAfterRenderSize = 16;
				}
				return "<span title=\"" + data.displayValue + "\" class='labeling-aspect'>&nbsp;</span>";
			}
			return "";
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:entityDataListState",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null) {
				if (oRecord._oData.itemData.prop_bcpg_entityDataListState && oRecord._oData.itemData.prop_bcpg_entityDataListState.value == "Valid") {
					if (oColumn.hidden) {
						oColumn.showAfterRender = true;
					}
					if (oColumn.label == "") {
						oColumn.showAfterRenderSize = 16;
					}
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
					return "<span class='locked'></span>";
				}
			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:illValue", "bcpg:illManualValue"],
		renderer: function(oRecord, data, label, scope) {

			var html = '', displayValue = data.displayValue,
				htmlId = "id-" + oRecord._sId, suffix = "bcpg_illValue",
				editPermission = oRecord.getData().permissions.userAccess.edit;

			if (data.value != null) {
				if (data.displayValue == null || data.displayValue == "") {
					displayValue = "<i>" + scope.msg("label.labelling.available") + "</i>";
				}
			} else {
				if (data.displayValue == null || data.displayValue == "") {
					displayValue = "<i>" + scope.msg("label.labelling.empty") + "</i>";
				}
			}


			html += '<div class="note rounded"><div id="' + htmlId + suffix + '" > ' + displayValue + '</div>';

			var nodeRef = oRecord._oData.nodeRef;
			html += '<div class="labeling-action-container">';

			if (label == "bcpg:illValue") {


				if (editPermission) {
					html += '<span id="' + htmlId + "#" + nodeRef + "#" + label + '" class="copyToIllManualValue"><a href="#" title="' + scope.msg("label.copy.to.illManualValue.title") + '" class="labeling-action copy-to"></a> </span>';
					html += '<span>&nbsp;</span>';
				}
				html += '<span id="' + htmlId + "#" + nodeRef + "#" + suffix + '" class="onCopyToClipboard"><a href="#" title="' + scope.msg("label.copy.to.clipboard.title") + '" class="labeling-action clipboard"></a> </span>';
				html += '<span>&nbsp;&nbsp;</span>';
			}
			if (label == "bcpg:illManualValue") {
				if (editPermission) {
					html += '<span id="' + htmlId + "#" + nodeRef + "#" + label + "#false" + '" class="onShowTranslation"><a href="#" title="' + scope.msg("label.edit.translation.title") + '" class="labeling-action edit-translation"></a> </span>';
					html += '<span>&nbsp;</span>';
				}
				html += '<span id="' + htmlId + "#" + nodeRef + "#" + label + "#diff-bcpg:illValue" + '" class="onShowTranslation"><a href="#" title="' + scope.msg("label.show.diff.title") + '" class="labeling-action show-diff"></a> </span>';
				html += '<span>&nbsp;&nbsp;</span>';

			}


			if (data.value != null) {
				html += '<span id="' + htmlId + "#" + nodeRef + "#" + label + "#true" + '" class="onShowTranslation"><a href="#" title="' + scope.msg("label.show.translation.title") + '" class="labeling-action show-translation" ></a></span>';
				html += '</div>';
			}


			html += '</div>';
			return html;
		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["ecm:cclSourceValue", "ecm:cclTargetValue"],
		renderer: function(oRecord, data, label, scope) {
			if (data.displayValue != null && data.displayValue.length > 0) {
				return data.displayValue;
			}
			return "";
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["ecm:wulLink"],
		renderer: function(oRecord, data, label, scope) {
			var additionalProps = Object.entries(oRecord.getData("itemData")["dt_ecm_wulLink"][0].itemData);

			var ret = "";

			var columns = null;

			for (var i = 0; i < scope.datalistColumns.length; i++) {
				if (scope.datalistColumns[i].name == "ecm:wulLink" && scope.datalistColumns[i].type == "entity") {
					columns = scope.datalistColumns[i].columns;
					break;
				}
			}

			for (var i = 0; i < additionalProps.length; i++) {
				var propName = additionalProps[i][0];
				var propValue = additionalProps[i][1];
				var label = null;
				if (propName.startsWith("prop_")) {
					if (propValue && propValue.displayValue) {
						for (var j = 0; j < columns.length; j++) {
							if (columns[j].name == propName.replace("prop_", "").replace("_", ":")) {
								label = columns[j].label;
								break;
							}
						}
						ret += label + ": " + propValue.displayValue + " ";
					}
				}
			}

			return ret;
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "boolean_bcpg:lrIsActive",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (oColumn.hidden) {
				oColumn.showAfterRender = true;
			}
			if (oColumn.label == "") {
				oColumn.showAfterRenderSize = 16;
			}
			if (data.value) {
				return "<span  class='rule-enabled'>&nbsp;</span>";
			}
			return "<span  class='rule-disabled'>&nbsp;</span>";
		}
	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:dynamicCharactColumn1", "bcpg:dynamicCharactColumn2", "bcpg:dynamicCharactColumn3", "bcpg:dynamicCharactColumn4",
			"bcpg:dynamicCharactColumn5", "bcpg:dynamicCharactColumn6", "bcpg:dynamicCharactColumn7", "bcpg:dynamicCharactColumn8",
			"bcpg:dynamicCharactColumn9", "bcpg:dynamicCharactColumn10"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {


			if (!oColumn.numberFormat) {
				oColumn.numberFormat = NUMBER_FORMAT;
			}

			if (oRecord.getData("itemData")["isMultiLevel"]) {

				var parts = oRecord.getData("itemData")["path"].split("/");
				var lastPart = parts[parts.length - 1];

				if (scope.subCompCache != null && scope.subCompCache["idx_" + oColumn.getKeyIndex() + lastPart] != null) {
					return scope.subCompCache["idx_" + oColumn.getKeyIndex() + lastPart];
				}
				if (scope.subCache != null && scope.subCache["idx_" + oColumn.getKeyIndex()] != null) {
					for (var j = 0; j < scope.subCache["idx_" + oColumn.getKeyIndex()].length; j++) {
						var path = scope.subCache["idx_" + oColumn.getKeyIndex()][j].path;
						if (path == oRecord.getData("itemData")["path"] && scope.subCache["idx_" + oColumn.getKeyIndex()][j].value && scope.subCache["idx_" + oColumn.getKeyIndex()][j].value != null) {
							return beCPG.util.formatNumber(oColumn.numberFormat, scope.subCache["idx_" + oColumn.getKeyIndex()][j].value);
						}
					}
				}
				return "";
			}

			if (data.value && data.value.indexOf && data.value.indexOf("\"comp\":") > -1) {
				var json = JSON.parse(data.value);
				if (json) {
					var ret = "", z = 0, refValue = null, className, currValue = null;
					json.comp.sort(function(a, b) {
						if (a.name == null) {
							return -1;
						} else if (b.name == null) {
							return 1;
						}
						return a.name.localeCompare(b.name);
					});
					var refTab = [];
					for (z = 0; z < json.comp.length; z++) {
						var compareItem = json.comp[z];
						if (compareItem) {
							if (compareItem.value) {
								if (z == 0) {
									refValue = beCPG.util.sigFigs(parseFloat(compareItem.value), 4);
									ret += '<span>' + beCPG.util.formatNumber(oColumn.numberFormat, compareItem.value) + '</span>';
								} else {
									ret += extractComparison(refValue, compareItem, oColumn);
								}
							}
							if (compareItem.sub) {
								if (!scope.subCompCache) {
									scope.subCompCache = [];
								}
								for (var cx = 0; cx < compareItem.sub.length; cx++) {
									var subItem = compareItem.sub[cx];
									if (z == 0) {
										refTab[cx] = subItem.value;
									} else {
										var value = refTab[cx];
										var compValue = '<span>' + beCPG.util.formatNumber(oColumn.numberFormat, value) + '</span>';
										value = beCPG.util.sigFigs(parseFloat(compareItem.value), 4);
										compValue += extractComparison(value, subItem, oColumn);

										var parts = subItem.path.split("/");
										var lastPart = parts[parts.length - 1];
										scope.subCompCache["idx_" + oColumn.getKeyIndex() + lastPart] = compValue;
									}
								}
							}
						}
					}
					return ret;
				}
			}

			if (data.value != null) {
				if (!oRecord.getData("itemData")["isMultiLevel"]) {
					if (data.value.indexOf && data.value.indexOf("\"sub\":") > -1) {
						var json = JSON.parse(data.value);
						if (json) {
							if (!scope.subCache) {
								scope.subCache = [];
							}
							scope.subCache["idx_" + oColumn.getKeyIndex()] = json.sub;
							if (json.value && json.value != null) {
								return beCPG.util.formatNumber(oColumn.numberFormat, json.value);
							}
							return "";
						}

					}
				}
				if (data.value != null) {
					return beCPG.util.formatNumber(oColumn.numberFormat, data.value);
				}
			}
			return "";

		}

	});

	function extractComparison(refValue, comparisonItem, oColumn) {
		var currValue = beCPG.util.sigFigs(parseFloat(comparisonItem.value), 4);
		var className = null;
		if (currValue != Number.NaN && refValue != Number.NaN) {
			if (refValue == currValue) {
				className = "dynaCompEquals";
			} else {
				className = (refValue < currValue) ? "dynaCompIncrease" : "dynaCompDecrease";
			}
		} else {
			className = "dynaCompNone";
		}
		return '<span  class="' + className + '" >(<a title="' + comparisonItem.name + '" href="'
			+ beCPG.util.entityURL(comparisonItem.siteId, comparisonItem.nodeRef, comparisonItem.itemType) + '">'
			+ beCPG.util.formatNumber(oColumn.numberFormat, comparisonItem.value) + '</a>)</span>';
	}

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:compoListQty",
		renderer: function(oRecord, data, label, scope) {
			var qty = "";
			if (data.value != null) {
				var unit = "";
				if (Alfresco.constants.JS_LOCALE == "en_US") {
					var absValue = Math.abs(data.value * 2.204622622);
					var compoUnit = oRecord.getData("itemData")["prop_bcpg_compoListUnit"] != null ?
						oRecord.getData("itemData")["prop_bcpg_compoListUnit"].value : "";
					if ((absValue < 1 && compoUnit != "lb")
						|| compoUnit == "oz") {
						qty = data.value * 35.27396195;
						unit = " oz";
					} else {
						qty = data.value * 2.204622622;
						unit = " lb";
					}
				} else {
					var absValue = Math.abs(data.value);
					if (absValue < 0.0000001) {
						qty = data.value * 1000000000;
						unit = " µg";
					} else if (absValue < 0.0001) {
						qty = data.value * 1000000;
						unit = " mg";
					} else if (absValue < 1) {
						qty = data.value * 1000;
						unit = " g";
					} else if (absValue > 1000) {
						qty = data.value / 1000;
						unit = " t";
					} else {
						qty = data.value;
						unit = " kg";
					}
				}

				qty = beCPG.util.sigFigs(qty, 4).toLocaleString(beCPG.util.getJSLocale()) + unit;
			}

			return Alfresco.util.encodeHTML(qty);
		}
	});



	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:instruction"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null && data.value.length > 0) {
				if (oColumn.label == "") {

					if (oColumn.hidden) {
						oColumn.showAfterRender = true;
					}
					if (oColumn.label == "") {
						oColumn.showAfterRenderSize = 16;
					}

					return "<span title=\"" + data.displayValue.replace(/&nbsp;/gi, " ")
						.replace(/<(?:.|\n)*?>/gm, '').replace(/\n/gm, " ").replace(/"/gm, "")
						+ "\" class='instructions'>&nbsp;</span>";
				} else {
					return data.displayValue;
				}

			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:glopValue"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data && data.value != null && data.value.length > 0) {
				var glopValue = JSON.parse(data.value);

				if (glopValue.status == "optimal") {
					return "<span style=\"color:green\">" + glopValue.value + "</span>";
				} else if (glopValue.status == "suboptimal") {
					return "<span style=\"color:orange\">" + glopValue.value + "</span>";
				}
			}

			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:lclComments"],
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null && data.value.length > 0) {
				return data.displayValue;
			}
			return "";
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "cm:cmobject_bcpg:lclMissingLabelClaims",
		renderer: function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

			var missingSources = oRecord.getData("itemData")["assoc_bcpg_lclMissingLabelClaims"];

			//only put text on first result
			if (data.value != null && data.value.length > 0 && missingSources.length > 0 && i == 0) {
				var description = scope.msg("becpg.forms.help.lclMissingSources", missingSources.length);
				var tooltip = "";

				for (var source in missingSources) {
					tooltip += missingSources[source].displayValue + "\n";

				}

				var tooltipText = scope.msg("becpg.forms.help.lclMissingSources.list", tooltip);

				return "<span class=\"lcl-formulated-error\" title=\"" + tooltipText + "\">" + description + "</span>";
			}
			return "";
		}
	});

	function extractErrorMessage(reqCtrlList, rclDataType) {

		var errorMessage = "";

		if (reqCtrlList) {
			for (var i in reqCtrlList) {
				var reqCtrl = reqCtrlList[i];

				var type = reqCtrl["itemData"]["prop_bcpg_rclDataType"];

				if (type && type.value == rclDataType && reqCtrl["itemData"]["prop_bcpg_rclReqMessage"]) {
					errorMessage += reqCtrl["itemData"]["prop_bcpg_rclReqMessage"].value;

					var sources = reqCtrl["itemData"]["prop_bcpg_rclSourcesV2"];

					if (sources && sources.length > 0) {
						errorMessage += ": ";
						for (var index in sources) {
							if (index >= 5) {
								errorMessage += "...";
							} else {
								var source = sources[index];
								if (index == 0) {
									errorMessage += source.displayValue;
								} else {
									errorMessage += ("," + source.displayValue);
								}
							}
						}
					}
				}
			}
		}

		return errorMessage;
	}

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:compoListVolume",
		renderer: function(oRecord, data, label, scope) {

			var qty = "";
			if (data.value != null) {
				var unit = "";
				if (data.value < 1) {
					qty = data.value * 1000;
					unit = " mL";
				} else {
					qty = data.value;
					unit = " L";
				}

				qty = beCPG.util.sigFigs(qty, 4).toLocaleString(beCPG.util.getJSLocale()) + unit;
			}

			return Alfresco.util.encodeHTML(qty);
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:compoListDeclType", "bcpg:packagingListPkgLevel"],
		renderer: function(oRecord, data, label, scope) {
			if (data.displayValue != null) {
				var msgKey = "data." + label.replace(":", "_").toLowerCase() + "." + data.displayValue.toLowerCase(), msgValue = scope.msg(msgKey);
				if (msgKey != msgValue) {
					return Alfresco.util.encodeHTML(msgValue);
				}
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}
	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:regulatoryUsageRef"],
		renderer: function(oRecord, data, label, scope) {

			var url;

			if (data.displayValue) {
				var usages = oRecord.getData("itemData")["dt_bcpg_regulatoryUsageRef"];
				var countries = oRecord.getData("itemData")["dt_bcpg_regulatoryCountries"];
				if (oRecord.getData("itemData")["prop_bcpg_regulatoryRecipeId"]) {
					var recipeId = oRecord.getData("itemData")["prop_bcpg_regulatoryRecipeId"].value;
				}
				if (usages && countries) {
					var countriesParam = "";
					for (var i in countries) {
						var country = countries[i];
						countriesParam += ("&countries=" + country.itemData["prop_bcpg_regulatoryCode"].value);
					}
					for (var i in usages) {
						var usage = usages[i];
						if (usage.nodeRef == data.value) {
							var usageID = usage.itemData["prop_bcpg_regulatoryId"].value;
							if (recipeId) {
								url = "https://formula.decernis.com/recipes-analysis/analyze?recipe=" + recipeId + "&moduleId=1&removeRecipe=false&usage=" + usageID + countriesParam;
							}
						}
					}
				}
			}

			return (url != null ? '<a href="' + url + '">' : '') + Alfresco.util.encodeHTML(data.displayValue);
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:irlCitation", "bcpg:irlRestrictionLevels", "bcpg:irlResultIndicator", "bcpg:irlUsages", "bcpg:irlPrecautions"],
		renderer: function(oRecord, data, label, scope) {

			if (data.displayValue) {
				var parts = data.displayValue.split(";;");
				var displayedData = "";
				for (var i in parts) {
					var part = parts[i];
					if (label != "bcpg:irlUsages" && label != "bcpg:irlPrecautions") {
						var subParts = part.split(" :: ");
						part = "<b>" + Alfresco.util.encodeHTML(subParts[0]) + "</b>" + " : " + Alfresco.util.encodeHTML(subParts[1]);
					}
					displayedData += "- " + part;
					if (i != parts.length - 1) {
						displayedData += "<br>";
					}
				}
				return displayedData;
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: ["bcpg:nutListGDAPerc"],
		renderer: function(oRecord, data, label, scope) {

			var percentValue = data.value;
			var nutColor = null;

			if (oRecord.getData("itemData")["dt_bcpg_nutListNut"] &&
				oRecord.getData("itemData")["dt_bcpg_nutListNut"].length > 0) {
				nutColor = oRecord.getData("itemData")["dt_bcpg_nutListNut"][0].color;
			}


			if (percentValue !== null && percentValue > 0 && nutColor != null && nutColor !== undefined) {

				var additionalProps = oRecord.getData("itemData")["dt_bcpg_nutListNut"][0].itemData;
				var nutValue = oRecord.getData("itemData")["prop_bcpg_nutListValue"].displayValue;
				var gda = additionalProps.prop_bcpg_nutGDA.value;
				var ul = additionalProps.prop_bcpg_nutUL.value;

				if (oRecord.getData("itemData")["prop_bcpg_nutListRoundedValue"]
					&& oRecord.getData("itemData")["prop_bcpg_nutListRoundedValue"].value != null) {
					var key = beCPG.util.getRegulatoryCountryKey(Alfresco.constants.JS_LOCALE);
					var jsonData = JSON.parse(oRecord.getData("itemData")["prop_bcpg_nutListRoundedValue"].value);
					if (jsonData.ul && jsonData.ul[key]) {
						ul = jsonData.ul[key];
						percentValue = jsonData.gda[key];
					}
				}

				var unit = additionalProps.prop_bcpg_nutUnit.displayValue;

				var ulExceeded = false;
				var red = "#F44336";
				var gray = "#cccccc";

				if (nutValue !== null && ul !== null && (nutValue > ul)) {
					ulExceeded = true;
					nutColor = red;
				}

				var hexColorSplit = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(nutColor);
				var rgbColor = hexColorSplit ? {
					r: parseInt(hexColorSplit[1], 16),
					g: parseInt(hexColorSplit[2], 16),
					b: parseInt(hexColorSplit[3], 16)
				} : null;

				var emptyBarColor = null;

				//background of unfilled part of progress bar
				if (percentValue < 100) {
					emptyBarColor = ulExceeded ? red : gray;
				}

				var fontColor = "black";

				if (rgbColor && (rgbColor.r * 0.2126 + rgbColor.g * 0.7152 + rgbColor.b * 0.0722) < 186 && percentValue > 20) {
					fontColor = "white";
				}

				var gdaReminder = null;
				if (gda !== null && unit !== null) {
					gdaReminder = scope.msg("becpg.forms.help.gda-reminder", gda, unit);
				}

				var reminderColor = "black";

				if (percentValue > 80 || ulExceeded) {
					reminderColor = fontColor;
				}

				var html = "<div class=\"progress-bar\" " + (emptyBarColor != null ? "style=\"background-color: " + emptyBarColor + ";\" " : "") + " title=\"" + percentValue.toFixed(1) + "% " + gdaReminder + (ulExceeded ? "\n" + scope.msg("becpg.forms.help.ul-exceeded") : "") + "\";\">";
				if (gdaReminder !== null) {
					html += "<div class =\"outer-progress-bar\" style=\"float: right; color: " + reminderColor + "\">" + gdaReminder + "</div>";
				}
				html += "<div class =\"inner-progress-bar\" style=\"width: " + Math.min(percentValue, 100) + "%; background-color: " + nutColor + (percentValue < 100 ? ";" : "") + ";\">";
				html += "	<div  style=\"color: " + fontColor + "; white-space: nowrap;\">" + percentValue.toFixed(1) + "%</div>";
				html += "</div>";
				html += "</div>";


				return html;
			} else if (percentValue !== null && percentValue > 0) {

				if (oColumn.numberFormat) {
					return beCPG.util.formatNumber(oColumn.numberFormat, percentValue) + " %";
				}

				return Alfresco.util.encodeHTML(beCPG.util.sigFigs(percentValue, 1).toLocaleString(beCPG.util.getJSLocale()) + " %");
			} else {
				return "";
			}
		}
	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "mltext_survey:questionLabel",
		renderer: function(oRecord, data, label, scope) {
			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
				return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">' + Alfresco.util
					.encodeHTML(data.displayValue) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);

		}

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName: "bcpg:reqCtrlList",
		renderer: function(oRecord, data, label, scope, idx, length, elCell, oColumn) {
			var oData = oRecord.getData();
			if (data["itemData"]) {

				if (idx == 0) {

					var reqCtrlList = oRecord.getData("itemData")["dt_bcpg_reqCtrlList"];

					var reqHtlm = "<ul>";

					for (j in reqCtrlList) {
						var reqCtrl = reqCtrlList[j];
						var rclDataType = reqCtrl["itemData"]["prop_bcpg_rclDataType"].value;

						if (rclDataType == "Specification") {

							Dom.removeClass(elCell.parentNode, "yui-dt-hidden");

							YAHOO.Bubbling.fire("columnRenamed", {
								columnId: "dt_bcpg_reqCtrlList",
								label: scope.msg("becpg.forms.field.compliance.label")
							});

							var desc = "";

							var reqType = reqCtrl["itemData"]["prop_bcpg_rclReqType"].value;

							desc += '<div class="rclReq-details">';
							if (reqType) {
								desc += '<span class="reqType' + reqType + '" title="'
									+ Alfresco.util.encodeHTML(scope.msg("data.reqtype." + reqType.toLowerCase())) + '">&nbsp;</span>';
							}
							if (reqCtrl["itemData"]["prop_bcpg_regulatoryCode"] && reqCtrl["itemData"]["prop_bcpg_regulatoryCode"].value != null && reqCtrl["itemData"]["prop_bcpg_regulatoryCode"].value.length > 1) {
								var regulatoryCode = reqCtrl["itemData"]["prop_bcpg_regulatoryCode"].value;
								desc += '      <span class="rclReq-regulatoryCode" title="'
									+ beCPG.util.encodeAttr(reqCtrl["itemData"]["prop_bcpg_rclReqMessage"].displayValue.replace(regulatoryCode, "")) + '"  >'
									+ Alfresco.util.encodeHTML(regulatoryCode);
								if (reqCtrl["itemData"]["prop_bcpg_rclReqMaxQty"] && reqCtrl["itemData"]["prop_bcpg_rclReqMaxQty"].value != null) {
									desc += " (" + reqCtrl["itemData"]["prop_bcpg_rclReqMaxQty"].displayValue + " %)";
								}

								desc += '</span>';
							} else {
								desc += '      <span class="rclReq-title">'
									+ Alfresco.util.encodeHTML(reqCtrl["itemData"]["prop_bcpg_rclReqMessage"].displayValue) + '</span>';
							}
							desc += "</div>";

							reqHtlm += "<li>" + desc + "</li>";
						}

					}
					reqHtlm += "</ul>";

					return reqHtlm;

				}

				return null;


			}
		}
	});

}
