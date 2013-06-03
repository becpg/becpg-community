function createTags(nodeName, tagName)
{
	for(var i=0 ; i<1200 ; i++){
		var node = space.createNode(nodeName+i, "cm:content");
		node.addAspect("cm:taggable");
		node.addTag(tagName);
	}

}

createTags("nodeA-", "tag1");
createTags("nodeB-", "tag2");


