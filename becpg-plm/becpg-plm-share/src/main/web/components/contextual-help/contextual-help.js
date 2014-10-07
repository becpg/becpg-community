/**
 * ContextualHelp component.
 * 
 * @namespace beCPG
 * @class beCPG.component.ContextualHelp
 */
(function()
{
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;

    /**
     * ContextualHelp constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.component.ContextualHelp} The new ContextualHelp instance
     * @constructor
     */
    beCPG.component.ContextualHelp = function ContextualHelp_constructor(htmlId)
    {
        beCPG.component.ContextualHelp.superclass.constructor.call(this, "beCPG.component.ContextualHelp", htmlId,
                [ "button" ]);
        return this;
    };

    YAHOO.extend(beCPG.component.ContextualHelp, Alfresco.component.Base,
    {
        /**
         * Fired by YUI when parent element is available for scripting.
         * Component initialisation, including instantiation of YUI widgets and
         * event listener binding.
         * 
         * @method onReady
         */
        onReady : function ContextualHelp_onReady()
        {
//
//            Event.onAvailable('HEADER_MY_FILES', function()
//            {
//                var balloon = Alfresco.util.createBalloon("HEADER_MY_FILES",
//                {
//                    text : "Pweet"
//                });
//                balloon.show();
//            });
            
            
           var node =  YAHOO.util.Selector.query('.DocListTree div.create-content',null, true)
           if(node!=null){
               this.createBalloon(node,"help.button.create")
           }
           
           
          //Document view
           
           node =  YAHOO.util.Selector.query(".node-header div.entity-view-details",null,true);
           if(node!=null){
               this.createBalloon(node,"help.button.entity-view-details",{target:true,showOn:"creation",tipJoint:"top left" })
           }
           
          
           
           //Charact view
          
           node =  YAHOO.util.Selector.query(".datalist-toolbar > div.entity-view-details",null,true);
           if(node!=null){
               this.createBalloon(node,"help.button.entity-view-details",{target:true,showOn:"creation",tipJoint:"top left" })
           }
           
           node =  YAHOO.util.Selector.query(".datalist-toolbar > div.entity-view-documents",null,true);
           if(node!=null){
               this.createBalloon(node,"help.button.entity-view-documents",{target:true,showOn:"creation",tipJoint:"top left"})
           }
           
           
           //View EntityDetails
           
           node =  YAHOO.util.Selector.query(".node-header div.entity-view-datalist",null,true);
           if(node!=null){
               this.createBalloon(node,"help.button.entity-view-datalist",{target:true,showOn:"creation",tipJoint:"bottom left"})
           }
                   
           node =  YAHOO.util.Selector.query(".node-header div.entity-view-documents",null,true);
           if(node!=null){
               this.createBalloon(node,"help.button.entity-view-documents",{target:true,showOn:"creation",tipJoint:"top left"})
           }  
           
           
           
           
           
                   
                   
        }, 
        createBalloon : function ContextualHelp_createBalloon(node, msg, options){
           
            var balloon =  new Opentip(node,options);
            balloon.setContent(this.msg(msg));
            balloon.show();
            
//            
//            
//            var balloon = Alfresco.util.createBalloon(node,
//                    {
//                        text : this.msg(msg)
//                    });
//            
//            balloon.hideOthers = function(){alert("PWET")};
//            
           // balloon.show();
            
            
        }

    });
})();
