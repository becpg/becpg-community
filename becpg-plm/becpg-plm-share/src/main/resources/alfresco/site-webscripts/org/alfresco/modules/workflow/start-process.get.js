function main()
{

    model.processScripts = [];
    try
    {
        var uri = "/becpg/workflow/start-process?list=true&nodeRef="+args.nodeRef;
        var connector = remote.connect("alfresco");
        var result = connector.get(uri);
        if (result.status.code == status.STATUS_OK && result != "{}")
        {
            var jsonRes = eval('(' + result.response + ')');
            model.processScripts = jsonRes.scripts;
        }
    }
    catch (e)
    {
    }

}

main();