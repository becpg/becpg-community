/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
	 * ProjectDetails constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.ProjectDetails} The new ProjectDetails instance
	 * @constructor
	 */
	beCPG.component.ProjectDetails = function(htmlId) {
	beCPG.component.ProjectDetails.superclass.constructor.call(this,
				"beCPG.component.ProjectDetails", htmlId, [ "button",
						"container" ]);
	
		YAHOO.Bubbling.addDefaultAction("filter-details-action", this.onFilterDetailsAction);
	
		return this;
	};


	   YAHOO
	         .extend(
	        	   beCPG.component.ProjectDetails,
	               Alfresco.component.Base,
	               {
				/**
				 * Object container for initialization options
				 * 
				 * @property options
				 * @type object
				 */
				options : {
					/**
					 * Current entityNodeRef.
					 * 
					 * @property entityNodeRef
					 * @type string
					 * @default ""
					 */
					entityNodeRef : "",
					graphData : []
				},

				onReady : function projectGraph() {
				var me = this;
				 require(dojoConfig,["bccc"], function(pvc){
						var relational_01_ds = {
	
							"resultset" : me.options.graphData,
							"metadata" : [ {
								"colIndex" : 0,
								"colType" : "String",
								"colName" : me.msg("projectdetails.graph.xaxis")
							}, {
								"colIndex" : 1,
								"colType" : "Numeric",
								"colName" : me.msg("projectdetails.graph.yaxis")
							} ]
						};
						new pvc.StackedAreaChart({
	
							canvas : me.id+"-chart",
							width : 400,
							height : 150,
							animate : true,
							selectable : false,
							hoverable : true,
							tooltipEnabled : false,
							axisGrid : true,
							nullInterpolationMode : 'linear',
							extensionPoints : {
								area_interpolate : 'cardinal',
								axisGrid_strokeStyle : 'lightgray'
							},
	
							orthoAxisVisible : true,
							orthoAxisTitle : me.msg("projectdetails.graph.yaxis"),
							baseAxisVisible : true,
							colors : [ 'rgba(107, 210, 169, 0.6)' ],
	
							focusWindowBaseConstraint : function(oper) {
					
								if (oper.type === 'new'
										&& oper.length0 === oper.length) {
									var len = (oper.max - oper.min) / 4;
									var middle = ((+oper.max) + (+oper.min)) / 2;
									oper.value = new Date(middle - len / 2);
									oper.length = len;
								}
								var tim = pvc.time;
								var interval = tim.weekday;
	
								var minLen = tim.intervals.d;
								var sign = oper.target === 'end' ? -1 : +1;
								var t0 = +oper.value;
	
								var t = interval.closestOrSelf(tim
										.withoutTime(oper.value));
								oper.min = interval.previousOrSelf(tim
										.withoutTime(oper.minView));
								oper.max = interval.previousOrSelf(tim
										.withoutTime(oper.maxView));
								if (oper.type === 'new') {
									oper.value = t;
									oper.length = Math.max(oper.length, minLen);
									return;
								}
								var l = +oper.length;
								var o = t0 + sign * l;
								l = sign * (o - t);
								if (l < minLen) {
									t = o - sign * minLen;
								}
								oper.value = t;
							},
	
						}).setData(relational_01_ds, {
							crosstabMode : false
						}).render();
					});
						
				},

				onFilterDetailsAction : function(layer,	args) {

					var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
					if (owner !== null) {
							args[1].stop = true;
							var fieldId = owner.id;
							var idSpan0 = fieldId.split("#")[0], idSpan1 = fieldId
									.split("#")[1];
							var filteredDivEls = Dom.getElementsByClassName(idSpan0);

							for (var i in filteredDivEls) {

								Dom.removeClass(filteredDivEls[i], "hidden");
								if (!Dom.hasClass(filteredDivEls[i], idSpan1)) {
									Dom.addClass(filteredDivEls[i], "hidden");
								}
							}
					}
					
				},

			});

})();