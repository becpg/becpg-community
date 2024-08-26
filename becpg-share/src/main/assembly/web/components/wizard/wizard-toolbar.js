(function() {
    if (beCPG.component.WizardMgr) {
		
		 YAHOO.Bubbling
		          .fire("registerWizardToolbarButtonAction", {
            actionName: "entity-comments",
            evaluate: function(wizard) {
                return wizard.options.comments;
            },
            fn: function() {
				var me = this;
                var container = Dom.get(me.id + "-commentsList");
                if (container) {
                    Dom.removeClass(container, "hidden");
                }
            }
        });
    }
})();
