// Declare namespace...
(function()
{
	
  // Define constructor...
  beCPG.custom.RepositoryDocListTree = function CustomRepositoryDocListTree_constructor(htmlId)
  {
    beCPG.custom.RepositoryDocListTree.superclass.constructor.call(this, htmlId);
    return this;
  };

  // Extend default RepositoryDocListTree...
  YAHOO.extend(beCPG.custom.RepositoryDocListTree, Alfresco.RepositoryDocListTree,
  {
	    /**
      * Build a tree node using passed-in data
      *
      * @method _buildTreeNode
      * @param p_oData {object} Object literal containing required data for new node
      * @param p_oParent {object} Optional parent node
      * @param p_expanded {object} Optional expanded/collaped state flag
      * @return {YAHOO.widget.TextNode} The new tree node
      */
     _buildTreeNode: function DLT__buildTreeNode(p_oData, p_oParent, p_expanded)
     {
        var treeNode =  new YAHOO.widget.TextNode(
        {
           label: p_oData.name,
           path: p_oData.path,
           nodeRef: p_oData.nodeRef,
           description: p_oData.description
        }, p_oParent, p_expanded);
        
        if(p_oData.aspects!=null && p_oData.aspects.indexOf("bcpg:entityListsAspect") > 0){
      	  treeNode.labelStyle = p_oData.type.replace(":", "-");
        }
        
        return treeNode;
     }
    
  });
})();