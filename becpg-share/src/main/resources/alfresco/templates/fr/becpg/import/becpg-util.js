var BeCPGUtil = {

	getDefaultReport : function getDefaultReport(reports) {
		var defaultReport = null;

		for ( var j in reports) {
			var report = reports[j];
			if (defaultReport == null) {
				// Set First as default
				defaultReport = report;
			}
			if (report.isSelected || report.isDefault) {
				// Override with default
				defaultReport = report;
				if (report.isSelected) {
					// If selected here we are
					break;
				}
			}
		}
		return defaultReport;
	},

    getProductState : function getProductState(item){
       
        if(item.properties["bcpg:productState"] && item.properties["bcpg:productState"]!=null){
            return "entity-"+item.properties["bcpg:productState"].toLowerCase();
        }
        if(item.properties["ecm:ecoState"] && item.properties["ecm:ecoState"]!=null){
            return "ecm-"+item.properties["ecm:ecoState"].toLowerCase();
        }
        if(item.properties["pjt:projectState"] && item.properties["pjt:projectState"]!=null){
            return "project-"+item.properties["pjt:projectState"].toLowerCase();
        }
        
        return null;
    }

}
