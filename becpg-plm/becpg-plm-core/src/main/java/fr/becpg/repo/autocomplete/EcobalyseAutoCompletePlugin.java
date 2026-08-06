package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredientService;

/**
 * Suggests the ingredients of the Ecobalyse reference, so that mapping a raw material is a
 * pick rather than a copied identifier.
 *
 * @author matthieu
 */
@Service
public class EcobalyseAutoCompletePlugin implements AutoCompletePlugin {

	/** Constant <code>SOURCE_TYPE_ECOBALYSE="ecobalyse"</code> */
	private static final String SOURCE_TYPE_ECOBALYSE = "ecobalyse";

	private final EcobalyseIngredientService ecobalyseIngredientService;

	/**
	 * <p>Constructor for EcobalyseAutoCompletePlugin.</p>
	 *
	 * @param ecobalyseIngredientService a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredientService} object
	 */
	@Autowired
	public EcobalyseAutoCompletePlugin(EcobalyseIngredientService ecobalyseIngredientService) {
		this.ecobalyseIngredientService = ecobalyseIngredientService;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_ECOBALYSE };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		return ecobalyseIngredientService.suggest(query, pageNum, pageSize);
	}

}
