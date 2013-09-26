var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;
var state = args.state;

var node = search.findNode(nodeRef);
if(node!=null  && state!=null){

   node.properties["bcpg:entityDataListState"] =   state;
   node.save();
    
}


model.newValue = state;