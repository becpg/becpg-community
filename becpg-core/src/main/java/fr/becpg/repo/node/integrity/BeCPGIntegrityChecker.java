package fr.becpg.repo.node.integrity;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class BeCPGIntegrityChecker extends IntegrityChecker {
	
	private static final Log logger = LogFactory.getLog(BeCPGIntegrityChecker.class);
	
	private static final String MSG_ENTITY_REMOVED = "integrity-checker.entity-removed";
	private static final String MSG_CONSTRAINT_VALUE_REMOVED = "integrity-checker.constraint-value-removed";

	/**
     * Runs several types of checks, querying specifically for events that
     * will necessitate each type of test.
     * <p>
     * The interface contracts also requires that all events for the transaction
     * get cleaned up.
     */
	@Override
    public void checkIntegrity() throws IntegrityException
    {
		try {
			super.checkIntegrity();
		} catch (IntegrityException e) {
			
			List<IntegrityRecord> records = e.getRecords();
			
			List<String> reworkedMessages = new ArrayList<>();

			final String targetClassRegex = "class=ClassDef\\[name=(\\{.*\\}\\w+)\\]";
			final String targetNodeRefRegex = "workspace:\\/\\/SpacesStore\\/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
			
			final String propertyRegex = "Property: (\\{[a-zA-Z0-9\\.\\/\\:]+\\}[a-zA-Z0-9]+)";
			final String constraintValueRegex = "Constraint: \\d+ The value is not an allowed value: (.+)";
			//not tested
			final String associationDefRegex = "Association: Association\\[ class=ClassDef\\[name=(\\{[a-zA-Z0-9\\.\\/\\:]+\\}[a-zA-Z0-9]+), name=(\\{[a-zA-Z0-9\\.\\/\\:]+\\}[a-zA-Z0-9]+), target class=(\\{[a-zA-Z0-9\\.\\/\\:]+\\}[a-zA-Z0-9]+)";

			final String actualTargetMultiplicityRegex = "Actual target Multiplicity: ([0-9\\.]+)";
			final String requiredTargetMultiplicityRegex = "Required target Multiplicity: ([0-9\\.]+)";
			
			
			final Pattern targetClassPattern = Pattern.compile(targetClassRegex, Pattern.MULTILINE);
			final Pattern targetNodeRefPattern = Pattern.compile(targetNodeRefRegex, Pattern.MULTILINE);
			final Pattern propertyPattern = Pattern.compile(propertyRegex, Pattern.MULTILINE);
			final Pattern constraintValuePattern = Pattern.compile(constraintValueRegex, Pattern.MULTILINE);
			final Pattern associationDefPattern = Pattern.compile(associationDefRegex, Pattern.MULTILINE);
			final Pattern actualTargetMultiplicityPattern = Pattern.compile(actualTargetMultiplicityRegex, Pattern.MULTILINE);
			final Pattern requiredTargetMultiplicityPattern = Pattern.compile(requiredTargetMultiplicityRegex, Pattern.MULTILINE);
			
			/*
			 * Error templates
			 * 
			 * Invalid property value: 
   Node: workspace://SpacesStore/74d8e8af-df16-4a30-8666-59211ff4a2cc
   Name: 13d87abc-36a8-4fbd-9152-ecb85bb05e66
   Type: {http://www.bcpg.fr/model/becpg/1.0}nutList
   Property: {http://www.bcpg.fr/model/becpg/1.0}nutListGroup
   Constraint: 08030010 The value is not an allowed value: Autres nutriments

			 * 
			 * The association target multiplicity has been violated: 
   Source Node: workspace://SpacesStore/278cf1e9-aaff-40c4-a2ae-cbb14f5f7a99
   Association: Association[ class=ClassDef[name={http://www.bcpg.fr/model/becpg/1.0}compoList], name={http://www.bcpg.fr/model/becpg/1.0}compoListProduct, target class={http://www.bcpg.fr/model/becpg/1.0}product, source role=null, target role=null]
   Required target Multiplicity: 1..1
   Actual target Multiplicity: 0

			 * 
			 * 
			 */
			
			for(IntegrityRecord record : records) {
				
				String recordMessage = record.getMessage();
				String targetClassString = null;
				String targetNodeRef = null;
				String constraintValue = null;
				String assocName = null;
				String actualTargetMultiplicity = null;
				String requiredTargetMultiplicity = null;
				String property = null;
				
				Matcher targetClassMatcher = targetClassPattern.matcher(recordMessage);
				Matcher targetNodeRefMatcher = targetNodeRefPattern.matcher(recordMessage);
				
				Matcher propertyMatcher = propertyPattern.matcher(recordMessage);
				Matcher constraintValueMatcher = constraintValuePattern.matcher(recordMessage);
				Matcher associationDefMatcher = associationDefPattern.matcher(recordMessage);
				Matcher actualTargetMultiplicityMatcher = actualTargetMultiplicityPattern.matcher(recordMessage);
				Matcher requiredTargetMultiplicityMatcher = requiredTargetMultiplicityPattern.matcher(recordMessage);
				
				if(targetClassMatcher.find()){
					logger.debug("Match for target class: "+targetClassMatcher.groupCount() + " groups");
					targetClassString = targetClassMatcher.group(1);
				}
				
				if(targetNodeRefMatcher.find()){
					logger.debug("Match for nodeRef: "+targetNodeRefMatcher.groupCount() + " groups");
					targetNodeRef = targetNodeRefMatcher.group(0);
				}

				if(propertyMatcher.find()){
					logger.debug("Match for property: "+propertyMatcher.groupCount() + " groups");
					property = propertyMatcher.group(0);
				}
				
				if(constraintValueMatcher.find()){
					logger.debug("Match for constraint: "+constraintValueMatcher.groupCount() + " groups");
					constraintValue = constraintValueMatcher.group(0);
				}
				
				if(associationDefMatcher.find()){
					logger.debug("Match for nodeRef class: "+associationDefMatcher.groupCount() + " groups");
					assocName = associationDefMatcher.group(1);
				}
				
				if(actualTargetMultiplicityMatcher.find()){
					logger.debug("Match for nodeRef class: "+actualTargetMultiplicityMatcher.groupCount() + " groups");
					actualTargetMultiplicity = actualTargetMultiplicityMatcher.group(0);
				}
				
				if(requiredTargetMultiplicityMatcher.find()){
					logger.debug("Match for nodeRef class: "+requiredTargetMultiplicityMatcher.groupCount() + " groups");
					requiredTargetMultiplicity = requiredTargetMultiplicityMatcher.group(0);
				}
				
				logger.debug("Intercepted record: "+recordMessage);
				logger.debug("Target class: "+ targetClassString);
				logger.debug("Target NodeRef: " + targetNodeRef);
				logger.debug("property: " + property);
				logger.debug("constraintValue: " + constraintValue);
				logger.debug("assocName: " + assocName);
				logger.debug("actualTargetMultiplicity: " + actualTargetMultiplicity);
				logger.debug("requiredTargetMultiplicity: " + requiredTargetMultiplicity);
				
				//TODO
				//
				// Cases :
				// listValue removed -> Property matcher and constraint matcher have sth
				// product (or entity) removed -> association matcher: second name not empty, actual target multiplicity = 0 and required target multiplicity = 1..1
				// note that "product removed" should be a PLM message and not a core one..
				//
				// add messages for reworked messages list
				// problem : all nodeRefs (node, or sourceNode) are PLM types. Move this to PLM project ?
				
				String currentMessage = null;
				String entityName = null;
				if(property != null && !property.isEmpty() && constraintValue != null && !constraintValue.isEmpty()){
					
					//get product name for msg					
					currentMessage = I18NUtil.getMessage(MSG_CONSTRAINT_VALUE_REMOVED, entityName);
					
				} else if(assocName != null && !assocName.isEmpty() && actualTargetMultiplicity != null && "0".equals(actualTargetMultiplicity) && requiredTargetMultiplicity != null && "1..1".equals(requiredTargetMultiplicity)){
					
					//get product name for msg
					currentMessage = I18NUtil.getMessage(MSG_ENTITY_REMOVED, entityName);
					
				} else {
					currentMessage = recordMessage;
				}
				
				reworkedMessages.add(currentMessage);
				
			}
			
			List<IntegrityRecord> newRecords = new ArrayList<>();
			for(String reworkedMessage : reworkedMessages){
				newRecords.add(new IntegrityRecord(reworkedMessage));
			}
			
			throw new IntegrityException(newRecords);
		}
    }
}
