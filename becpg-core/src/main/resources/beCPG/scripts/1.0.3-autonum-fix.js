/**
 * Use this script to change prefix on existing codes
 * 
 */
var data =[ {type:"bcpg:finishedProduct",prefix:"PF"}
				    ,{type:"bcpg:rawMaterial",prefix:"MP"}
				    ,{type:"bcpg:localSemiFinishedProduct",prefix:"LSF"}
				    ,{type:"bcpg:packagingMaterial",prefix:"E"}
				    ,{type:"bcpg:packagingKit",prefix:"KE"}
				    ,{type:"bcpg:semiFinishedProduct",prefix:"SF"}
				    ,{type:"bcpg:condSalesUnit",prefix:"UVC"}
				    ,{type:"bcpg:client",prefix:"C"}
				    ,{type:"bcpg:supplier",prefix:"F"}];

for(row in data){
	var nodes = search.luceneSearch('+TYPE:"'+data[row].type+'"');
	for(i in nodes){
		var prop = nodes[i].properties["bcpg:code"];
		if(prop.indexOf("-")>0){
			 nodes[i].properties["bcpg:code"] = prop.replace(/-/g,"");
		} else if(prop.indexOf( data[row].prefix)<0) {
			 nodes[i].properties["bcpg:code"]  = data[row].prefix+prop;
		}
		nodes[i].save();
	}
}	