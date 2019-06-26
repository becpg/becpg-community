<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">

var ret = [];
var filters = getFilters()
for(i in filters){
	if(filters[i].id == args.mode){
		ret.push(filters[i]);
	}
}

model.filters = ret;
