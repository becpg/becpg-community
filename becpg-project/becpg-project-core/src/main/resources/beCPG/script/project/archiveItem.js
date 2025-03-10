<import resource="classpath:/beCPG/rules/helpers.js">

function main() {    

    if (project.getEntities() != null) {

        for (var i = 0; i < project.getEntities().size(); i++) {                    

            var projectEntity = search.findNode(project.getEntities().get(i));
            var propertyToUpdate = "";

            if (projectEntity.isSubType("bcpg:supplier")) {
                propertyToUpdate = "bcpg:supplierState";                
            } else if (projectEntity.isSubType("bcpg:client")) {
                propertyToUpdate = "bcpg:clientState";
            } else if (projectEntity.isSubType("bcpg:product")) {
                propertyToUpdate = "bcpg:productState";
            }                        

            var shouldSave = setValue(projectEntity, propertyToUpdate,"Archived");

            if (shouldSave) {
                projectEntity.save();
            }                    
        }
        //complete the current task
        task.state = "Completed";
    }
}

main();
