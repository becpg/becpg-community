function main()
{
    var supplier = null, rawMaterial = null;

    var projectNode = search.findNode(project.nodeRef);

    if (projectNode.assocs["pjt:projectEntity"] != null && projectNode.assocs["pjt:projectEntity"].length > 0)
    {
        rawMaterial = projectNode.assocs["pjt:projectEntity"][0];
    } 

    bSupplier.assignToSupplier(project, task ,rawMaterial);
    
}

main();