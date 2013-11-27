
var entityNode = search.findNode(args.entityNodeRef);

var simulationNode = entityNode.copy(entityNode.parent,true);
   
simulationNode.properties["bcpg:productState"] =  "Simulation";
simulationNode.save();

model.simulationNode = simulationNode;