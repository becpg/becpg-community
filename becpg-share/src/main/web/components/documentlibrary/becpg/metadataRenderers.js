
if (Alfresco.DocumentList)
{  
    YAHOO.Bubbling.fire("registerRenderer",   
    {      
        propertyName: "suppliers",      
        renderer: function(record, label)      
        {         
           return "...";      
        }   
    });
    

    YAHOO.Bubbling.fire("registerRenderer",   
    {      
        propertyName: "clients",      
        renderer: function(record, label)      
        {         
           return "...";      
        }   
    });
    
    
}