package fr.becpg.repo.form.column.decorator;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;

public interface DataGridFormFieldTitleProvider extends ExcelFieldTitleProvider {
    boolean isAllowed(QName field);
    String getTitle(QName field);
}
