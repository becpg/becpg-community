/**
 * Main entry point: Return the model list
 * 
 * @method getData
 */
function getData()
{
   
			  
	  var items = search.query(
			      {
			         query: "+ASPECT:\"dsg:modelAspect\"",
			         limitResults: null,
			         sort: [
			              {
			                 column: "@cm:name",
			                 ascending: true
			              }],
			          language: "lucene",
			          templates: null,
			          namespace : null
			      });
			
		   
     return items;  

}

model.items = getData();