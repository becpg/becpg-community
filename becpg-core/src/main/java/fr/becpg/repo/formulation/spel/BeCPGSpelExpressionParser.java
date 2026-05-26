package fr.becpg.repo.formulation.spel;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * <p>BeCPGSpelExpressionParser class.</p>
 *
 * @author matthieu
 */
public class BeCPGSpelExpressionParser extends SpelExpressionParser {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(BeCPGSpelExpressionParser.class);

	/** Constant <code>FORBIDDEN_METHODS</code> */
	private static final Set<String> FORBIDDEN_METHODS = Set.of("getBean", "executeScript", "executeScriptString",
			"call", "createNewFile", "eval", "exec",
			"exit", "forName", "gc", "getByName", "getDeclaredConstructor",
			"getDeclaredConstructors", "getDeclaredFields", "getDeclaredMethod", "getField",
			"getFields", "getHostName", "getLocalHost", "getMethod", "getMethods",
			"getProperties", "getRuntime", "getSystemClassLoader", "getenv", "invoke",
			"invokeFunction", "load", "loadLibrary", "mkdir", "mkdirs",
			"newInstance", "openConnection", "read",
			"readObject", "setAccessible", "start", "write", "writeObject");

	/** Constant <code>FORBIDDEN_FIELDS</code> */
	private static final Set<String> FORBIDDEN_FIELDS = Set.of(
			"class", "classLoader", "declaredFields", "declaredMethods", "declaredConstructors", "declaredClasses");

	/**
	 * <p>Constructor for BeCPGSpelExpressionParser.</p>
	 */
	public BeCPGSpelExpressionParser() {
		super();
	}

	/**
	 * <p>Constructor for BeCPGSpelExpressionParser.</p>
	 *
	 * @param config a {@link org.springframework.expression.spel.SpelParserConfiguration} object
	 */
	public BeCPGSpelExpressionParser(SpelParserConfiguration config) {
		super(config);
	}

	/** {@inheritDoc} */
	@Override
	public Expression parseExpression(String expressionString) throws ParseException {
		Expression expr = super.parseExpression(expressionString);
		if (expr instanceof SpelExpression spelExpr) {
			checkAst(spelExpr.getAST());
		}
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public Expression parseExpression(String expressionString, ParserContext context) throws ParseException {
		Expression expr = super.parseExpression(expressionString, context);
		if (expr instanceof SpelExpression spelExpr) {
			checkAst(spelExpr.getAST());
		}
		return expr;
	}

	/**
	 * <p>checkAst.</p>
	 *
	 * @param node a {@link org.springframework.expression.spel.SpelNode} object
	 */
	private void checkAst(SpelNode node) {
		if (node == null) {
			return;
		}
		if (node instanceof MethodReference ref && FORBIDDEN_METHODS.contains(ref.getName())) {
			logger.error("Expression contains unsafe method: " + ref.getName());
			throw new EvaluationException("Expression contains unsafe method: " + ref.getName());
		}
		if (node instanceof PropertyOrFieldReference ref && FORBIDDEN_FIELDS.contains(ref.getName())) {
			logger.error("Expression contains unsafe field: " + ref.getName());
			throw new EvaluationException("Expression contains unsafe field: " + ref.getName());
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			checkAst(node.getChild(i));
		}
	}

}
