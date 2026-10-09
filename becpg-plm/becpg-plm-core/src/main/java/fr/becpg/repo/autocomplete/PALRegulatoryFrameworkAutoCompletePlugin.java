package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.model.FileInfo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.product.formulation.allergen.PALDatabaseService;

/**
 * Suggests the PAL / VITAL regulatory frameworks available on the repository.
 *
 * <p>The frameworks are not a list of values to maintain: they are derived from the
 * reference dose grids published in the PAL database folder, so dropping a new grid
 * is enough to make it selectable on the products.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PALRegulatoryFrameworkAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	/** Constant <code>SOURCE_TYPE_PAL_FRAMEWORKS="palRegulatoryFrameworks"</code> */
	private static final String SOURCE_TYPE_PAL_FRAMEWORKS = "palRegulatoryFrameworks";

	private static final String CSS_CLASS = "file";

	@Autowired
	private PALDatabaseService palDatabaseService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PAL_FRAMEWORKS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		return new AutoCompletePage(extractFrameworkCodes(query), pageNum, pageSize, null);
	}

	/**
	 * Turns the published grids into framework codes, the grid file name without its
	 * extension being the code stored on the products.
	 *
	 * @param query the text typed by the user, may be {@code null}
	 * @return the matching framework codes
	 */
	private List<AutoCompleteEntry> extractFrameworkCodes(String query) {
		List<AutoCompleteEntry> suggestions = new ArrayList<>();

		for (FileInfo database : palDatabaseService.getPALDatabases()) {
			String frameworkCode = FilenameUtils.removeExtension(database.getName());

			if (matches(frameworkCode, query)) {
				suggestions.add(new AutoCompleteEntry(frameworkCode, frameworkCode, CSS_CLASS));
			}
		}

		return suggestions;
	}

	/**
	 * <p>matches.</p>
	 *
	 * @param frameworkCode the candidate framework code
	 * @param query the text typed by the user, may be {@code null}
	 * @return true when the code should be suggested
	 */
	private boolean matches(String frameworkCode, String query) {
		return (query == null) || query.isBlank() || frameworkCode.toLowerCase().contains(query.toLowerCase());
	}

}
