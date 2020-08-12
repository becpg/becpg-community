package fr.becpg.repo.form.column.decorator;

import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl;
import org.alfresco.service.namespace.QName;

public interface DataGridFormFieldTitleProvider extends ExcelFieldTitleProvider {
    boolean isAllowed(QName field);
    String getTitle(QName field);
}
