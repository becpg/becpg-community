<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/modify.lib.js">

script:
{
    // ensure atom entry is posted
    if (entry === null)
    {
        status.code = 400;
        status.message = "Expected atom entry";
        status.redirect = true;
        break script;
    }

    // locate source node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    model.target = object.node;

    // create 
    var policy = applyPolicy(model.target, entry);
    if (policy == null)
    {
        break script;
    }
    
    // success
    model.policy = policy;

    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + "/cmis/pol/" + policy.id;
    status.redirect = true;
}
