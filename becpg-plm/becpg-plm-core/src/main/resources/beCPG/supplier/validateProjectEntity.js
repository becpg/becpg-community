function main() {
	var projectEntity = null;

	if (project.entities != null && project.entities.size() > 0) {
		projectEntity = search.findNode(project.entities.get(0));
		/*
		  if no autoMerge date is specified  merge and validate entity 
		 */
		projectEntity = bSupplier.validateProjectEntity(projectEntity);

		project.entities.add(projectEntity.nodeRef);
		
	}
}

main();