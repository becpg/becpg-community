package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QtyFormater {

	private static Log logger = LogFactory.getLog(QtyFormater.class);

	private DecimalFormat decimalFormat;
	private RoundingMode roundingMode;
	private String qtyFormula;

	public QtyFormater(DecimalFormat decimalFormat, RoundingMode roundingMode, String qtyFormula) {
		super();
		this.decimalFormat = decimalFormat;
		this.roundingMode = roundingMode;
		this.qtyFormula = qtyFormula;
	}

	public DecimalFormat getDecimalFormat() {
		return decimalFormat;
	}

	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	public String getQtyFormula() {
		return qtyFormula;
	}

	public String format(Double qty) {
		return decimalFormat.format(apply(qty));
	}

	public Double apply(Double qtyPerc) {
		if(qtyPerc!=null && "NEAREST_HALF_DOWN".equals(qtyFormula)) {
	        return Math.round(qtyPerc * 200) / 200.0;
		} else if(qtyPerc!=null && "NEAREST_HALF_UP".equals(qtyFormula) ) {
			return Math.ceil(qtyPerc * 200) / 200.0;
		}
		return qtyPerc;
	}

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
