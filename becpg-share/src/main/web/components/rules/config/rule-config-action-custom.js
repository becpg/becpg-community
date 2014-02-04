/**
 * RuleConfigActionCustom.
 * 
 * @namespace beCPG.custom
 * @class beCPG.custom.RuleConfigActionCustom
 */
(function() {

 

   /**
    * Alfresco Slingshot aliases
    */
   var  $hasEventInterest = Alfresco.util.hasEventInterest;

   beCPG.custom.RuleConfigActionCustom = function(htmlId) {
      beCPG.custom.RuleConfigActionCustom.superclass.constructor.call(this, htmlId);

      // Re-register with our own name
      this.name = "beCPG.custom.RuleConfigActionCustom";
      Alfresco.util.ComponentManager.reregister(this);

      // Instance variables
      this.customisations = YAHOO.lang.merge(this.customisations,
            beCPG.custom.RuleConfigActionCustom.superclass.customisations);
      this.renderers = YAHOO.lang.merge(this.renderers, beCPG.custom.RuleConfigActionCustom.superclass.renderers);

      return this;
   };

   YAHOO.extend(beCPG.custom.RuleConfigActionCustom, Alfresco.RuleConfigAction, {

      /**
       * CUSTOMISATIONS
       */

      customisations : {
         VersionCleaner : {
            text : function(configDef, ruleConfig, configEl) {
               ruleConfig.parameterValues = ruleConfig.parameterValues || {};
               ruleConfig.parameterValues["versionType"] = this.msg("label.version-cleaner."+ruleConfig.parameterValues["versionType"]);
               
               if(ruleConfig.parameterValues["numberOfVersion"]!=null){
                  ruleConfig.parameterValues["numberOfVersion"] = this.msg("label.version-cleaner.message.numberOfVersion",ruleConfig.parameterValues["numberOfVersion"]);
               }
               if(ruleConfig.parameterValues["numberOfDay"]!=null){
                  ruleConfig.parameterValues["numberOfDay"] = this.msg("label.version-cleaner.message.numberOfDay",ruleConfig.parameterValues["numberOfDay"]);
               }
               
               if(ruleConfig.parameterValues["numberByDay"]!=null){
                  ruleConfig.parameterValues["numberByDay"] = this.msg("label.version-cleaner.message.numberByDay",ruleConfig.parameterValues["numberByDay"]);
               }
               
               
               return configDef;
            },
            edit : function(configDef, ruleConfig, configEl) {
               this._hideParameters(configDef.parameterDefinitions);
               configDef.parameterDefinitions.push({
                  type : "arca:versioncleaner-dialog-button",
                  _buttonLabel : this.msg("button.options")
               });
               return configDef;
            }
         }
      },

      renderers : {

         "arca:versioncleaner-dialog-button" : {
            manual : {
               edit : true
            },
            currentCtx : {},
            edit : function(containerEl, configDef, paramDef, ruleConfig, value) {
               this._createButton(containerEl, configDef, paramDef, ruleConfig,
                     function RCA_versionCleanerFormButton_onClick(type, obj) {
                        this.renderers["arca:versioncleaner-dialog-button"].currentCtx = {
                           configDef : obj.configDef,
                           ruleConfig : obj.ruleConfig
                        };
                        if (!this.widgets.versionCleanerForm) {
                           this.widgets.versionCleanerForm = new Alfresco.module.RulesVersionCleanerAction(this.id + "-versionCleanerForm");
                           YAHOO.Bubbling.on("vcleanerConfigCompleted", function(layer, args) {
                              if ($hasEventInterest(this.widgets.versionCleanerForm, args)) {
                                 var values = args[1].options;
                                 if (values !== null) {
                                    var ctx = this.renderers["arca:versioncleaner-dialog-button"].currentCtx;
                                    this._setHiddenParameter(ctx.configDef, ctx.ruleConfig, "versionType",values.versionType);
                                    this._setHiddenParameter(ctx.configDef, ctx.ruleConfig, "numberOfVersion",values.numberOfVersion);
                                    this._setHiddenParameter(ctx.configDef, ctx.ruleConfig, "numberOfDay",values.numberOfDay);
                                    this._setHiddenParameter(ctx.configDef, ctx.ruleConfig, "numberByDay",values.numberByDay);
                                    this._updateSubmitElements(ctx.configDef);
                                 }
                              }
                           }, this);
                        }
                        var params = this._getParameters(obj.configDef);
                        this.widgets.versionCleanerForm.showDialog({
                           versionType : params.versionType,
                           numberOfVersion : params.numberOfVersion,
                           numberOfDay : params.numberOfDay,
                           numberByDay : params.numberByDay
                        });
                     });
            }

         }
      }
   });

})();