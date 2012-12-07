package fr.becpg.repo.repository.model;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

public interface SimpleCharactDataItem extends RepositoryEntity{

	public void setCharactNodeRef(NodeRef nodeRef);

	public void setValue(Double value);

	public NodeRef getCharactNodeRef();

	public Double getValue();
}
