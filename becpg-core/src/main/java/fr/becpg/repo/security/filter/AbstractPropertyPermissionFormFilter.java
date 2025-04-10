/*
 * 
 */
package fr.becpg.repo.security.filter;

import java.util.Iterator;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.security.SecurityService;

/**
 * Check property permission according to securityService
 *
 * @param <ItemType>
 *            the generic type
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractPropertyPermissionFormFilter<ItemType> extends AbstractFilter<ItemType, NodeRef> {

	private static final Log logger = LogFactory.getLog(AbstractPropertyPermissionFormFilter.class);

	private SecurityService securityService;

	/**
	 * The namespace prefix resolver for resolving namespace prefixes.
	 */
	protected NamespacePrefixResolver namespacePrefixResolver;

	/**
	 * <p>Setter for the field <code>namespacePrefixResolver</code>.</p>
	 *
	 * @param namespacePrefixResolver a {@link org.alfresco.service.namespace.NamespacePrefixResolver} object.
	 */
	public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>filterFormFields.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @param form a {@link org.alfresco.repo.forms.Form} object.
	 * @param item a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	protected void filterFormFields(NodeRef item, QName nodeType, Form form) {
		if (form != null && form.getFieldDefinitions() != null) {
			Iterator<FieldDefinition> it = form.getFieldDefinitions().iterator();
			while (it.hasNext()) {
				FieldDefinition fieldDefinition = it.next();
				int accessMode = securityService.computeAccessMode(item, nodeType, fieldDefinition.getName());

				switch (accessMode) {
				case SecurityService.READ_ACCESS:
					if (logger.isDebugEnabled()) {
						logger.debug("Mark as read only :" + fieldDefinition.getName());
					}
					fieldDefinition.setProtectedField(true);
					break;
				case SecurityService.NONE_ACCESS:
					if (logger.isDebugEnabled()) {
						logger.debug("Remove field from form :" + fieldDefinition.getName());
					}
					it.remove();
					break;
				default:
					break;
				}

			}
		}

	}

}
