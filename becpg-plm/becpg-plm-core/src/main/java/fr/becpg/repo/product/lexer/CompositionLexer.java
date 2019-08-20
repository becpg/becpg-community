package fr.becpg.repo.product.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
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

	private static Log logger = LogFactory.getLog(CompositionLexer.class);
	
	
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
		compoListDataItem.setCompoListUnit(ProductUnit.P);

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
				compoListDataItem.setCompoListUnit(ProductUnit.extractUnit(matcher.group(TokenType.COMPOUNIT.name())));
				matcher.appendReplacement(productName, "");
				findIdx++;
				continue;
			} else if (matcher.group(TokenType.WHITESPACE.name()) != null) {
				matcher.appendReplacement(productName, " ");
				continue;
			}
		}
		matcher.appendTail(productName);

		NodeRef productNodeRef = extractProduct(productName.toString());
		
		if(productNodeRef!=null){
			compoListDataItem.setProduct(productNodeRef);
		} else {
			logger.warn("No product found for name : "+productName.toString());
			return null;
		}
		
		compoListDataItem.setDeclType(DeclarationType.DoNotDetails);
		
		return compoListDataItem;
	}

	public static List<CompoListDataItem> lexMultiLine(String input) {

		StringTokenizer st = new StringTokenizer(input, "\n");
		List<CompoListDataItem> compoList = new ArrayList<>();

		while (st.hasMoreElements()) {
			CompoListDataItem ret = lex(st.nextToken());
			if(ret!=null){
				if(logger.isDebugEnabled()){
					logger.debug("Adding compolistItem : "+ret);
				}
				compoList.add(ret);
			}
		}

		return compoList;
	}

	private static NodeRef extractProduct(String productName) {
		return BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).excludeDefaults().inDBIfPossible().andPropEquals(ContentModel.PROP_NAME, productName.trim())
				.singleValue();
	}


}
