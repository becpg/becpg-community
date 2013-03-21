package fr.becpg.repo.migration;

import org.alfresco.service.namespace.QName;

/**
 * Service to migrate model and data
 * @author quere
 *
 */
public interface MigrationService {

	public void addMandatoryAspectInMt(final QName type, final QName aspect);
	public void removeAspectInMt(final QName type, final QName aspect);
	public void migrateAssociationInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
	public void migratePropertyInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
}
