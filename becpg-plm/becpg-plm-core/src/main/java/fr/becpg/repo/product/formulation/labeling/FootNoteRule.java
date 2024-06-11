package fr.becpg.repo.product.formulation.labeling;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>FootNoteRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FootNoteRule extends AbstractFormulaFilterRule implements Comparable<FootNoteRule>, Serializable {

	private static final long serialVersionUID = 4589879904230809954L;

	private final String footNoteMarker;

	private final MLText footNoteLabel;

	private final int sort;

	/**
	 * <p>Constructor for FootNoteRule.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object
	 * @param footNoteLabel a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param formula a {@link java.lang.String} object
	 * @param locales a {@link java.util.List} object
	 * @param sort a int
	 */
	public FootNoteRule(String ruleName, MLText footNoteLabel, String formula, List<String> locales, int sort) {
		super(ruleName, formula, locales);
		String label = MLTextHelper.getClosestValue(footNoteLabel, I18NUtil.getLocale());
		if(label!=null && label.contains("|")) {
			this.footNoteMarker = label.split("\\|")[0];
		}else  {
			footNoteMarker = "*";
		}
		
		this.footNoteLabel = footNoteLabel;
		this.sort = sort;

	}

	/**
	 * <p>Getter for the field <code>footNoteMarker</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFootNoteMarker() {
		return footNoteMarker;
	}

	/**
	 * <p>Getter for the field <code>footNoteLabel</code>.</p>
	 *
	 * @param locale a {@link java.util.Locale} object
	 * @return a {@link java.lang.String} object
	 */
	public String getFootNoteLabel(Locale locale) {
		String label = MLTextHelper.getClosestValue(footNoteLabel, locale);
		if(label!=null && label.contains("|")) {
			return label.split("\\|")[1];
		}
		return label;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(FootNoteRule b) {
		return Integer.compare(this.sort, b.sort);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(footNoteLabel, footNoteMarker, sort);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		FootNoteRule other = (FootNoteRule) obj;
		return Objects.equals(footNoteLabel, other.footNoteLabel) && Objects.equals(footNoteMarker, other.footNoteMarker) && (sort == other.sort);
	}

}
