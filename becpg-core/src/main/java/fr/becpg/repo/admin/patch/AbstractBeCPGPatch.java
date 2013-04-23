package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;

public abstract class AbstractBeCPGPatch  extends AbstractPatch{

   protected Repository repository;
	
    
    public void setRepository(Repository repository) {
		this.repository = repository;
	}



    protected NodeRef searchFolder(String xpath)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            // the node does not exist
            return null;
        }
        else
        {
            return nodeRefs.get(0);
        }
    }
    
}
