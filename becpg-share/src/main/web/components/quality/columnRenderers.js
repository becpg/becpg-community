(function() {

	if (beCPG.module.EntityDataGridRenderers) {


		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "qa:ncPriority",
		   renderer : function(oRecord, data, label, scope) {
		      var  priority = data.value, priorityMap = {
		            "1" : "high",
		            "2" : "medium",
		            "3" : "low"
		         }, priorityKey = priorityMap[priority + ""];
		         return '<img class="priority" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this
		               .msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
		   }

		});

	}
})();
