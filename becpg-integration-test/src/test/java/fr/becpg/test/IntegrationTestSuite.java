package fr.becpg.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.entity.EntityReportServiceTest;
import fr.becpg.test.repo.entity.datalist.MultiLevelDataServiceTest;
import fr.becpg.test.repo.listvalue.CompoListValuePluginTest;
import fr.becpg.test.repo.listvalue.ListValueServiceTest;
import fr.becpg.test.repo.product.CompareProductServiceTest;
import fr.becpg.test.repo.product.ProductServiceTest;
import fr.becpg.test.repo.product.lexer.CompositionLexerTest;
import fr.becpg.test.repo.project.NPDServiceTest;
import fr.becpg.test.repo.project.ProjectListSortTest;
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
	
		SearchWebScriptTest.class,
		
		//Compare
		CompareProductServiceTest.class,
		
		
		//Slow
		MultiLevelDataServiceTest.class,
		EntityReportServiceTest.class,
		
		
		EntityVersionWebScriptTest.class,
		
		
		//Bug
		CheckOutCheckInWebScriptTest.class,
	    RemoteEntityWebScriptTest.class,
		ExportSearchWebScriptTest.class,
		CompareProductReportWebScriptTest.class,
		
		
		//Project
		NPDServiceTest.class,
		ProjectListSortTest.class
})
public class IntegrationTestSuite {

}
