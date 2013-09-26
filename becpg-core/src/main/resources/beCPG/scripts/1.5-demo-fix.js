var nodes = search.luceneSearch('+TYPE:"bcpg:productSpecification" -ASPECT:"bcpg:codeAspect"');

for each(var node in nodes) {
  if(node.hasAspect('bcpg:codeAspect') == false){
    node.addAspect('bcpg:codeAspect', null);
	node.save();
  }
}

nodes = search.luceneSearch('+TYPE:"bcpg:costList"');

for each(var node in nodes) {
  if(node.assocs["bcpg:costListCost"] == null){    
   	logger.log(node.name + " (" + node.typeShort + "): " + node.nodeRef);
    node.createAssociation(search.findNode("workspace://SpacesStore/5656650f-b306-414a-9326-1a6b6e43e1ac"), "bcpg:costListCost");
  }
}

// remove bcpg:compoListFather
/*
<association name="bcpg:compoListFather">
					<source>
						<mandatory>false</mandatory>
						<many>true</many>
					</source>
					<target>
						<class>bcpg:compoList</class>
						<mandatory>false</mandatory>
						<many>false</many>
					</target>
				</association>
*/
var nodes = search.luceneSearch('+TYPE:"bcpg:compoList"');

for each(var node in nodes) {
  
  var target = node.assocs["bcpg:compoListFather"];
  if(target != null){
	logger.log(node.nodeRef);
    node.removeAssociation(target[0], "bcpg:compoListFather");
  }
}

// remove costListDetails
/*
<association name="bcpg:costDetailsListSource">
					<source>
						<mandatory>true</mandatory>
						<many>true</many>
					</source>
					<target>
						<class>cm:content</class>
						<mandatory>true</mandatory>
						<many>false</many>
					</target>
				</association>
*/

var nodes = search.luceneSearch('+TYPE:"dl:dataList" +@dl\\:dataListItemType:"bcpg:costDetailsList"');

for each(var node in nodes) {
  
	node.remove();
}

// remove old versions (quiche with 1600 versions)
var version2CompanyHome = search.findNode("workspace://version2Store/8f486056-ece0-4c22-8333-48286b35c73c");

logger.log(version2CompanyHome.name + " (" + version2CompanyHome.typeShort + "): " + version2CompanyHome.nodeRef);

for each(var version in version2CompanyHome.children) {

  //logger.log(version.name + " (" + version.typeShort + "): " + version.nodeRef + " - " + version.children.length);
  
  var node = search.findNode("workspace://SpacesStore/" + version.name);
  if(node == null){
  	logger.log(version.name + " (" + version.typeShort + "): " + version.nodeRef);
    version.remove();
  }
}