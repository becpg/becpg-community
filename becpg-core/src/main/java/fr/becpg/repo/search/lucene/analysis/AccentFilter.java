package fr.becpg.repo.search.lucene.analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * <p>AccentFilter class.</p>
 *
 * @author matthieu
 */
public class AccentFilter extends TokenFilter {
	
	private static final HashMap<String,String> substitutions = initSubstitutions();

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/**
	 * <p>Constructor for AccentFilter.</p>
	 *
	 * @param input a {@link org.apache.lucene.analysis.TokenStream} object
	 */
	public AccentFilter(TokenStream input) {
		super(input);
	}

	/** {@inheritDoc} */
	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			killAccents(termAtt.buffer());
			return true;
		}
		
		return false;
	}

	/**
	 * <p>killAccents.</p>
	 *
	 * @param chars an array of {@link char} objects
	 */
	public void killAccents(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (substitutions.containsKey("" + chars[i])) {
				chars[i] = substitutions.get(("" + chars[i])).charAt(0);
			}
		}
	}
	
	private static HashMap<String,String> initSubstitutions() {
		HashMap<String,String> retVal = new HashMap<>(12);

		retVal.put("Â", "A");
		retVal.put("Ç", "C");
		retVal.put("È", "E");
		retVal.put("É", "E");
		retVal.put("Ê", "E");
		retVal.put("Ë", "E");
		retVal.put("Î", "I");
		retVal.put("Ï", "I");
		retVal.put("Ù", "U");
		retVal.put("Û", "U");
		retVal.put("à", "a");
		retVal.put("â", "a");
		retVal.put("ç", "c");
		retVal.put("è", "e");
		retVal.put("é", "e");
		retVal.put("ê", "e");
		retVal.put("ë", "e");
		retVal.put("î", "i");
		retVal.put("ï", "i");
		retVal.put("ô", "o");
		retVal.put("ù", "u");
		retVal.put("û", "u");
		retVal.put("Ǻ", "A");
		retVal.put("ǻ", "a");
		retVal.put("Ǽ", "Æ");
		retVal.put("ǽ", "æ");
		retVal.put("Ǿ", "Ø");
		retVal.put("ǿ", "ø");
		retVal.put("Á", "A");
		retVal.put("Ã", "A");
		retVal.put("Ä", "A");
		retVal.put("Å", "A");
		retVal.put("Ì", "I");
		retVal.put("Í", "I");
		retVal.put("Ð", "D");
		retVal.put("Ñ", "N");
		retVal.put("Ò", "O");
		retVal.put("Ó", "O");
		retVal.put("Ô", "O");
		retVal.put("Õ", "O");
		retVal.put("Ö", "O");
		retVal.put("Ú", "U");
		retVal.put("Ü", "U");
		retVal.put("Ý", "Y");
		retVal.put("á", "a");
		retVal.put("ã", "a");
		retVal.put("ä", "a");
		retVal.put("å", "a");
		retVal.put("ì", "i");
		retVal.put("í", "i");
		retVal.put("ñ", "n");
		retVal.put("ò", "o");
		retVal.put("ó", "o");
		retVal.put("õ", "o");
		retVal.put("ö", "o");
		retVal.put("ú", "u");
		retVal.put("ü", "u");
		retVal.put("ý", "y");
		retVal.put("ÿ", "y");
		retVal.put("Ā", "A");
		retVal.put("ā", "a");
		retVal.put("Ă", "A");
		retVal.put("ă", "a");
		retVal.put("Ą", "A");
		retVal.put("ą", "a");
		retVal.put("Ć", "C");
		retVal.put("ć", "c");
		retVal.put("Ĉ", "C");
		retVal.put("ĉ", "c");
		retVal.put("Ċ", "C");
		retVal.put("ċ", "c");
		retVal.put("Č", "C");
		retVal.put("č", "c");
		retVal.put("Ď", "D");
		retVal.put("ď", "d");
		retVal.put("Đ", "D");
		retVal.put("đ", "d");
		retVal.put("Ē", "E");
		retVal.put("ē", "e");
		retVal.put("Ĕ", "E");
		retVal.put("ĕ", "e");
		retVal.put("Ė", "E");
		retVal.put("ė", "e");
		retVal.put("Ę", "E");
		retVal.put("ę", "e");
		retVal.put("Ě", "E");
		retVal.put("ě", "e");
		retVal.put("Ĝ", "G");
		retVal.put("ĝ", "g");
		retVal.put("Ğ", "G");
		retVal.put("ğ", "g");
		retVal.put("Ġ", "G");
		retVal.put("ġ", "g");
		retVal.put("Ģ", "G");
		retVal.put("ģ", "g");
		retVal.put("Ĥ", "H");
		retVal.put("Ħ", "H");
		retVal.put("ħ", "h");
		retVal.put("Ĩ", "I");
		retVal.put("ĩ", "I");
		retVal.put("Ī", "I");
		retVal.put("ī", "i");
		retVal.put("Ĭ", "I");
		retVal.put("ĭ", "i");
		retVal.put("Į", "I");
		retVal.put("į", "i");
		retVal.put("İ", "I");
		retVal.put("Ĵ", "J");
		retVal.put("ĵ", "j");
		retVal.put("Ķ", "K");
		retVal.put("ķ", "k");
		retVal.put("Ĺ", "L");
		retVal.put("ĺ", "I");
		retVal.put("Ļ", "I");
		retVal.put("ļ", "I");
		retVal.put("Ľ", "L");
		retVal.put("ľ", "l");
		retVal.put("Ŀ", "L");
		retVal.put("ŀ", "l");
		retVal.put("Ł", "L");
		retVal.put("ł", "l");
		retVal.put("Ń", "N");
		retVal.put("ń", "n");
		retVal.put("Ņ", "N");
		retVal.put("ņ", "n");
		retVal.put("Ň", "N");
		retVal.put("ň", "n");
		retVal.put("ŉ", "n");
		retVal.put("Ō", "O");
		retVal.put("ō", "o");
		retVal.put("Ŏ", "O");
		retVal.put("ŏ", "o");
		retVal.put("Ő", "O");
		retVal.put("ő", "o");
		retVal.put("Ŕ", "R");
		retVal.put("ŕ", "r");
		retVal.put("Ŗ", "R");
		retVal.put("ŗ", "r");
		retVal.put("Ř", "R");
		retVal.put("ř", "r");
		retVal.put("Ś", "S");
		retVal.put("ś", "s");
		retVal.put("Ŝ", "S");
		retVal.put("ŝ", "s");
		retVal.put("Ş", "S");
		retVal.put("ş", "s");
		retVal.put("Š", "S");
		retVal.put("š", "s");
		retVal.put("Ţ", "T");
		retVal.put("ţ", "t");
		retVal.put("Ť", "T");
		retVal.put("ť", "t");
		retVal.put("Ŧ", "T");
		retVal.put("ŧ", "t");
		retVal.put("Ũ", "U");
		retVal.put("ũ", "u");
		retVal.put("Ū", "U");
		retVal.put("ū", "u");
		retVal.put("Ŭ", "U");
		retVal.put("ŭ", "u");
		retVal.put("Ů", "U");
		retVal.put("ů", "u");
		retVal.put("Ű", "U");
		retVal.put("ű", "u");
		retVal.put("Ų", "U");
		retVal.put("ų", "u");
		retVal.put("Ŵ", "W");
		retVal.put("ŵ", "w");
		retVal.put("Ŷ", "Y");
		retVal.put("ŷ", "y");
		retVal.put("Ÿ", "Y");
		retVal.put("Ź", "Z");
		retVal.put("ź", "z");
		retVal.put("Ż", "Z");
		retVal.put("ż", "z");
		retVal.put("Ž", "Z");
		retVal.put("ž", "z");
		retVal.put("'", " ");

		return retVal;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		
		AccentFilter fobj = (AccentFilter) obj;
		
		return termAtt.equals(fobj.termAtt);
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(termAtt);
	}
}
