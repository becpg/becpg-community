/* beCPG : Get the compare report templates */

function main()
{

    model.reportTpls = [];
    try
    {
        var uri = "/becpg/report/compare/templates";
        var connector = remote.connect("alfresco");
        var result = connector.get(uri);
        if (result.status.code == status.STATUS_OK && result != "{}")
        {
            var tpls = eval('(' + result.response + ')');
            model.reportTpls = tpls.reportTpls;
        }
    }
    catch (e)
    {
    }

}

main();