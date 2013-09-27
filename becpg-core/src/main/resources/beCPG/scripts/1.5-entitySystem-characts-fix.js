var characts =[ {oldCharact:"workspace://SpacesStore/5ddecb51-5bf0-4a93-8981-eb52650e3a18", newCharact:"workspace://SpacesStore/56566520-888e-4ac2-a23f-b8439593a46d"}, 
                      {oldCharact:"workspace://SpacesStore/db30adbd-edd8-45b5-b1bd-847b3b73d65f", newCharact:"workspace://SpacesStore/39baa240-4cd5-436a-92c7-1f02c7d337e1"},
                      {oldCharact:"workspace://SpacesStore/58b0a07d-b6cc-49cd-8bd1-a241f12c4003", newCharact:"workspace://SpacesStore/5a340783-4050-4c3f-9c16-6e68db3271cc"}];

var assocName="bcpg:pclPhysicoChem";

for(row in characts){
	
	var versionedNode = search.findNode(characts[row].oldCharact);
	var repositoryNode = search.findNode(characts[row].newCharact);

	if(versionedNode !=null && repositoryNode != null){
		
		var sourceNodes = versionedNode.sourceAssocs[assocName];
		
		if(sourceNodes != null)
		{
			for (var k = 0; k < sourceNodes.length; k++)
			{
				logger.log("node " + sourceNodes[k].name + " replace charact " + versionedNode.name + " by " + repositoryNode.name);
				sourceNodes[k].removeAssociation(versionedNode, assocName);
				sourceNodes[k].createAssociation(repositoryNode, assocName);
			}
		}
	}
	
}