if (beCPG.module.EntityDataGridRenderers) {

   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : [ "bcpg:product", "bcpg:supplier", "bcpg:client", "bcpg:entity",
                        "bcpg:resourceProduct", "cm:content_bcpg:costDetailsListSource",
                        "bcpg:product_bcpg:packagingListProduct", "bcpg:product_bcpg:compoListProduct",
                        "ecm:wulSourceItems", "ecm:rlSourceItems", "ecm:rlTargetItem", "ecm:culSourceItem",
                        "ecm:culTargetItem", "ecm:cclSourceItem" ],
                  renderer : function(oRecord, data, label, scope) {

                     var url = beCPG.util.entityCharactURL(data.siteId, data.value), version = "";

                     if (label == "mpm:plProduct" || label == "bcpg:compoListProduct" || label == "bcpg:packagingListProduct" || label == "mpm:plResource") {

                        // datalist
                        if (data.metadata.indexOf("finishedProduct") != -1 || data.metadata
                              .indexOf("semiFinishedProduct") != -1) {
                           url += "&list=compoList";
                        } else if (data.metadata.indexOf("packagingKit") != -1) {
                           url += "&list=packagingList";
                        } else if (data.metadata.indexOf("localSemiFinishedProduct") != -1) {
                           url = scope._buildCellUrl(data);
                        }
                        if (data.version && data.version !== "") {
                           version = '<span class="document-version">' + data.version + '</span>';
                        }
                     }

                     if (label == "bcpg:compoListProduct" || label == "ecm:wulSourceItems") {

                        var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
                        return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;"><a href="' + url + '">' + Alfresco.util
                              .encodeHTML(data.displayValue) + '</a></span>' + version;
                     }
                     return '<span class="' + data.metadata + '" ><a href="' + url + '">' + Alfresco.util
                           .encodeHTML(data.displayValue) + '</a></span>' + version;
                  }

               });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "text_bcpg:lkvValue",
      renderer : function(oRecord, data, label, scope) {
         if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
            var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
            return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">' + Alfresco.util
                  .encodeHTML(data.displayValue) + '</span>';
         }
         return Alfresco.util.encodeHTML(data.displayValue);

      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "boolean_bcpg:allergenListVoluntary", "boolean_bcpg:allergenListInVoluntary" ],
      renderer : function(oRecord, data, label, scope) {
         var booleanValueTrue = scope.msg("data.boolean.true");
         var booleanValueFalse = scope.msg("data.boolean.false");
         if (data.value) {
            return '<span class="presentAllergen">' + Alfresco.util.encodeHTML(data.value == true ? booleanValueTrue
                  : booleanValueFalse) + '</span>';
         }
         return Alfresco.util.encodeHTML(data.value == true ? booleanValueTrue : booleanValueFalse);
      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "bcpg:rclReqType", "bcpg:filReqType", "ecm:culReqType" ],
      renderer : function(oRecord, data, label, scope) {
         var reqTypeForbidden = scope.msg("data.reqtype.forbidden");
         var reqTypeTolerated = scope.msg("data.reqtype.tolerated");
         var reqTypeInfo = scope.msg("data.reqtype.info");
         if (data.displayValue == "Forbidden") {
            return '<span class="reqTypeForbidden">' + Alfresco.util.encodeHTML(reqTypeForbidden) + '</span>';
         } else if (data.displayValue == "Tolerated") {
            return '<span class="reqTypeTolerated">' + Alfresco.util.encodeHTML(reqTypeTolerated) + '</span>';
         } else if (data.displayValue == "Info") {
            return '<span class="reqTypeInfo">' + Alfresco.util.encodeHTML(reqTypeInfo) + '</span>';
         } else {
            return Alfresco.util.encodeHTML(data.displayValue);
         }

      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "ecm:rlRevisionType", "ecm:culRevision" ],
      renderer : function(oRecord, data, label, scope) {

         if (data.displayValue != null) {
            return scope.msg("data.revisiontype." + data.displayValue.toLowerCase());
         }
         return Alfresco.util.encodeHTML(data.displayValue);

      }

   });

   YAHOO.Bubbling
         .fire("registerDataGridRenderer",
               {
                  propertyName : [ "qa:sdlControlPoint", "qa:slControlPoint" ],
                  renderer : function(oRecord, data, label, scope) {
                     var url = beCPG.util.entityCharactURL(data.siteId, data.value);
                     return '<span class="controlPoint"><a href="' + url + '">' + Alfresco.util
                           .encodeHTML(data.displayValue) + '</a></span>';

                  }

               });

   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : [ "qa:clCharacts" ],
                  renderer : function(oRecord, data, label, scope) {
                     var url = scope._buildCellUrl(data);
                     return '<span class="sample"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';

                  }

               });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "bcpg:cost", "bcpg:allergen", "bcpg:nut", "bcpg:ing", "bcpg:geoOrigin", "bcpg:bioOrigin",
            "bcpg:geo", "bcpg:microbio", "bcpg:physicoChem", "bcpg:organo", "bcpg:labelClaim" ],
      renderer : function(oRecord, data, label, scope) {
         return '<span class="' + data.metadata + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlTaskName" ],
      renderer : function(oRecord, data, label, scope) {
         var className = oRecord.getData("itemData")["prop_pjt_tlIsMilestone"].value ? "task-milestone" : "task";
         return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlState", "pjt:dlState" ],
      renderer : function(oRecord, data, label, scope) {
         return '<span class="' + "task-" + data.displayValue.toLowerCase() + '" title="' + data.displayValue + '" />';
      }

   });

   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : "bcpg:dynamicCharactValue",
                  renderer : function(oRecord, data, label, scope) {
                     if (data.value != null) {
                        var error = oRecord.getData("itemData")["prop_bcpg_dynamicCharactErrorLog"].value;
                        if (error == null) {
                           var color = oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value;
                           if (!color) {
                              color = "000000";
                           }
                           return '<span style="color:#' + color + ';">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
                        }

                        return '<span class="dyna' + data.value.replace("#", "") + '" title="' + Alfresco.util
                              .encodeHTML(error) + '">' + Alfresco.util.encodeHTML(error.substring(0, 7)) + '</span>';
                     }
                     return data.displayValue;
                  }

               });

   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : "bcpg:dynamicCharactGroupColor",
                  renderer : function(oRecord, data, label, scope) {
                     var color = oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value;
                     if (!color) {
                        color = "000000";
                     }
                     return '<div style="background-color:#' + color + ';width:15px;height:15px;border: 1px solid; border-radius: 5px;margin-left:15px;"></div></div>';
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

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "pjt:slScreening",
      renderer : function(oRecord, data, label, scope) {

         return '<div class="scoreList-screening">' + data.displayValue + '</div>';
      }
   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "bcpg:variantIds",
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
         var variants = data.value, isInDefault = !variants || variants.length < 1;

         if (data.value != null) {

            if (oColumn.hidden) {
               scope.widgets.dataTable.showColumn(oColumn);
               Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
            }
            // if (scope.afterRenderShowColumns.indexOf(oColumn) == -1) {
            // scope.afterRenderShowColumns.push(oColumn);
            // }
            Dom.setStyle(elCell, "width", "16px");
            Dom.setStyle(elCell.parentNode, "width", "16px");
         }

         if (isInDefault) {
            return "<span  class='variant-common'>&nbsp;</span>";
         }

         for ( var j in variants) {
            for ( var i in scope.entity.variants) {
               if (variants[j] == scope.entity.variants[i].nodeRef && scope.entity.variants[i].isDefaultVariant) {
                  isInDefault = true;
                  break;
               }
            }
         }

         if (isInDefault) {
            return "<span title=\"" + data.displayValue + "\" class='variant-default'>&nbsp;</span>";
         }

         return "<span title=\"" + data.displayValue + "\" class='variant'>&nbsp;</span>";

      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "bcpg:dynamicCharactColumn1", "bcpg:dynamicCharactColumn2", "bcpg:dynamicCharactColumn3",
            "bcpg:dynamicCharactColumn4", "bcpg:dynamicCharactColumn5" ],
      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
         if (data.value != null) {
            // if (scope.afterRenderShowColumns.indexOf(oColumn) == -1) {
            // scope.afterRenderShowColumns.push(oColumn);
            // }
            if (oColumn.hidden) {
               scope.widgets.dataTable.showColumn(oColumn);
               Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
            }
         }
         return data.value;

      }

   });

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "bcpg:compoListQty",
      renderer : function(oRecord, data, label, scope) {

         var qty = "";
         if (data.value != null) {
            var unit = "";
            if (data.value < 0.0001) {
               qty = data.value * 1000000;
               unit = " mg";
            } else if (data.value < 0.1) {
               qty = data.value * 1000;
               unit = " g";
            } else if (data.value > 1000) {
               qty = data.value / 1000;
               unit = " t";
            } else {
               qty = data.value;
               unit = " kg";
            }

            qty = parseFloat(qty.toPrecision(5)) + unit;
         }

         return Alfresco.util.encodeHTML(qty);
      }
   });
}
