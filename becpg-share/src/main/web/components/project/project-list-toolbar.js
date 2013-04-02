/**
 * ProjectListToolbar component.
 * 
 * @namespace Alfresco
 * @class beCPG.component.ProjectListToolbar
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector;

   /**
    * WorkflowListToolbar constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.component.ProjectListToolbar} The new WorkflowListToolbar instance
    * @constructor
    */
   beCPG.component.ProjectListToolbar = function TDH_constructor(htmlId, view) {
      this.view = view;
      beCPG.component.ProjectListToolbar.superclass.constructor.call(this, "beCPG.component.ProjectListToolbar",
            htmlId, [ "button" ]);
      return this;
   };

   YAHOO.extend(beCPG.component.ProjectListToolbar, Alfresco.component.Base, {
      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation
       * of YUI widgets and event listener binding.
       * 
       * @method onReady
       */
      onReady : function PTL_onReady() {

         this.widgets.showGanttButton = Alfresco.util.createYUIButton(this, "show-gantt-button",
               this.onGanttButtonClick);
         this.widgets.showPlanningButton = Alfresco.util.createYUIButton(this, "show-planning-button",
               this.onPlanningButtonClick);

         if (this.view == "gantt") {
            this.widgets.showGanttButton.set("disabled", true);
         } else {
            this.widgets.showPlanningButton.set("disabled", true);
         }

         Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");
      },

      onGanttButtonClick : function PTL_onGanttButtonClick(e, p_obj) {
         document.location.href = Alfresco.util.siteURL("project-list?view=gantt#filter=projects|InProgress");
      },
      onPlanningButtonClick : function PTL_onPlanningButtonClick(e, p_obj) {
         document.location.href = Alfresco.util.siteURL("project-list#filter=projects|InProgress");
      }

   });

})();
