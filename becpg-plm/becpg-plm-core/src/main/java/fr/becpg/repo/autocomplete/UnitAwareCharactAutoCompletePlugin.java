package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;

/**
 * <p>UnitAwareCharactAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 * 
 * Autocomplete plugin that provide a custom extractor for nut and physico appending unit to charact name
 * 
 * Example:
 * <pre>
 * {@code
 * 		<control template="/org/alfresco/components/form/controls/autocomplete-association.ftl" >
 *			<control-param name="ds">becpg/autocomplete/physicoChem</control-param>
 *		</control>
 * }
 * </pre>
 *   
 *  Datasources available:
 * 
 *  becpg/autocomplete/nut
 *  becpg/autocomplete/physicoChem
 * 
 * 
 */
@Service("unitAwareCharactAutoCompletePlugin")
public class UnitAwareCharactAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_NUT = "nut";
	private static final String SOURCE_TYPE_PHYSICO_CHEM = "physicoChem";
	private static final String SOURCE_TYPE_MICROBIO = "microbio";
	private static final String SOURCE_TYPE_CHARACTS=  "characts";
	
	@Autowired
	private UnitAwareCharactAutoCompleteExtractor charactValueExtractor;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_NUT, SOURCE_TYPE_PHYSICO_CHEM,SOURCE_TYPE_MICROBIO, SOURCE_TYPE_CHARACTS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		
		if (SOURCE_TYPE_NUT.equals(sourceType)) {
			return suggestTargetAssoc(null, PLMModel.TYPE_NUT, query, pageNum, pageSize, null, props);
		} else if (SOURCE_TYPE_PHYSICO_CHEM.equals(sourceType)) {
			return suggestTargetAssoc(null, PLMModel.TYPE_PHYSICO_CHEM, query, pageNum, pageSize, null, props);
		} else if (SOURCE_TYPE_MICROBIO.equals(sourceType)) {
			return suggestTargetAssoc(null, PLMModel.TYPE_MICROBIO, query, pageNum, pageSize, null, props);
		} else {
			String classNames = (String) props.get(AutoCompleteService.PROP_CLASS_NAMES);
			String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;
			return suggestTargetAssoc(null, BeCPGModel.TYPE_CHARACT, query, pageNum, pageSize, arrClassNames, props);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected AutoCompleteExtractor<NodeRef> getTargetAssocValueExtractor() {
		return charactValueExtractor;
	}

}
