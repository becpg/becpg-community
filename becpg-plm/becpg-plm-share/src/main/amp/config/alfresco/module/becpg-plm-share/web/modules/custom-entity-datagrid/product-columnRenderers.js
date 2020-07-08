/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:product", "bcpg:supplier", "bcpg:client", "bcpg:entityV2", "bcpg:resourceProduct",
				"cm:content_bcpg:costDetailsListSource", "bcpg:product_bcpg:packagingListProduct", "bcpg:product_bcpg:compoListProduct",
				"ecm:wulSourceItems", "ecm:rlSourceItems","bcpg:psclSourceItem" , "ecm:rlTargetItem", "ecm:culSourceItem", "ecm:culTargetItem", "ecm:cclSourceItem"
				,"cm:cmobject_bcpg:lrComponents" ],
		renderer : function(oRecord, data, label, scope, z, zz, elCell, oColumn) {

			var url = beCPG.util.entityURL(data.siteId, data.value), version = "";
			var toogleGroupButton = null;
			var padding = 0;

			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
				 padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
			}
			
			if (label == "mpm:plProduct" || label == "bcpg:compoListProduct" || label == "bcpg:packagingListProduct" || label == "mpm:plResource") {
				// datalist
				if (data.metadata.indexOf("finishedProduct") != -1 || data.metadata.indexOf("semiFinishedProduct") != -1) {
					url = beCPG.util.entityURL(data.siteId, data.value,null,null,"compoList");
				} else if (data.metadata.indexOf("packagingKit") != -1) {
				    url = beCPG.util.entityURL(data.siteId, data.value,null,null,"packagingList");
				} else if (data.metadata.indexOf("localSemiFinishedProduct") != -1) {
					url = null;
				}
				if (data.version && data.version !== "") {
					version = '<span class="document-version">' + data.version + '</span>';
				}
				if(url!=null){
					url+="&bcPath=true&bcList="+scope.datalistMeta.name;
				}
				
				if(false === oRecord.getData("itemData")["isLeaf"]){
					toogleGroupButton = '<div id="group_'+( oRecord.getData("itemData")["open"]?"expanded":"collapsed")+'_'+ oRecord.getData("nodeRef")+'" style="margin-left:' + padding
							+ 'px;" class="onCollapsedAndExpanded" ><a href="#" class="'+scope.id + '-action-link"><span class="gicon ggroup-'
							+( oRecord.getData("itemData")["open"]?"expanded":"collapsed")+'"></span></a></div>';
				} else if( true === oRecord.getData("itemData")["isLeaf"] ){
					padding +=25;
				}
			}
			
			if(label == "mpm:rplResourceRef"){
			    if (oColumn.hidden) {
                    scope.widgets.dataTable.showColumn(oColumn);
                    Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
                }
			}
			
			return (toogleGroupButton!=null ? toogleGroupButton : '')+'<span class="' + data.metadata + '" '+(toogleGroupButton==null && padding!=0 ?'style="margin-left:' + padding + 'px;"':'')+'>'
						+(url!=null?'<a href="' + url + '">':'') 
						+ Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+'</span>' + version;
			
		}

	});
	

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "boolean_bcpg:allergenListVoluntary", "boolean_bcpg:allergenListInVoluntary",
				"boolean_bcpg:packagingListIsMaster", "boolean_ecm:culTreated", "boolean_ecm:isWUsedImpacted" ],
		renderer : function(oRecord, data, label, scope) {
			if (data.value) {
				return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}
			return Alfresco.util.encodeHTML(data.displayValue);
		}

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : [ "bcpg:lclClaimValue" ],
        renderer : function(oRecord, data, label, scope) {
            if ("true" === data.value) {
                return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
            } else if("suitable" === data.value){
            	return '<span class="blue">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
            }
            return Alfresco.util.encodeHTML(data.displayValue);
        }

    });
	

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:allergenListDecisionTree",
		renderer : function(oRecord, data, label, scope) {
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
		propertyName : [ "cm:cmobject_bcpg:allergenListVolSources", "cm:cmobject_bcpg:allergenListInVolSources" ],
		renderer : function(oRecord, data, label, scope) {
			if (data.metadata == "ing") {
				return '<span class="' + data.metadata + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}
			return '<span class="' + data.metadata + '" ><a href="' + beCPG.util.entityURL(data.siteId, data.value) + '">'
					+ Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:rclReqType", "bcpg:filReqType", "ecm:culReqType" ],
		renderer : function(oRecord, data, label, scope) {
			if(data.value!=null && data.value.length>0){
			    return '<span class="reqType'+data.value+'">' + Alfresco.util.encodeHTML( scope.msg("data.reqtype."+ data.value.toLowerCase())) + '</span>';
			}	
			return Alfresco.util.encodeHTML(data.displayValue);	
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "ecm:rlRevisionType", "ecm:culRevision" ],
		renderer : function(oRecord, data, label, scope) {

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
		propertyName : [ "qa:slControlPoint" ],
		renderer : function(oRecord, data, label, scope) {
			var url = beCPG.util.entityURL(data.siteId, data.value);
			return '<span class="controlPoint"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "qa:clCharacts" ],
		renderer : function(oRecord, data, label, scope) {
			var url = scope._buildCellUrl(data);
			return '<span class="' + data.metadata + '">'+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';

		}

	});

	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "qa:clState","qa:slSampleState" ],
		renderer : function(oRecord, data, label, scope) {
			if(data.value!=null){
				if(data.value=="Compliant"){
					return '<span class="green">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				} else if(data.value=="NonCompliant"){
					return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				}
				
			}
			return  Alfresco.util.encodeHTML(data.displayValue);
		}

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:allergen",  "bcpg:ing", "bcpg:geoOrigin", "bcpg:bioOrigin", "bcpg:geo", "bcpg:microbio", "bcpg:organo","bcpg:listValue" ],
		renderer : function(oRecord, data, label, scope) {
			var url = null;
			if(scope.datalistMeta && scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed")>-1){
				url = beCPG.util.entityURL(data.siteId, data.value);
			}
			
			if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
				var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
				return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">'+(url!=null?'<a href="' + url + '">':'') 
				+ Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+'</span>';
			}
			
			return '<span class="' + data.metadata + '" >'+(url!=null?'<a href="' + url + '">':'') + Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+'</span>';
		}

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : "bcpg:physicoChem",
        renderer : function(oRecord, data, label, scope) {
        	
        	var url = null;
			if(scope.datalistMeta && scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed")>-1){
				url = beCPG.util.entityURL(data.siteId, data.value);
			}
            
            var title = Alfresco.util.encodeHTML(data.metadata);
            var cssClass = data.metadata;
            var isFormulated = oRecord.getData("itemData")["prop_bcpg_physicoChemIsFormulated"].value;
            var error = oRecord.getData("itemData")["prop_bcpg_physicoChemFormulaErrorLog"].value;
            if(error != null){
           	 cssClass= "physicoChem-formulated-error";
               title = Alfresco.util.encodeHTML(error);
            } else if(isFormulated){
           	 cssClass= "physicoChem-formulated";
            }
            
            if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
                var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
                return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="'+title+'">' 
                +(url!=null?'<a href="' + url + '">':'') + Alfresco.util.encodeHTML(data.displayValue)
                        + (url!=null?'</a>':'')+ '</span>';
            }

            return '<span class="' + cssClass + '" title="'+title+'">' +(url!=null?'<a href="' + url + '">':'') + Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+ '</span>';
        }

    });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : "bcpg:nut",
        renderer : function(oRecord, data, label, scope) {
        	
        	var url = null;
			if(scope.datalistMeta && scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed")>-1){
				url = beCPG.util.entityURL(data.siteId, data.value);
			}
            
            var title = Alfresco.util.encodeHTML(data.metadata);
            var cssClass = data.metadata;
            if(oRecord.getData("itemData")["prop_bcpg_nutListIsFormulated"]){
	            if( oRecord.getData("itemData")["prop_bcpg_nutListFormulaErrorLog"]  &&  oRecord.getData("itemData")["prop_bcpg_nutListFormulaErrorLog"].value  != null){
	           	 cssClass= "nut-formulated-error";
	               title = Alfresco.util.encodeHTML( oRecord.getData("itemData")["prop_bcpg_nutListFormulaErrorLog"].value );
	            } else if(oRecord.getData("itemData")["prop_bcpg_nutListIsFormulated"].value){
	           	 cssClass= "nut-formulated";
	            }
            }
            
            if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
                var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
                return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="'+title+'">' 
                +(url!=null?'<a href="' + url + '">':'') +  Alfresco.util.encodeHTML(data.displayValue)
                        + (url!=null?'</a>':'')+ '</span>';
            }

            return '<span class="' + cssClass + '" title="'+title+'">' +(url!=null?'<a href="' + url + '">':'') +  Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+ '</span>';
        }

    });
	
	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "bcpg:nutListValue",
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
          var ret = "";
          
          function exp(val){
        	  if(val == 0){
        		  return "0";
        	  }  else if(Math.abs(val) < 0.000001){
        		  return beCPG.util.sigFigs(val*1000000,3).toLocaleString()+"×10<sup>-6</sup>";
        	  } else if(Math.abs(val) < 0.01){
        		  return beCPG.util.sigFigs(val*1000,3).toLocaleString()+"×10<sup>-3</sup>";
        	  } else if(Math.abs(val) >= 1000000){
        		  return beCPG.util.sigFigs(val/1000000,3).toLocaleString()+"×10<sup>6</sup>";
        	  }
        	  return beCPG.util.sigFigs(val,3).toLocaleString();
          }
          
          var unit = oRecord._oData.itemData.prop_bcpg_nutListUnit.value;
          
          if(oColumn.label!=null && oColumn.label.indexOf && oColumn.label.indexOf("100g")>0){
        	  unit = unit.replace("/100g","");
          }
          
          if (data.value != null) {
        	  ret+=exp(data.value)+" "+unit;
        	  
          }
          
          var formulatedValue = oRecord.getData("itemData")["prop_bcpg_nutListFormulatedValue"];
          if(formulatedValue!=null && formulatedValue.value!=null ){
              if(ret.length>0){
                  ret+= '&nbsp;&nbsp;(' + exp(formulatedValue.value) + ')';
              } else {
                ret+= exp(formulatedValue.value)+" "+unit ;
              }
          }
          
         return ret;
      }

  });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : ["bcpg:nutListMini", "bcpg:nutListMaxi", "bcpg:nutListValuePerServing"],
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
      	if(data.value != null){
      		return Alfresco.util.encodeHTML(beCPG.util.sigFigs(data.value,3).toLocaleString());
      	}      
      	return "";
      }
  });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "bcpg:nutListGDAPerc",
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
      	if(data.value != null){
      		return Alfresco.util.encodeHTML(beCPG.util.sigFigs(data.value,2).toLocaleString());
      	}      
      	return "";
      }
  });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : ["bcpg:allergenListQtyPerc", "bcpg:filQtyPercMaxi", "bcpg:allergenRegulatoryThreshold", "bcpg:ingListQtyPerc"],
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
      	if(data.value != null){
      		var unit, qty;
      		if(data.value == 0){
      			return "0";
      		} else if(Math.abs(data.value) < 0.01){
      			qty = data.value * 10000;
      			unit = " ppm";
      		} else if(Math.abs(data.value) < 0.1){
      			qty = data.value * 10;
      			unit = " ‰";
      		} else{
      			qty = data.value;
      			unit = " %";
      		}
      		return Alfresco.util.encodeHTML(beCPG.util.sigFigs(qty,4).toLocaleString() + unit);
      	}      
      	return "";
      }
  });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "bcpg:cost",
      renderer : function(oRecord, data, label, scope) {
          
    	  var url = null;
			if(scope.datalistMeta && scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed")>-1){
				url = beCPG.util.entityURL(data.siteId, data.value);
			}
    	  
          var title = Alfresco.util.encodeHTML(data.metadata);
          var cssClass = data.metadata;
          
          if(oRecord.getData("itemData")["prop_bcpg_costListIsFormulated"]!=null){
              var isFormulated = oRecord.getData("itemData")["prop_bcpg_costListIsFormulated"].value;
              var error = oRecord.getData("itemData")["prop_bcpg_costListFormulaErrorLog"].value;
              if(error != null){
             	 cssClass= "cost-formulated-error";
                 title = Alfresco.util.encodeHTML(error);
              } else if(isFormulated){
             	 cssClass= "cost-formulated";
              }
          }
          
          if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
              var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
              return '<span class="' + cssClass + '" style="margin-left:' + padding + 'px;" title="'+title+'">' 
              +(url!=null?'<a href="' + url + '">':'') + Alfresco.util.encodeHTML(data.displayValue)
                      + (url!=null?'</a>':'')+ '</span>';
          }

          return '<span class="' + cssClass + '" title="'+title+'">' +(url!=null?'<a href="' + url + '">':'') + Alfresco.util.encodeHTML(data.displayValue) + (url!=null?'</a>':'')+ '</span>';
      }

  });
	

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:dynamicCharactValue",
		renderer : function(oRecord, data, label, scope) {
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
							for (i = 0; i < json.comp.length; i++) {
								if (json.comp[i].value) {
									if (i == 0) {
										refValue = beCPG.util.sigFigs(parseFloat(json.comp[i].value),4);
										ret += '<span style="color:' + color + ';">' + Alfresco.util.encodeHTML(json.comp[i].displayValue)
												+ '</span>';
									} else {
										currValue = beCPG.util.sigFigs(parseFloat(json.comp[i].value),4);
										if (currValue != Number.NaN && refValue != Number.NaN) {
											if(refValue == currValue){
												className = "dynaCompEquals";
											} else {
												className = (refValue < currValue) ? "dynaCompIncrease" : "dynaCompDecrease";
											}
										} else {
											className = "dynaCompNone";
										}
										ret += '<span  class="' + className + '" >(<a title="' + json.comp[i].name + '" href="'
												+ beCPG.util.entityURL(json.comp[i].siteId, json.comp[i].nodeRef, json.comp[i].itemType)
												+ '">' + Alfresco.util.encodeHTML(json.comp[i].displayValue) + '</a>)</span>';
									}
								}
							}
							return ret;
						}
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
		propertyName : "bcpg:labelClaim",
		renderer : function(oRecord, data, label, scope) {

			var isFormulated = oRecord.getData("itemData")["prop_bcpg_lclIsFormulated"]!=null 
						? oRecord.getData("itemData")["prop_bcpg_lclIsFormulated"].value : false;
			if (isFormulated) {
				var error = oRecord.getData("itemData")["prop_bcpg_lclFormulaErrorLog"]!=null ?
						oRecord.getData("itemData")["prop_bcpg_lclFormulaErrorLog"].value : null;
				if (error == null) {
					
					var description = oRecord.getData("itemData")["dt_bcpg_lclLabelClaim"][0]["itemData"]["prop_cm_description"]!=null 
					? oRecord.getData("itemData")["dt_bcpg_lclLabelClaim"][0]["itemData"]["prop_cm_description"].displayValue : "";
					
					return '<span class="lcl-formulated"  title="' + Alfresco.util.encodeHTML(description) + '">'
							+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';
				}

				return '<span class="lcl-formulated-error" title="' + Alfresco.util.encodeHTML(error) + '">'
						+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			}			
			
			return '<span>' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
		}

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : "bcpg:rclReqMessage",
        renderer : function(oRecord, data, label, scope) {
            
            if(scope.datalistMeta && scope.datalistMeta && scope.datalistMeta.name.indexOf("WUsed")>-1){
                return data.displayValue;
            } else {
                var reqType = oRecord.getData("itemData")["prop_bcpg_rclReqType"].value;
                var reqProducts = oRecord.getData("itemData")["assoc_bcpg_rclSources"];
                var html = "";
                    html += '<div class="rclReq-details">';
                    if(reqType){
                        html += '   <div class="icon" ><span class="reqType'+reqType+'" title="'+ 
                        Alfresco.util.encodeHTML( scope.msg("data.reqtype."+ reqType.toLowerCase())) + '">&nbsp;</span></div>';
                    }
                    html += '      <div class="rclReq-title">' + Alfresco.util.encodeHTML(data.displayValue) + '</div>';
                    html += '      <div class="rclReq-content"><ul>';
                    
                    if(reqProducts){
                        for(var i in reqProducts){
                            var product = reqProducts[i], pUrl =  beCPG.util.entityURL(product.siteId, product.value);
                            
                            if (product.metadata.indexOf("finishedProduct") != -1 || product.metadata.indexOf("semiFinishedProduct") != -1) {
                            	pUrl = beCPG.util.entityURL(product.siteId, product.value,null,null,"compoList");
            				} else if (product.metadata.indexOf("packagingKit") != -1) {
            					pUrl = beCPG.util.entityURL(product.siteId, product.value,null,null,"packagingList");
            				} 
                            
                            if(pUrl){
                            	pUrl+="&bcPath=true&bcList="+scope.datalistMeta.name;
            				}
                            
                            html +='<li><span class="' + product.metadata + '" ><a href="' + pUrl + '">' 
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
        propertyName : "bcpg:dynamicCharactTitle",
        renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
            var column = oRecord.getData("itemData")["prop_bcpg_dynamicCharactColumn"].value;
            
            if (column !=null && column.length > 0) {                
                
                if(!Dom.get(scope.id+"-colCheckbox").checked){
                    Dom.addClass(elCell.parentNode.parentNode, "hidden");
                }
                
               return "<b>"+Alfresco.util.encodeHTML(data.displayValue)+"</b>";
            }
            
            return Alfresco.util.encodeHTML(data.displayValue);
        }
    });

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : "bcpg:lrLocales",
        renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
        	if (data.value != null) {
				Dom.setStyle(elCell, "width", "16px");
				Dom.setStyle(elCell.parentNode, "width", "16px");
				if (oColumn.hidden) {
					scope.widgets.dataTable.showColumn(oColumn);
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				}
				
				var country = data.value[0];
				
			  	if(country.indexOf("_")>0){
			  	 country = country.split("_")[1].toLowerCase();
			  	}
			  	
				return '<img  title="'+scope.msg('locale.name.'+data.value[0])+'" src="'+ Alfresco.constants.URL_RESCONTEXT  +'/components/images/flags/'+country+'.png" />';
			}
			return "";
        }
    });

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:dynamicCharactGroupColor",
		renderer : function(oRecord, data, label, scope) {
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
		propertyName : "bcpg:dynamicCharactColumn",
		renderer : function(oRecord, data, label, scope) {

			if (data.value != null && data.value.length > 0) {
				YAHOO.Bubbling.fire("columnRenamed", {
					columnId : "prop_" + data.value,
					label : oRecord.getData("itemData")["prop_bcpg_dynamicCharactTitle"].value
				});
			}

			return null;
		}
	});

	YAHOO.Bubbling.on("onDatalistColumnsReady", function(layer, args) {
		var obj = args[1];
		if (obj != null) {
			if (obj.entityDatagrid.entity && obj.entityDatagrid.entity.compareWithEntities
					&& obj.entityDatagrid.entity.compareWithEntities.length > 0) {

				var scope =  obj.entityDatagrid;
				var oColumn = scope.widgets.dataTable.getColumn("prop_bcpg_compareWithDynColumn");
				if (oColumn != null) {
					var precColumn = obj.entityDatagrid.widgets.dataTable.getColumn(oColumn.getKeyIndex() - 1);
					var ind = "";
					for (var j = 0, jj = scope.entity.compareWithEntities.length; j < jj; j++) {
						ind +="'";
						var compareWithEntity = scope.entity.compareWithEntities[j];
						scope.widgets.dataTable.insertColumn({
							key : "dynCompareWith-" + compareWithEntity.nodeRef,
							label : "<span title='" +compareWithEntity.name + "' >" +precColumn.label+ind+"</span>",
							editor : scope.rendererHelper.getCellEditor(scope, {
								dataType : "double",
								mandatory : true,
								fieldRef : precColumn.getField()
							},scope.options.saveFieldUrl),
							formatter : function(elCell, oRecord, oColumn, oData) {
								if (oData != null) {
									elCell.innerHTML = oData.displayValue;
								}
								return null;
							}
						}, oColumn.getKeyIndex() + j+1);
					}
				}
			}
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:compareWithDynColumn",
		renderer : function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
			if (data.value != null && data.value.indexOf && data.value.indexOf("\"comp\":") > -1) {
				var json = JSON.parse(data.value);
				if (json) {
					for (var i = 0; i < json.comp.length; i++) {
						if (json.comp[i].value != null && json.comp[i].value !== undefined) {

							var newColumn = scope.widgets.dataTable.getColumn("dynCompareWith-" + json.comp[i].nodeRef);
							scope.widgets.dataTable.updateCell(oRecord, newColumn, {
								value : json.comp[i].value,
								displayValue : json.comp[i].displayValue,
								itemNodeRef : json.comp[i].itemNodeRef
							}, false);
						}
					}
				}
			}
			return null;
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:variantIds",
		renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			var variants = data.value, isInDefault = !variants || variants.length < 1;

			if (data.value != null && data.value.length > 0) {
				if(oColumn.label == ""){
					Dom.setStyle(elCell, "width", "16px");
					Dom.setStyle(elCell.parentNode, "width", "16px");
				}
				if (oColumn.hidden) {
					scope.widgets.dataTable.showColumn(oColumn);
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				}
			}

			if (isInDefault) {
			   return "<span  class='variant-common'>&nbsp;</span>";
			}

			var cssClass = "variant"
		    var style = "";
				
			if(data.metadata!=null){
			  var splitted = data.metadata.split(",")[0].split("#");
			  cssClass = splitted[0];
			 
			  if(splitted.length>0){
				 var variantColor = "#"+splitted[1];
				  if(cssClass!= "variant-default" && variantColor!=null && variantColor.length>0 ){
					  style="style=\"background-color:"+variantColor+"\"";
				}  
			  }
			}
			if(oColumn.label != ""){
				return "<span "+style+" title=\"" + data.displayValue + "\" class='"+cssClass+"'>&nbsp;</span>&nbsp;"+data.displayValue;
			}	
			
			return "<span "+style+" title=\"" + data.displayValue + "\" class='"+cssClass+"'>&nbsp;</span>";
		}

	});
	

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : ["bcpg:startEffectivity","bcpg:endEffectivity"],
		renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			
			
			if(scope.options.extraDataParams!=null && scope.options.extraDataParams.indexOf("effectiveFilterOn="+(!oColumn.hidden))>0){
				if (oColumn.hidden) {
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
					scope.widgets.dataTable.showColumn(oColumn);
				} else {
					Dom.addClass(elCell.parentNode, "yui-dt-hidden");
					scope.widgets.dataTable.hideColumn(oColumn);
				}
			}
			
			if(label == "bcpg:startEffectivity" ){
				var now = new Date();
				var past = false;
				var future = false;
				var startEffectivity = data.value;
				var endEffectivity = oRecord.getData("itemData")["prop_bcpg_endEffectivity"];
				
				if(startEffectivity !=null && now.getTime() < Alfresco.util.fromISO8601(startEffectivity).getTime()){
					future = true ;
				}
				
				if(!future && endEffectivity!=null && endEffectivity.value!=null && now.getTime() > Alfresco.util.fromISO8601(endEffectivity.value).getTime()) {
					past = true;
				}
				
				if(past || future){
					var elTr = scope.widgets.dataTable.getTrEl(elCell);
					if(past){
						Dom.setStyle(elTr, 'background-color', "#ffebee");
					} else {
						Dom.setStyle(elTr, 'background-color', "#e8f5e9");
					}
					 Dom.setStyle(elTr, "opacity" ,"0.5");
				}
			}
			
			if (data.value != null) {
				return Alfresco.util.formatDate(data.value,"yyyy-mm-dd");
			} 
			return "";
		}
	});
	
	

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "pack:labelingPosition",
		renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			if (data.value != null) {
				Dom.setStyle(elCell, "width", "16px");
				Dom.setStyle(elCell.parentNode, "width", "16px");
				if (oColumn.hidden) {
					scope.widgets.dataTable.showColumn(oColumn);
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				}
				return "<span title=\"" + data.displayValue + "\" class='labeling-aspect'>&nbsp;</span>";
			}
			return "";
		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:illValue", "bcpg:illManualValue" ],
		renderer : function(oRecord, data, label, scope) {
			if (data.value != null) {
				
				var html ='', displayValue = data.displayValue,
					htmlId = "id-" + oRecord._sId, suffix = "bcpg_illValue",
					editPermission = oRecord.getData().permissions.userAccess.edit;
				
					if(data.displayValue == null || data.displayValue == "" ){
						displayValue = "<i>"+scope.msg("label.empty")+"</i>";
					}
					
					html += '<div class="note rounded"><div id="'+ htmlId + suffix +'" > ' + displayValue + '</div>';
					
					var nodeRef = oRecord._oData.nodeRef;
					html += '<div class="labeling-action-container">';
				
					if(label == "bcpg:illValue"){
						
						
						if(editPermission){
							html += '<span id="' +htmlId+"#"+ nodeRef+"#"+ label+  '" class="copyToIllManualValue"><a href="#" title="'+ scope.msg("label.copy.to.illManualValue.title") +'" class="labeling-action copy-to"></a> </span>';
							html += '<span>&nbsp;</span>';
						}
						html += '<span id="' +htmlId+"#"+ nodeRef+"#"+ suffix+  '" class="onCopyToClipboard"><a href="#" title="'+ scope.msg("label.copy.to.clipboard.title") +'" class="labeling-action clipboard"></a> </span>';
						html += '<span>&nbsp;&nbsp;</span>';
					}
					if(label == "bcpg:illManualValue" && editPermission){
						html += '<span id="' +htmlId+"#"+ nodeRef+"#"+ label+ "#false" + '" class="onShowTranslation"><a href="#" title="'+ scope.msg("label.edit.translation.title") +'" class="labeling-action edit-translation"></a> </span>';
						html += '<span>&nbsp;</span>';
					}
					html += '<span id="' +htmlId+"#"+ nodeRef+"#"+ label+ "#true" + '" class="onShowTranslation"><a href="#" title="'+ scope.msg("label.show.translation.title") +'" class="labeling-action show-translation" ></a></span>';
					html += '</div>';
				
				
				html += '</div>';
				return html;
			}
			return "";
		}

	});
	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "ecm:cclSourceValue", "ecm:cclTargetValue" ],
		renderer : function(oRecord, data, label, scope) {
			if (data.displayValue != null && data.displayValue.length>0) {
				return  data.displayValue;
			}
			return "";
		}

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	    propertyName : "boolean_bcpg:lrIsActive",
	    renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
	    	Dom.setStyle(elCell, "width", "16px");
	        Dom.setStyle(elCell.parentNode, "width", "16px");    
	    	if (oColumn.hidden) {
                    scope.widgets.dataTable.showColumn(oColumn);
                    Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
            }
	        if(data.value){
	            return "<span  class='rule-enabled'>&nbsp;</span>";
	        } 
	        return "<span  class='rule-disabled'>&nbsp;</span>";
	    }
	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:dynamicCharactColumn1", "bcpg:dynamicCharactColumn2", "bcpg:dynamicCharactColumn3", "bcpg:dynamicCharactColumn4",
				"bcpg:dynamicCharactColumn5", "bcpg:dynamicCharactColumn6", "bcpg:dynamicCharactColumn7", "bcpg:dynamicCharactColumn8",
				"bcpg:dynamicCharactColumn9", "bcpg:dynamicCharactColumn10" ],
		renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
		   
		    if(oRecord.getData("itemData")["isMultiLevel"]){
		        if( scope.subCache!=null && scope.subCache["idx_"+oColumn.getKeyIndex()]!=null){
		              for (var j = 0; j <  scope.subCache["idx_"+oColumn.getKeyIndex()].length; j++) {
                        var path =  scope.subCache["idx_"+oColumn.getKeyIndex()][j].path;
                        if(path == oRecord.getData("itemData")["path"] && scope.subCache["idx_"+oColumn.getKeyIndex()][j].displayValue){
                        	
                        	if(oColumn.numberFormat && scope.subCache["idx_"+oColumn.getKeyIndex()][j].value!=null){
                        		 return  beCPG.util.formatNumber(oColumn.numberFormat, scope.subCache["idx_"+oColumn.getKeyIndex()][j].value);	  
             				} else {
             					 return  scope.subCache["idx_"+oColumn.getKeyIndex()][j].displayValue;
             				}

                        }
                    }
                }
		        return "";
		    }
		    
		    if (data.value != null) {
			    if(!oRecord.getData("itemData")["isMultiLevel"]){ 
    			    if (data.value.indexOf && data.value.indexOf("\"sub\":") > -1) {
    			        var json = JSON.parse(data.value);
                        if (json) {
                            if(!scope.subCache){
                                scope.subCache = [];
                            }
                            scope.subCache["idx_"+oColumn.getKeyIndex()] = json.sub;
                            if(oColumn.numberFormat && json.value!=null){
            				    return  beCPG.util.formatNumber(oColumn.numberFormat, json.value);	   
            				} else {
            					return json.displayValue? json.displayValue : "";
            				}
                        }
    			    }
			    }
			    
				if (data.value.indexOf && data.value.indexOf("\"comp\":") > -1) {
					var json = JSON.parse(data.value);
					if (json) {
						var ret = "", z = 0, refValue = null, className, currValue = null;
						for (z = 0; z < json.comp.length; z++) {
							if (json.comp[z].value) {
								if (z == 0) {
									refValue = beCPG.util.sigFigs(parseFloat(json.comp[z].value),4);
									ret += '<span>' + Alfresco.util.encodeHTML(json.comp[z].displayValue) + '</span>';
								} else {
									currValue = beCPG.util.sigFigs(parseFloat(json.comp[z].value),4);
									if (currValue != Number.NaN && refValue != Number.NaN) {
										if(refValue == currValue){
											className = "dynaCompEquals";
										} else {
											className = (refValue < currValue) ? "dynaCompIncrease" : "dynaCompDecrease";
										}
									} else {
										className = "dynaCompNone";
									}
									ret += '<span  class="' + className + '" >(<a title="' + json.comp[z].name + '" href="'
											+ beCPG.util.entityURL(json.comp[z].siteId, json.comp[z].nodeRef, json.comp[z].itemType) + '">'
											+ Alfresco.util.encodeHTML(json.comp[z].displayValue) + '</a>)</span>';
								}
							}
						}
						return ret;
					}
				}
				
				if(oColumn.numberFormat && data.value!=null){
				   return  beCPG.util.formatNumber(oColumn.numberFormat, data.value); 
				} else {
				   return data.displayValue;
				}
			}
			return "";

		}

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:compoListQty",
		renderer : function(oRecord, data, label, scope) {
			var qty = "";			
			if (data.value != null) {				
				var unit = "";
				if( Alfresco.constants.JS_LOCALE == "en_US"){
					var absValue = Math.abs(data.value*2.204622622);
					var compoUnit = oRecord.getData("itemData")["prop_bcpg_compoListUnit"]!=null ?
							 oRecord.getData("itemData")["prop_bcpg_compoListUnit"].value : "";
				  if ((absValue < 1 &&  compoUnit != "lb")  
						  || compoUnit == "oz") {
						qty = data.value *35.27396195;
						unit = " oz";
					}  else {
						qty = data.value*2.204622622;
						unit = " lb";
					}
				} else {
					var absValue = Math.abs(data.value);
					if (absValue < 0.0001) {
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

				qty = beCPG.util.sigFigs(qty,4).toLocaleString() + unit;
			}

			return Alfresco.util.encodeHTML(qty);
		}
	});
	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName : "bcpg:packagingListQty",
        renderer : function(oRecord, data, label, scope) {
        	if (data.value != null) {
        		return data.value;
        	}
        	return "";
        }
    });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "bcpg:instruction"],
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
         if(data.value != null && data.value.length>0){
        	if(oColumn.label == ""){
        		Dom.setStyle(elCell, "width", "16px");
				Dom.setStyle(elCell.parentNode, "width", "16px");
        		if (oColumn.hidden) {
						scope.widgets.dataTable.showColumn(oColumn);
						Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				}
				
				return "<span title=\"" + data.displayValue.replace(/&nbsp;/gi," ")
						.replace(/<(?:.|\n)*?>/gm, '').replace(/\n/gm," ").replace(/"/gm,"") 
				+ "\" class='instructions'>&nbsp;</span>";
        	} else {
        		return data.displayValue;
        	}
			
         }
         return "";
      }
  });	

	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "bcpg:lclComments"],
	      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
	         if(data.value != null && data.value.length>0){
				return data.displayValue;
	         }
	         return "";
	      }
	  });	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : "cm:cmobject_bcpg:lclMissingLabelClaims",
	      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
	    	 
	    	 var missingSources = oRecord.getData("itemData")["assoc_bcpg_lclMissingLabelClaims"];
	    	 
	    	 //only put text on first result
	         if(data.value != null && data.value.length>0 && missingSources.length > 0 && i==0){
					var description = scope.msg("becpg.forms.help.lclMissingSources", missingSources.length);
					var tooltip = "";
					
					for(var source in missingSources){
						tooltip+=missingSources[source].displayValue+"\n";
						
					}
					
					var tooltipText = scope.msg("becpg.forms.help.lclMissingSources.list", tooltip);
					
				return "<span class=\"lcl-formulated-error\" title=\"" + tooltipText +"\">"+description+"</span>";
	         }
	         return "";
	      }
	  });
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : "bcpg:compoListVolume",
		renderer : function(oRecord, data, label, scope) {

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

				qty =  beCPG.util.sigFigs(qty,4).toLocaleString() + unit;
			}

			return Alfresco.util.encodeHTML(qty);
		}
	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:compoListDeclType", "bcpg:packagingListPkgLevel" ],
		renderer : function(oRecord, data, label, scope) {
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
		propertyName : [ "bcpg:nutListGDAPerc" ],
		renderer : function(oRecord, data, label, scope) {
			
			var percentValue = data.value;
			var nutColor = null;
			
			if(oRecord.getData("itemData")["dt_bcpg_nutListNut"] && 
					oRecord.getData("itemData")["dt_bcpg_nutListNut"].length>0){
			   nutColor = oRecord.getData("itemData")["dt_bcpg_nutListNut"][0].color;
			}
			
			if(percentValue !== null && percentValue > 0 && nutColor!=null && nutColor !== undefined){
				
				var additionalProps = oRecord.getData("itemData")["dt_bcpg_nutListNut"][0].itemData;
				var nutValue = oRecord.getData("itemData")["prop_bcpg_nutListValue"].displayValue;
				var gda = additionalProps.prop_bcpg_nutGDA.value;
				var ul = additionalProps.prop_bcpg_nutUL.value;
				var unit = additionalProps.prop_bcpg_nutUnit.displayValue;
	
				var ulExceeded = false;
				var red = "#F44336";
				var gray = "#cccccc";
				
				if(nutValue !== null && ul !== null && (nutValue > ul)){
					ulExceeded = true;
					nutColor = red;
				}
				
				var hexColorSplit = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(nutColor);
			    var rgbColor =  hexColorSplit ? {
			        r: parseInt(hexColorSplit[1], 16),
			        g: parseInt(hexColorSplit[2], 16),
			        b: parseInt(hexColorSplit[3], 16)
			    } : null;
	
			    var emptyBarColor = null;
			    
			    //background of unfilled part of progress bar
			    if(percentValue <100){
			    	emptyBarColor = ulExceeded ? red : gray;
			    }
			    
				var fontColor = "black";
				
				if (rgbColor && (rgbColor.r*0.2126 + rgbColor.g*0.7152 + rgbColor.b*0.0722) < 186 && percentValue > 20){
					fontColor = "white";
				}
				
				var gdaReminder = null;
				if(gda !== null && unit !== null){
					gdaReminder = scope.msg("becpg.forms.help.gda-reminder", gda, unit);
				}
				
				var reminderColor = "black";
				
				if(percentValue > 80 || ulExceeded){
					reminderColor = fontColor;
				}

				var html="<div class=\"progress-bar\" "+ (emptyBarColor != null ? "style=\"background-color: " + emptyBarColor +";\" ": "") + " title=\"" + percentValue.toFixed(1)+"% " + gdaReminder + (ulExceeded?"\n"+scope.msg("becpg.forms.help.ul-exceeded") : "") +"\";\">";
					if(gdaReminder !== null){
						html += "<div class =\"outer-progress-bar\" style=\"float: right; color: " + reminderColor + "\">" + gdaReminder + "</div>";
					}
						html += "<div class =\"inner-progress-bar\" style=\"width: " + Math.min(percentValue, 100) + "%; background-color: " + nutColor + (percentValue < 100 ? ";" : "") + ";\">";
						html += "	<div  style=\"color: " + fontColor + "; white-space: nowrap;\">" + percentValue.toFixed(1) + "%</div>";
					html += "</div>";
				html += "</div>";					        	


				return html;
			} else if(percentValue !== null && percentValue > 0){
				return Alfresco.util.encodeHTML(percentValue.toFixed(1)+" %");
			} else {
				return "";
			}
		}
	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
		propertyName : [ "bcpg:nutListValuePerServing" ],
		renderer : function(oRecord, data, label, scope) {
			if(data.value != null && data.value > 0){
				var additionalProps = oRecord.getData("itemData")["dt_bcpg_nutListNut"] ? oRecord.getData("itemData")["dt_bcpg_nutListNut"][0].itemData: null;
				var unit =additionalProps!=null ? additionalProps.prop_bcpg_nutUnit.displayValue : "";
				
				return Alfresco.util.encodeHTML(beCPG.util.sigFigs(data.value,3).toLocaleString()+" "+unit);
				
			}
			return "";
		}
	});
	

}
