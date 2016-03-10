var node = search.findNode('workspace://SpacesStore/604afcb7-33b1-4c0a-85c3-7f088fd461ad');

var sourceNodes = node.sourceAssocs["bcpg:entityTplRef"];

if(sourceNodes !== null){
    for (var k = 0; k < sourceNodes.length; k++){
        logger.log(sourceNodes[k].name + " - " + sourceNodes[k].properties["bcpg:code"]);

        for each(var containerList in sourceNodes[k].children){                            
           if(containerList.name =="DataLists"){                
               for each(var list in containerList.children){
                   if(list.name == "compoList"){
                       logger.log("remove " + list.name);
                       list.remove();
                   }
               }
           }
        }
    }
}