package fr.becpg.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.autocomplete.TargetAssocAutoCompletePluginIT;
import fr.becpg.test.repo.autocomplete.CompoListAutoCompletePluginIT;
import fr.becpg.test.repo.entity.EntityReportServiceIT;
import fr.becpg.test.repo.entity.datalist.MultiLevelDataServiceIT;
import fr.becpg.test.repo.product.CompareDocumentServiceIT;
import fr.becpg.test.repo.product.CompareProductServiceIT;
import fr.becpg.test.repo.product.ProductServiceIT;
import fr.becpg.test.repo.product.lexer.CompositionLexerIT;
import fr.becpg.test.repo.project.NPDServiceIT;
import fr.becpg.test.repo.project.ProjectListSortIT;
import fr.becpg.test.repo.web.scripts.admin.AdminModuleWebScriptIT;
import fr.becpg.test.repo.web.scripts.entity.EntityDictionaryWebScriptIT;
import fr.becpg.test.repo.web.scripts.entity.EntityListsWebScriptIT;
import fr.becpg.test.repo.web.scripts.entity.EntityVersionWebScriptIT;
import fr.becpg.test.repo.web.scripts.listvalue.AutoCompleteWebScriptIT;
import fr.becpg.test.repo.web.scripts.product.CompareProductReportWebScriptIT;
import fr.becpg.test.repo.web.scripts.product.ProductWUsedWebScriptIT;
import fr.becpg.test.repo.web.scripts.remote.RemoteEntityWebScriptIT;
import fr.becpg.test.repo.web.scripts.report.ExportSearchWebScriptIT;
import fr.becpg.test.repo.web.scripts.search.SearchWebScriptIT;

@RunWith(Suite.class)
@SuiteClasses(value = { AdminModuleWebScriptIT.class, CompoListAutoCompletePluginIT.class, TargetAssocAutoCompletePluginIT.class, CompositionLexerIT.class,

		ProductServiceIT.class, EntityDictionaryWebScriptIT.class, EntityListsWebScriptIT.class, AutoCompleteWebScriptIT.class,
		ProductWUsedWebScriptIT.class,

		SearchWebScriptIT.class,

		// Compare
		CompareProductServiceIT.class, CompareDocumentServiceIT.class,

		// Slow
		MultiLevelDataServiceIT.class, EntityReportServiceIT.class,

		EntityVersionWebScriptIT.class,

		// Bug
		RemoteEntityWebScriptIT.class, ExportSearchWebScriptIT.class, CompareProductReportWebScriptIT.class,

		// Project
		NPDServiceIT.class, ProjectListSortIT.class })
public class IntegrationTestSuite {

}
