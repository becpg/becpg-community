package fr.becpg.repo.product.formulation.labeling;

import java.util.List;

/**
 * <p>ForcePercentageRule class.</p>
 *
 * @author matthieu
 */
class ForcePercentageRule extends AbstractFormulaFilterRule {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for ForcePercentageRule.</p>
     *
     * @param ruleName a {@link java.lang.String} object
     * @param formula a {@link java.lang.String} object
     * @param locales a {@link java.util.List} object
     */
    protected ForcePercentageRule(String ruleName, String formula, List<String> locales) {
        super(ruleName, formula, locales);
    }
}
