/**
 * Main entry point: Return the model list
 * 
 * @method getData
 */
function getData()
{
   
			  
	  var items = search.query(
			      {
			         query: '+ASPECT:"dsg:modelAspect"',
			         language : 'lucene'
			      });
			
		   
     return items;  

}

model.items = getData();