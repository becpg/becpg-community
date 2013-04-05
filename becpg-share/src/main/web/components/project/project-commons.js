
(function()
{
   
   Bubbling = YAHOO.Bubbling;

  
   beCPG.component.ProjectCommons = {};
   beCPG.component.ProjectCommons.prototype =
   {

             onActionShowTask : function PL_onActionShowTask(className) {

                     var me = this;

                     // Intercept before dialog show
                     var doBeforeDialogShow = function PL_onActionShowTask_doBeforeDialogShow(p_form, p_dialog) {

                        Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", me.msg("label.edit-row.title") ]);

                     };

                     var nodes = className.replace("node-", "").split("|");

                     var templateUrl = YAHOO.lang
                           .substitute(
                                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                 {
                                    itemKind : "node",
                                    itemId : nodes[0],
                                    mode : "edit",
                                    submitType : "json",
                                    entityNodeRef : nodes[1]
                                 });

                     // Using Forms Service, so always create new instance

                     var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editCharacts-" + Alfresco.util
                           .generateDomId());

                     editDetails.setOptions({
                        width : "34em",
                        templateUrl : templateUrl,
                        actionUrl : null,
                        destroyOnHide : true,
                        doBeforeDialogShow : {
                           fn : doBeforeDialogShow,
                           scope : this
                        },

                        onSuccess : {
                           fn : function PL_onActionShowTask_success(response) {

                              Bubbling.fire(me.scopeId + "dataItemUpdated", {
                                 nodeRef : nodes[1],
                                 callback : function(item) {

                                    // Display success message
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : me.msg("message.details.success")
                                    });
                                 }
                              });

                           },
                           scope : this
                        },
                        onFailure : {
                           fn : function PL_onActionShowTask_failure(response) {
                              Alfresco.util.PopupManager.displayMessage({
                                 text : me.msg("message.details.failure")
                              });
                           },
                           scope : this
                        }
                     }).show();

                  }
   };
})();