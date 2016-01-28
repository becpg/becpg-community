package fr.becpg.repo.product.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;

//200 gr de gruyère
//100 g petit pois
//2 tomates
//2 portions	

/**
 * 
 * @author matthieu
 * 
 */
public class CompositionLexer {

	public enum TokenType {

		QTY("[-+]?([0-9]*(\\.|,)[0-9]+|[0-9]+)"), PRODUCTNAME("(.*)^"), COMPOUNIT("([ \t\f\r\n]+)(kg|g|gr|ml|l|p|m2|m|perc|%)([ \t\f\r\n]+)"), WHITESPACE(
				"[ \t\f\r\n]+");

		public final String pattern;

		TokenType(String pattern) {
			this.pattern = pattern;
		}
	}

	public static class Token {
		public final TokenType type;
		public final String data;

		public Token(TokenType type, String data) {
			this.type = type;
			this.data = data;
		}

		@Override
		public String toString() {
			return String.format("(%s %s)", type.name(), data);
		}
	}

	public static CompoListDataItem lex(String input) {

		input = input.toLowerCase();
		// The tokens to return
		CompoListDataItem compoListDataItem = new CompoListDataItem();
		compoListDataItem.setCompoListUnit(CompoListUnit.P);

		// Lexer logic begins here
		StringBuilder tokenPatternsBuffer = new StringBuilder();
		for (TokenType tokenType : TokenType.values())
			tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
		Pattern tokenPatterns = Pattern.compile(new String(tokenPatternsBuffer.substring(1)));

		// Begin matching tokens
		Matcher matcher = tokenPatterns.matcher(input);
		StringBuffer productName = new StringBuffer();

		int findIdx = 0;
		while (matcher.find()) {
			if (matcher.group(TokenType.QTY.name()) != null && findIdx < 1) {
				compoListDataItem.setQtySubFormula(Double.valueOf(matcher.group(TokenType.QTY.name()).replace(",", ".").trim()));
				findIdx++;
				matcher.appendReplacement(productName, "");
				continue;
			} else if (matcher.group(TokenType.COMPOUNIT.name()) != null && findIdx == 1) {
				compoListDataItem.setCompoListUnit(extractUnit(matcher.group(TokenType.COMPOUNIT.name())));
				matcher.appendReplacement(productName, "");
				findIdx++;
				continue;
			} else if (matcher.group(TokenType.WHITESPACE.name()) != null) {
				matcher.appendReplacement(productName, " ");
				continue;
			}
		}
		matcher.appendTail(productName);

		compoListDataItem.setProduct(extractProduct(productName.toString()));
		return compoListDataItem;
	}

	public static List<CompoListDataItem> lexMultiLine(String input) {

		StringTokenizer st = new StringTokenizer(input, "\n");
		List<CompoListDataItem> compoList = new ArrayList<>();

		while (st.hasMoreElements()) {
			compoList.add(lex(st.nextToken()));
		}

		return compoList;
	}

	private static NodeRef extractProduct(String productName) {
		return BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).excludeDefaults().andPropEquals(ContentModel.PROP_NAME, productName)
				.singleValue();

	}

	private static CompoListUnit extractUnit(String unit) {

		switch (unit.trim()) {
		case "kg":
			return CompoListUnit.kg;
		case "g":
		case "gr":
			return CompoListUnit.g;
		case "l":
			return CompoListUnit.L;
		case "ml":
			return CompoListUnit.mL;
		case "cl":
			return CompoListUnit.mL;	
		case "p":
			return CompoListUnit.P;
		case "m":
			return CompoListUnit.m;
		case "m2":
			return CompoListUnit.m2;
		case "perc":
		case "%":
			return CompoListUnit.Perc;
		default:
			return CompoListUnit.P;
		}
	}

}
