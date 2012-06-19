/**
 * Use this script to migrate declarationType
 * 
 */


var nodes = search.luceneSearch('+TYPE:"bcpg:compoList"');

if(nodes.length > 0){
	
	for(j in nodes){		
		
		var name = nodes[j].properties["bcpg:compoListDeclType"];
		var value = null;
		
		if(name == "Déclarer"){
			value = "Declare";
		}
		else if(name == "Détailler"){
			value = "Detail";
		}
		else if(name == "Omettre"){
			value = "Omit";
		}
		else if(name == "Regrouper"){
			value = "Group";
		}
		else if(name == "Ne pas déclarer"){
			value = "DoNotDeclare";
		}
		
		if(value != null){
		
			nodes[j].properties["bcpg:compoListDeclType"] = value;
			nodes[j].save();
		}
		
	}		
}

nodes = search.luceneSearch('+TYPE:"bcpg:forbiddenIngList"');

if(nodes.length > 0){
	
	for(j in nodes){		
		
		//filIsGMO
		var prevValue = nodes[j].properties["bcpg:filIsGMO"];
		var value = null;
		
		if(prevValue == "Vrai"){
			value = true;
		}
		else if(prevValue == "Faux"){
			value = false;
		}
		else{
			value = null;
		}
		nodes[j].properties["bcpg:filIsGMO"] = value;
		
		//filIsIonized
		prevValue = nodes[j].properties["bcpg:filIsIonized"];
		value = null;
		
		if(prevValue == "Vrai"){
			value = true;
		}
		else if(prevValue == "Faux"){
			value = false;
		}
		else{
			value = null;
		}
		nodes[j].properties["bcpg:filIsIonized"] = value;
		
		nodes[j].save();		
	}		
}