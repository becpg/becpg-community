/* beCPG : Get the compare report templates */

function main()
{

    model.reportTpls = [];
    model.entities = "";
    try
    {
        var uri = "/becpg/report/compare/templates?entityNodeRef="+args.entityNodeRef;
        var connector = remote.connect("alfresco");
        var result = connector.get(uri);
        if (result.status.code == status.STATUS_OK && result != "{}")
        {
            var tpls = eval('(' + result.response + ')');
            model.reportTpls = tpls.reportTpls;
            model.entities = tpls.entities;
        }
    }
    catch (e)
    {
    }

}

main();