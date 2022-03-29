function main()
{
    var  projectEntity = null;

    if (project.entities != null && project.entities.size() > 0) {
        projectEntity = search.findNode(project.entities.get(0));
    } 

    bSupplier.assignToSupplier(project, task, projectEntity, true);
    
}

main();