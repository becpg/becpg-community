function main()
{
    var supplier = null, rawMaterial = null;

    var projectNode = search.findNode(project.nodeRef);

    if (projectNode.assocs["ext1:extSupplierRef"] != null && projectNode.assocs["ext1:extSupplierRef"].length > 0)
    {
        supplier = projectNode.assocs["ext1:extSupplierRef"][0];
    }

    if (projectNode.assocs["pjt:projectEntity"] != null && projectNode.assocs["pjt:projectEntity"].length > 0)
    {
        rawMaterial = projectNode.assocs["pjt:projectEntity"][0];
    }

    if (supplier != null && rawMaterial!=null)
    {
        //Move rawMaterial back  supplier
    }
}

main();