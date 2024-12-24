package fr.becpg.repo.formulation.spel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * <p>BeCPGSpelExpressionParser class.</p>
 *
 * @author matthieu
 */
public class BeCPGSpelExpressionParser extends SpelExpressionParser {

	private static final Log logger = LogFactory.getLog(BeCPGSpelExpressionParser.class);

	private static final String[] UNSAFE_PATTERNS = { ".forName(", ".invoke(", ".getDeclaredMethod(", ".getMethod(", ".getMethods(", ".exec(",
			".getDeclaredFields(", ".getField(", ".getFields(", ".setAccessible(", ".getDeclaredConstructors(", ".getDeclaredConstructor(", ".start(",
			".loadLibrary(", ".load(", ".call(", ".read(", ".write(", ".delete(", ".mkdir(", ".mkdirs(", ".createNewFile(", ".exit(", ".gc(",
			".currentTimeMillis(", ".nanoTime(", ".wait(", ".notify(", ".notifyAll(", ".sleep(", ".join(", ".yield(", ".openConnection(",
			".getHostName(", ".getByName(", ".getLocalHost(", "Runtime.getRuntime(", "System.getProperties(", "System.getenv(",
			"ClassLoader.getSystemClassLoader(", "URLClassLoader.newInstance(", "Files.delete(", "Files.copy(", "Files.move(", ".eval(",
			".executeScript(", ".invokeFunction(", ".readObject(", ".writeObject(" };
	
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
		checkExpression(expressionString);
		return super.parseExpression(expressionString);
	}

	/** {@inheritDoc} */
	@Override
	public Expression parseExpression(String expressionString, ParserContext context) throws ParseException {
		checkExpression(expressionString);
		return super.parseExpression(expressionString, context);
	}

	private void checkExpression(String expression) {
		for (String pattern : UNSAFE_PATTERNS) {
			if (expression.contains(pattern)) {
				logger.error("Expression is unsafe: " + expression);
				throw new EvaluationException("Expression is unsafe: " + expression);
			}
		}
	}

}
