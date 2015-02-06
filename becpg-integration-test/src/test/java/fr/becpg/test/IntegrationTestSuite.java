package fr.becpg.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.project.ProjectListSortTest;
import fr.becpg.test.repo.entity.EntityReportServiceTest;
import fr.becpg.test.repo.entity.datalist.MultiLevelDataServiceTest;
import fr.becpg.test.repo.listvalue.CompoListValuePluginTest;
import fr.becpg.test.repo.listvalue.ListValueServiceTest;
import fr.becpg.test.repo.product.CompareProductServiceTest;
import fr.becpg.test.repo.product.ProductServiceTest;
import fr.becpg.test.repo.product.lexer.CompositionLexerTest;
import fr.becpg.test.repo.web.scripts.admin.AdminModuleWebScriptTest;
import fr.becpg.test.repo.web.scripts.entity.CheckOutCheckInWebScriptTest;
import fr.becpg.test.repo.web.scripts.entity.EntityDictionnaryWebScriptTest;
import fr.becpg.test.repo.web.scripts.entity.EntityListsWebScriptTest;
import fr.becpg.test.repo.web.scripts.entity.EntityVersionWebScriptTest;
import fr.becpg.test.repo.web.scripts.listvalue.AutoCompleteWebScriptTest;
import fr.becpg.test.repo.web.scripts.product.CompareProductReportWebScriptTest;
import fr.becpg.test.repo.web.scripts.product.ProductWUsedWebScriptTest;
import fr.becpg.test.repo.web.scripts.remote.RemoteEntityWebScriptTest;
import fr.becpg.test.repo.web.scripts.report.ExportSearchWebScriptTest;
import fr.becpg.test.repo.web.scripts.search.SearchWebScriptTest;

@RunWith(Suite.class)
@SuiteClasses(value={
		AdminModuleWebScriptTest.class,
		CompoListValuePluginTest.class,
		ListValueServiceTest.class,
		CompositionLexerTest.class,
	
		ProductServiceTest.class,
		EntityDictionnaryWebScriptTest.class,
		EntityListsWebScriptTest.class,
		AutoCompleteWebScriptTest.class,
		ProductWUsedWebScriptTest.class,
		RemoteEntityWebScriptTest.class,
		
		ExportSearchWebScriptTest.class,
		SearchWebScriptTest.class,
		
		//Compare
		CompareProductServiceTest.class,
		CompareProductReportWebScriptTest.class,
		
		//Slow
		ProjectListSortTest.class,
		MultiLevelDataServiceTest.class,
		EntityReportServiceTest.class,
		
		//Bug
		EntityVersionWebScriptTest.class,
		CheckOutCheckInWebScriptTest.class
})
public class IntegrationTestSuite {

}
