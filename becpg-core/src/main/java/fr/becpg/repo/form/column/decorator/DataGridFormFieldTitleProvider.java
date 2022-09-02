package fr.becpg.repo.form.column.decorator;

import org.alfresco.service.namespace.QName;

/**
 * <p>DataGridFormFieldTitleProvider interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DataGridFormFieldTitleProvider {
    /**
     * <p>isAllowed.</p>
     *
     * @param field a {@link org.alfresco.service.namespace.QName} object.
     * @return a boolean.
     */
    boolean isAllowed(QName field);
    /**
     * <p>getTitle.</p>
     *
     * @param field a {@link org.alfresco.service.namespace.QName} object.
     * @return a {@link java.lang.String} object.
     */
    String getTitle(QName field);
}
