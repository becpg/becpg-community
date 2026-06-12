package fr.becpg.repo.regulatory.becpg.regulatory;

import com.google.common.collect.Lists;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.decernis.DecernisRegulatoryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for deserializing regulatory JSON payloads into {@link ProductData} objects.
 *
 * <p>Each datalist entry is parsed into its corresponding domain object. Ingredients or
 * country/usage pairs present in the reference {@link ProductData} but absent from the JSON
 * are surfaced as {@link RequirementListDataItem} alerts on the filled product.
 */
@Service
public class ProductDataJSONService {

    public static final String MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND = "message.regulatory.usage-to-country.missing";
    public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";

    private NodeService nodeService;

    public ProductDataJSONService(@Qualifier("nodeService") NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Creates a new, empty {@link ProductData} and populates it from the given JSON,
     * using {@code reference} to resolve node references.
     *
     * <p>Datalists currently handled: {@code bcpg:reqCtrlList} and {@code bcpg:ingRegulatoryList}
     *
     * <p>After parsing, any reference ingredient or country/usage pair that was NOT
     * covered by the JSON is appended to {@code reqCtrlList} as a warning item.
     *
     * @param reference the original product whose ingredient list and regulatory list
     *                  are used as a lookup table for node-ref resolution
     * @return a freshly constructed {@link ProductData} with all deserialized datalist entries
     */
    public ProductData newProductDataFromJson(ProductData reference, JSONObject json) {
        ProductData deserialized = new ProductData();

        fillProductDataFromJson(deserialized, reference, json);
        return deserialized;
    }

    /**
     * Fills an existing {@link ProductData} from the given JSON payload.
     *
     * <p>Datalists currently handled: {@code bcpg:reqCtrlList} and {@code bcpg:ingRegulatoryList}
     *
     * <p>After parsing, any reference ingredient or country/usage pair that was NOT
     * covered by the JSON is appended to {@code reqCtrlList} as a warning item.
     *
     * @param toFill    the target object to populate
     * @param reference the reference product for node-ref resolution
     */
    public void fillProductDataFromJson(ProductData toFill, ProductData reference, JSONObject json) {
        ensureReqCtrlList(toFill);

        Stream<RequirementListDataItem> uncoveredIngredients = fillIngredientRegulations(toFill, reference, json);
        Stream<RequirementListDataItem> uncoveredCountryUsagePairs = fillReqCtrlList(toFill, reference, json);

        uncoveredIngredients.forEach(e -> toFill.getReqCtrlList().add(e));
        uncoveredCountryUsagePairs.forEach(e -> toFill.getReqCtrlList().add(e));
    }

    /**
     * Parses the {@code bcpg:reqCtrlList} datalist
     *
     * @return a stream of {@link RequirementListDataItem} alerts for uncovered COUNTRY - USAGE pairs
     */
    private Stream<RequirementListDataItem> fillReqCtrlList(ProductData toFill, ProductData reference, JSONObject json) {

        Map<String, List<NodeRef>> uncoveredPairCodes = buildPairCodeMap(reference);

        JSONObject datalists = json.getJSONObject("datalists");
        String key = qnameToString(PLMModel.TYPE_REQCTRLLIST);

        if (datalists.has(key)) {
            JSONArray reqCtrlArray = datalists.getJSONArray(key);
            for (int i = 0; i < reqCtrlArray.length(); i++) {
                JSONObject attrs = reqCtrlArray.getJSONObject(i).optJSONObject("attributes");
                RequirementListDataItem item = parseReqCtrlItem(attrs, reference);
                if (item != null) {
                    toFill.getReqCtrlList().add(item);
                    uncoveredPairCodes.remove(item.getRegulatoryCode());
                }
            }
        }

        return uncoveredPairCodes.entrySet().stream().map(e -> createToleratedReqCtrl(
                e.getValue(), MLTextHelper.getI18NMessage(MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND), null, e.getKey()
        ));
    }

    /**
     * Builds a map from {@code "COUNTRY_CODE - USAGE_CODE"} strings to their corresponding node-ref lists
     *
     * @param reference the product whose {@link ProductData#regulatoryList} is the source
     * @return a mutable map of pair-code strings to their node-refs
     */
    private Map<String, List<NodeRef>> buildPairCodeMap(ProductData reference) {
        return reference.getRegulatoryList().stream().flatMap(item ->
                Lists.cartesianProduct(item.getRegulatoryCountriesRef(), item.getRegulatoryUsagesRef()).stream()
        ).collect(Collectors.toMap(
                pair -> getRegCode(pair.get(0)) + " - " + getRegCode(pair.get(1)),
                ArrayList::new,
                (existing, duplicate) -> existing
        ));
    }

    private RequirementListDataItem parseReqCtrlItem(JSONObject attrs, ProductData reference) {
        if (attrs == null)
            return null;

        RequirementListDataItem item = new RequirementListDataItem();

        readString(attrs, PLMModel.PROP_RCL_REQ_TYPE,               v -> item.setReqType(RequirementType.fromString(v)));
        readString(attrs, PLMModel.PROP_RCL_REQ_DATA_TYPE,          v -> item.setReqDataType(RequirementDataType.fromString(v)));
        readString(attrs, PLMModel.PROP_RCL_REQ_MESSAGE,            v -> item.setReqMlMessage(new MLText(v)));
        readDouble(attrs, PLMModel.PROP_RCL_REQ_MAX_QTY,            item::setReqMaxQty);
        readString(attrs, PLMModel.PROP_REGULATORY_CODE,            item::setRegulatoryCode);
        readString(attrs, PLMModel.PROP_RCL_FORMULATION_CHAIN_ID,   item::setFormulationChainId);
        readString(attrs, PLMModel.PROP_RCL_ERROR_LOG,              item::setErrorLog);

        String charactKey = qnameToString(PLMModel.ASSOC_RCL_CHARACT);
        if (attrs.has(charactKey)) {
            String id = attrs.getJSONObject(charactKey).getString("id");
            getIngListElemNodeRef(id, reference.getIngList()).ifPresent(item::setCharact);
        }

        String sourcesKey = qnameToString(PLMModel.PROP_RCL_SOURCES_V2);
        if (attrs.has(sourcesKey)) {
            JSONArray sources = attrs.getJSONArray(sourcesKey);
            for (int i = 0; i < sources.length(); i++) {
                String id = sources.getJSONObject(i).getString("id");
                getIngListElemNodeRef(id, reference.getIngList()).ifPresent(item::addSource);
            }
        }

        return item;
    }

    /**
     * Parses the {@code bcpg:ingRegulatoryList} datalist
     *
     * @return a stream of {@link RequirementListDataItem} alerts for ingredients present in reference:ingList but missing in json
     */
    private Stream<RequirementListDataItem> fillIngredientRegulations(ProductData toFill, ProductData reference, JSONObject json) {

        Set<IngListDataItem> unlisted = new HashSet<>(reference.getIngList());

        JSONObject datalists = json.getJSONObject("datalists");
        String key = qnameToString(PLMModel.TYPE_ING_REGULATORY_LIST);

        if (datalists.has(key)) {
            if (toFill.getIngRegulatoryList() == null)
                toFill.setIngRegulatoryList(new LinkedList<>());
            JSONArray ingRegArray = datalists.getJSONArray(key);
            for (int i = 0; i < ingRegArray.length(); i++) {
                JSONObject attrs = ingRegArray.getJSONObject(i).optJSONObject("attributes");
                IngRegulatoryListDataItem item = parseIngRegulatoryItem(attrs, reference);
                if (item != null) {
                    toFill.getIngRegulatoryList().add(item);
                    unlisted.removeIf(ing -> ing.getIng().getId().equals(item.getIng().getId()));
                }
            }
        }

        return unlisted.stream().map(ing -> createToleratedReqCtrl(
                Lists.newArrayList(ing.getIng()), MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING), ing.getIng(), null
        ));
    }

    private IngRegulatoryListDataItem parseIngRegulatoryItem(JSONObject attrs, ProductData reference) {
        if (attrs == null)
            return null;

        IngRegulatoryListDataItem item = new IngRegulatoryListDataItem();

        String ingKey = qnameToString(PLMModel.ASSOC_IRL_ING);
        if (attrs.has(ingKey)) {
            String id = attrs.getJSONObject(ingKey).getString("id");
            getIngNodeRef(id, reference.getIngList()).ifPresent(item::setIng);
        }

        readString(attrs, PLMModel.PROP_IRL_CITATION,           v -> item.setCitation(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_RESTRICTION_LEVELS, v -> item.setRestrictionLevels(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_PRECAUTIONS,        v -> item.setPrecautions(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_RESULT_INDICATOR,   v -> item.setResultIndicator(new MLText(v)));
        readString(attrs, PLMModel.PROP_REGULATORY_COMMENT,     v -> item.setComment(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_USAGES,             v -> item.setUsages(new MLText(v)));

        String countriesKey = qnameToString(PLMModel.ASSOC_REGULATORY_COUNTRIES);
        if (attrs.has(countriesKey)) {
            List<NodeRef> countryRefs = resolveNodeRefs(attrs.getJSONArray(countriesKey), id ->
                    getRegulatoryCountryNodeRef(id, reference.getRegulatoryList()));
            item.setRegulatoryCountries(countryRefs);
        }

        String usagesKey = qnameToString(PLMModel.ASSOC_REGULATORY_USAGE_REF);
        if (attrs.has(usagesKey)) {
            List<NodeRef> usageRefs = resolveNodeRefs(attrs.getJSONArray(usagesKey), id ->
                    getRegulatoryUsageNodeRef(id, reference.getRegulatoryList()));
            item.setRegulatoryUsages(usageRefs);
        }

        return item;
    }

    private static RequirementListDataItem createToleratedReqCtrl(List<NodeRef> sources, MLText message, NodeRef charact, String code) {

        RequirementListDataItem item = new RequirementListDataItem();
        item.setReqType(RequirementType.Tolerated);
        item.setReqDataType(RequirementDataType.Specification);
        item.setReqMlMessage(message);
        item.setSources(sources);
        item.setFormulationChainId(DecernisRegulatoryService.REGULATORY_KEY);

        if (code != null && !code.isBlank()) {
            item.setRegulatoryCode(code);
        }
        if (charact != null) {
            item.setCharact(charact);
        }
        return item;
    }

    private String getRegCode(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_CODE);
    }

    private static Optional<NodeRef> getIngNodeRef(String id, List<IngListDataItem> ingList) {
        return ingList.stream()
                .filter(elem -> elem.getIng().getId().equals(id))
                .findFirst()
                .map(IngListDataItem::getIng);
    }

    private static Optional<NodeRef> getIngListElemNodeRef(String id, List<IngListDataItem> ingList) {
        return ingList.stream()
                .filter(elem -> elem.getNodeRef().getId().equals(id))
                .findFirst()
                .map(IngListDataItem::getNodeRef);
    }

    private static Optional<NodeRef> getRegulatoryCountryNodeRef(String id, List<RegulatoryListDataItem> regulatoryList) {
        return regulatoryList.stream()
                .flatMap(elem -> elem.getRegulatoryCountriesRef().stream())
                .filter(country -> country.getId().equals(id))
                .findFirst();
    }


    private static Optional<NodeRef> getRegulatoryUsageNodeRef(String id, List<RegulatoryListDataItem> regulatoryList) {
        return regulatoryList.stream()
                .flatMap(elem -> elem.getRegulatoryUsagesRef().stream())
                .filter(usage -> usage.getId().equals(id))
                .findFirst();
    }

    private static String qnameToString(QName qname) {
        return "bcpg:" + qname.getLocalName();
    }

    private static void readString(JSONObject attrs, QName qname, Consumer<String> consumer) {
        String key = qnameToString(qname);
        if (attrs.has(key))
            consumer.accept(attrs.getString(key));
    }

    private static void readDouble(JSONObject attrs, QName qname, DoubleConsumer consumer) {
        String key = qnameToString(qname);
        if (attrs.has(key))
            consumer.accept(attrs.getDouble(key));
    }

    private static List<NodeRef> resolveNodeRefs(JSONArray jsonArray, Function<String, Optional<NodeRef>> resolver) {
        List<NodeRef> refs = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = jsonArray.getJSONObject(i).getString("id");
            resolver.apply(id).ifPresent(refs::add);
        }
        return refs;
    }

    private static void ensureReqCtrlList(ProductData toFill) {
        if (toFill.getReqCtrlList() == null)
            toFill.setReqCtrlList(new LinkedList<>());
    }
}