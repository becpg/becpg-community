// Declare namespace...
(function()
{
	
  // Define constructor...
  beCPG.custom.DocListTree = function CustomDocListTree_constructor(htmlId)
  {
    beCPG.custom.DocListTree.superclass.constructor.call(this, htmlId);
    return this;
  };

  // Extend default DocListTree...
  YAHOO.extend(beCPG.custom.DocListTree, Alfresco.DocListTree,
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
        
        if(beCPG.util.isEntity(p_oData)){
      	  treeNode.labelStyle = p_oData.type.replace(":", "-")+" ygtvlabel";
        }
        
        return treeNode;
     }
    
  
  });
})();