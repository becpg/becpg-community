package fr.becpg.repo.workflow;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Unit tests of the mail helper of the workflow script <code>process-utils.js</code>, run with Rhino against stubbed
 * root objects.
 */
public class ProcessUtilsScriptTest {

	private static final String PROCESS_UTILS_PATH = "alfresco/module/becpg-plm-core/workflow/process-utils.js";

	private static final String TEMPLATE_PATH = "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-approved-task-email.ftl";

	private static final String MESSAGE_KEY = "productValidationWF.mail.approved.message";

	private static final String SUBJECT_KEY = "productValidationWF.mail.approved.subject";

	private static final String STUBS = """
			var mails = [];
			var errors = [];
			var logger = { error: function(message) { errors.push(message); } };
			var task = { id: '42', dueDate: null, priority: 2, assignee: 'initiator' };
			var bpm_workflowDescription = 'Product validation - Product 1';
			var people = { getPerson: function(userName) { return { properties: { 'bcpg:userLocale': 'fr' } }; } };
			var bcpg = {
				extractPeople: function(authorities) { return ['initiator']; },
				getLocalizedMessage: function(key, locale) { return key == 'productValidationWF.mail.approved.message' ? 'Produit valide' : null; },
				sendMLAwareMail: function(authorities, fromEmail, subjectKey, subjectParams, templatePath, templateModel) {
					mails.push({ subjectKey: subjectKey, subjectParam: subjectParams[0], templatePath: templatePath,
						workflowTitle: templateModel.args.workflowTitle, workflowDocuments: templateModel.args.workflowDocuments });
				}
			};
			""";

	private Context context;

	private ScriptableObject scope;

	@Before
	public void loadScript() throws IOException {
		context = Context.enter();
		scope = context.initStandardObjects();
		evaluate(STUBS);
		evaluate(readProcessUtils());
	}

	@After
	public void exitContext() {
		Context.exit();
	}

	@Test
	public void legacySixArgumentCallUsesTheTemplatePath() {
		evaluate("sendMail(task.assignee, null, 'Validated: Product 1', 'Product validated', '" + TEMPLATE_PATH + "', 'documents');");

		assertEquals("0", evaluate("errors.length"));
		assertEquals(TEMPLATE_PATH, evaluate("mails[0].templatePath"));
		assertEquals("documents", evaluate("mails[0].workflowDocuments"));
	}

	@Test
	public void legacySixArgumentCallKeepsTheLocalizedSubjectAndMessage() {
		evaluate("sendMail(task.assignee, null, 'Validated: Product 1', 'Product validated', '" + TEMPLATE_PATH + "', 'documents');");

		assertEquals("Validated: Product 1", evaluate("mails[0].subjectKey"));
		assertEquals("null", evaluate("String(mails[0].subjectParam)"));
		assertEquals("Product validated", evaluate("mails[0].workflowTitle"));
	}

	@Test
	public void currentSevenArgumentCallIsUnchanged() {
		evaluate("sendMail(task.assignee, null, '" + SUBJECT_KEY + "', bpm_workflowDescription, '" + MESSAGE_KEY + "', '" + TEMPLATE_PATH
				+ "', 'documents');");

		assertEquals("0", evaluate("errors.length"));
		assertEquals(SUBJECT_KEY, evaluate("mails[0].subjectKey"));
		assertEquals("Product validation - Product 1", evaluate("mails[0].subjectParam"));
		assertEquals(TEMPLATE_PATH, evaluate("mails[0].templatePath"));
		assertEquals("Produit valide", evaluate("mails[0].workflowTitle"));
		assertEquals("documents", evaluate("mails[0].workflowDocuments"));
	}

	private String evaluate(String script) {
		return Context.toString(context.evaluateString(scope, script, "test", 1, null));
	}

	private String readProcessUtils() throws IOException {
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(PROCESS_UTILS_PATH)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
