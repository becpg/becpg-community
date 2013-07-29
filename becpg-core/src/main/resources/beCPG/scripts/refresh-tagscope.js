function refreshTagScope(sites){
	
	for each(site in sites.children)
	{
		for each(item in site.children)
		{
			if (item.isContainer)
			{
				var refresh = actions.create("refresh-tagscope");
				refresh.execute(item);
			}
		}		
	}
}
// execute on folder sites
var sitesNode = companyhome.childByNamePath("Sites"); 
refreshTagScope(sitesNode);
