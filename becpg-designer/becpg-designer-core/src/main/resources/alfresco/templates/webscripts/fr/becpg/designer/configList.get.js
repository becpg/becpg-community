/**
 * Main entry point: Return the model list
 * 
 * @method getData
 */
function getData()
{
   
			  
	  var items = search.query(
			      {
			         query: '+ASPECT:"dsg:configAspect" -@\\{http\\://www.alfresco.org/model/content/1.0\\}name:("-config-custom.xml")',
			         language : 'lucene'
			      });
			
		   
     return items;  

}

model.items = getData();
