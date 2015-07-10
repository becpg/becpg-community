function main()
{
    var projectEntity = null;

    var projectNode = search.findNode(project.nodeRef);

    if (projectNode.assocs["pjt:projectEntity"] != null && projectNode.assocs["pjt:projectEntity"].length > 0)
    {
        projectEntity = projectNode.assocs["pjt:projectEntity"][0];
    } 

    bSupplier.validateProjectEntity(projectEntity);
    
}

main();