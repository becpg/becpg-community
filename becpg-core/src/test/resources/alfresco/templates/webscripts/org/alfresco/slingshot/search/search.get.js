<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">
function main()
{
   var params =
   {
      siteId: (args.site !== null) ? args.site : null,
      containerId: (args.container !== null) ? args.container : null,
      repo: (args.repo !== null) ? (args.repo == "true") : false,
      term: (args.term !== null) ? args.term : null,
      tag: (args.tag !== null) ? args.tag : null,
      query: (args.query !== null) ? args.query : null,
      sort: (args.sort !== null) ? args.sort : null,
      maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS
   };
   
   model.data = getSearchResults(params);
}

main();