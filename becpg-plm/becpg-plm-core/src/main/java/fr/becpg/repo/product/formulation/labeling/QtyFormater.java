package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>QtyFormater class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class QtyFormater {

	private static Log logger = LogFactory.getLog(QtyFormater.class);

	private DecimalFormat decimalFormat;
	private RoundingMode roundingMode;
	private String qtyFormula;

	/**
	 * <p>Constructor for QtyFormater.</p>
	 *
	 * @param decimalFormat a {@link java.text.DecimalFormat} object
	 * @param roundingMode a {@link java.math.RoundingMode} object
	 * @param qtyFormula a {@link java.lang.String} object
	 */
	public QtyFormater(DecimalFormat decimalFormat, RoundingMode roundingMode, String qtyFormula) {
		super();
		this.decimalFormat = decimalFormat;
		this.roundingMode = roundingMode;
		this.qtyFormula = qtyFormula;
	}

	/**
	 * <p>Getter for the field <code>decimalFormat</code>.</p>
	 *
	 * @return a {@link java.text.DecimalFormat} object
	 */
	public DecimalFormat getDecimalFormat() {
		return decimalFormat;
	}

	/**
	 * <p>Getter for the field <code>roundingMode</code>.</p>
	 *
	 * @return a {@link java.math.RoundingMode} object
	 */
	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	/**
	 * <p>Getter for the field <code>qtyFormula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getQtyFormula() {
		return qtyFormula;
	}

	/**
	 * <p>format.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link java.lang.String} object
	 */
	public String format(Double qty) {
		return decimalFormat.format(apply(qty));
	}

	/**
	 * <p>apply.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double apply(Double qtyPerc) {
		if (qtyPerc != null && "NEAREST_HALF_DOWN".equals(qtyFormula)) {
			double rounded = Math.floor(qtyPerc * 200) / 200.0;
			// Si le nombre est exactement au milieu entre deux arrondis (x.5), il reste tel quel
			if (qtyPerc * 200 == Math.floor(qtyPerc * 200) + 1) {
				return qtyPerc;
			}
			return rounded;
		} else if (qtyPerc != null && "NEAREST_HALF_UP".equals(qtyFormula)) {
			return Math.ceil(qtyPerc * 200) / 200.0;
		}
		return qtyPerc;
	}

	/**
	 * <p>round.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double round(Double qty) {

		if ((qty != null) && (qty > -1) && (qty != 0d)) {
			String roundedQty = decimalFormat.format(qty);
			try {
				decimalFormat.setParseBigDecimal(true);
				return decimalFormat.parse(roundedQty).doubleValue();

			} catch (ParseException e) {
				logger.error(e, e);
			}
		}
		return qty != null ? qty : 0d;

	}

}
