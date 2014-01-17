/**
 * Main entry point: Return the model list
 * 
 * @method getData
 */
function getData()
{
   
			  
	  var items = search.query(
			      {
			         query: '+ASPECT:"dsg:configAspect"',
			         language : 'lucene'
			      });
			
		   
     return items;  

}

model.items = getData();
