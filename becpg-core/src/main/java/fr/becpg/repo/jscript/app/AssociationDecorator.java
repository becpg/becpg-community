package fr.becpg.repo.jscript.app;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;

/**
 * 
 * @author matthieu
 *
 */
public interface AssociationDecorator {

	Set<QName> getAssociationNames();

	JSONAware decorate(QName qName, NodeRef nodeRef, List<NodeRef> targetAssocs);

	QName getAspect();

}
