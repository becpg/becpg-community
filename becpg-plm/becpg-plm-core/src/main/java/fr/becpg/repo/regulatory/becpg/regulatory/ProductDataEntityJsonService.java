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
import org.alfresco.service.cmr.repository.StoreRef;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service responsible for deserializing regulatory JSON payloads into {@link ProductData} objects.
 *
 * <p>Each datalist entry is parsed into its corresponding domain object. Ingredients or
 * country/usage pairs present in the reference {@link ProductData} but absent from the JSON
 * are surfaced as {@link RequirementListDataItem} alerts on the filled product.
 */
@Service
public class ProductDataEntityJsonService {
    public static final String MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND = "message.regulatory.usage-to-country.missing";
    public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";

    private final Map<Class<?>, Function<JSONObject, Stream<?>>> listDeserializerRegistry;
    private NodeService nodeService;

    public ProductDataEntityJsonService(@Qualifier("nodeService") NodeService nodeService) {
        this.nodeService = nodeService;
        this.listDeserializerRegistry = Map.of(
                RequirementListDataItem.class, ProductDataEntityJsonService::parseReqCtrlList,
                IngRegulatoryListDataItem.class, ProductDataEntityJsonService::parseIngredientRegulations
        );
    }

    /**
     * Creates a new, {@link ProductData} precisely representing deserialized entity as it is in the JSON
     *
     * <p>Datalists currently handled: {@code bcpg:reqCtrlList} and {@code bcpg:ingRegulatoryList}
     */
    public ProductData newProductDataFromJson(JSONObject json) {
        ProductData deserialized = new ProductData();
        deserialized.setIngRegulatoryList(parseIngredientRegulations(json).collect(Collectors.toCollection(ArrayList::new)));
        deserialized.setReqCtrlList(parseReqCtrlList(json).collect(Collectors.toCollection(ArrayList::new)));
        return deserialized;
    }

    /**
     * Creates stream of datalist objects, precisely representing what is present in the json
     *
     * @param datalistElementClass Class of a list datalist element, for example {@link IngRegulatoryListDataItem}
     * @return empty stream if parser is not yet implemented
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<T> deserializeDatalist(Class<T> datalistElementClass, JSONObject json) {
        return (Stream<T>) listDeserializerRegistry.getOrDefault(datalistElementClass, ignored -> Stream.empty()).apply(json);
    }


    private static <T> Stream<T> parseDatalist(JSONObject json, String typeName, Function<JSONObject, T> itemParser) {
        JSONObject datalists = json.getJSONObject("datalists");

        if (datalists.has(typeName)) {
            JSONArray array = datalists.getJSONArray(typeName);
            return IntStream.range(0, array.length())
                    .mapToObj(i -> array.getJSONObject(i).optJSONObject("attributes"))
                    .filter(attributes -> attributes != null && !attributes.isEmpty())
                    .map(itemParser);
        }
        return Stream.empty();
    }

    private static Stream<RequirementListDataItem> parseReqCtrlList(JSONObject json) {
        return parseDatalist(json, qnameToString(PLMModel.TYPE_REQCTRLLIST), ProductDataEntityJsonService::parseReqCtrlItem);
    }

    private static RequirementListDataItem parseReqCtrlItem(JSONObject attrs) {
        RequirementListDataItem item = new RequirementListDataItem();

        readString(attrs, PLMModel.PROP_RCL_REQ_TYPE, v -> item.setReqType(RequirementType.fromString(v)));
        readString(attrs, PLMModel.PROP_RCL_REQ_DATA_TYPE, v -> item.setReqDataType(RequirementDataType.fromString(v)));
        readString(attrs, PLMModel.PROP_RCL_REQ_MESSAGE, v -> item.setReqMlMessage(new MLText(v)));
        readString(attrs, PLMModel.PROP_REGULATORY_CODE, item::setRegulatoryCode);
        readString(attrs, PLMModel.PROP_RCL_FORMULATION_CHAIN_ID, item::setFormulationChainId);
        readString(attrs, PLMModel.PROP_RCL_ERROR_LOG, item::setErrorLog);

        readDouble(attrs, PLMModel.PROP_RCL_REQ_MAX_QTY, item::setReqMaxQty);

        readNodeRefs(attrs, PLMModel.PROP_RCL_SOURCES_V2, item::setSources);

        String charactKey = qnameToString(PLMModel.ASSOC_RCL_CHARACT);
        if (attrs.has(charactKey)) {
            String id = attrs.getJSONObject(charactKey).getString("id");
            if (id != null && !id.isBlank())
                item.setCharact(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id));
        }
        return item;
    }

    /**
     * @param regulatoryElements    regulatory list contents, as defined in the product
     * @param parsedReqCtrlElements only ones, directly deserialized from JSON
     * @return a stream of {@link RequirementListDataItem} alerts for uncovered COUNTRY - USAGE pairs
     */
    public Stream<RequirementListDataItem> createAlertsForNotCoveredCountryToUsagePairs(Collection<RegulatoryListDataItem> regulatoryElements,
                                                                                        Collection<RequirementListDataItem> parsedReqCtrlElements) {
        // COUNTRY - USAGE that were handled
        Set<String> coveredPairCodes = parsedReqCtrlElements.stream()
                .map(RequirementListDataItem::getRegulatoryCode)
                .collect(Collectors.toSet());

        // distinct() ensures that each noderef is only looked-up once, all standard map-based solutions look-up and then dedup
        Map<NodeRef, String> codeByRef = regulatoryElements.stream().flatMap(item -> Stream.concat(
                item.getRegulatoryCountriesRef().stream(), item.getRegulatoryUsagesRef().stream()
        )).distinct().collect(Collectors.toMap(
                Function.identity(),
                nodeRef -> (String) nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_CODE)
        ));

        return regulatoryElements.stream().flatMap(item -> Lists.cartesianProduct(
                        item.getRegulatoryCountriesRef(), item.getRegulatoryUsagesRef()).stream()
                ).collect(Collectors.toMap(
                        pair -> codeByRef.get(pair.get(0)) + " - " + codeByRef.get(pair.get(1)),
                        ArrayList::new,
                        (existing, duplicate) -> existing
                )).entrySet()
                .stream()
                .mapMulti((entry, sink) -> {
                    String code = entry.getKey();
                    if (!coveredPairCodes.contains(code)) {
                        MLText i18NMessage = MLTextHelper.getI18NMessage(MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND);
                        List<NodeRef> sources = entry.getValue();
                        sink.accept(createToleratedReqCtrl(sources, i18NMessage, null, code));
                    }
                });
    }

    private static Stream<IngRegulatoryListDataItem> parseIngredientRegulations(JSONObject json) {
        return parseDatalist(json, qnameToString(PLMModel.TYPE_ING_REGULATORY_LIST), ProductDataEntityJsonService::parseIngRegulatoryItem);
    }

    private static IngRegulatoryListDataItem parseIngRegulatoryItem(JSONObject attrs) {
        IngRegulatoryListDataItem item = new IngRegulatoryListDataItem();

        String ingKey = qnameToString(PLMModel.ASSOC_IRL_ING);
        if (attrs.has(ingKey)) {
            String id = attrs.getJSONObject(ingKey).getString("id");
            if (id != null && !id.isBlank())
                item.setIng(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id));
        }

        readString(attrs, PLMModel.PROP_IRL_CITATION, v -> item.setCitation(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_RESTRICTION_LEVELS, v -> item.setRestrictionLevels(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_PRECAUTIONS, v -> item.setPrecautions(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_RESULT_INDICATOR, v -> item.setResultIndicator(new MLText(v)));
        readString(attrs, PLMModel.PROP_REGULATORY_COMMENT, v -> item.setComment(new MLText(v)));
        readString(attrs, PLMModel.PROP_IRL_USAGES, v -> item.setUsages(new MLText(v)));

        readNodeRefs(attrs, PLMModel.ASSOC_REGULATORY_COUNTRIES, item::setRegulatoryCountries);
        readNodeRefs(attrs, PLMModel.ASSOC_REGULATORY_USAGE_REF, item::setRegulatoryUsages);
        return item;
    }

    /**
     * @param ingredientElements           {@code ingList} contents, as defined in the product
     * @param ingredientRegulatoryElements deserialized from JSON
     * @return a stream of {@link RequirementListDataItem} alerts for each ingredient, for which {@link IngRegulatoryListDataItem} was not provided
     */
    public Stream<RequirementListDataItem> createAlertsForNotCoveredIngredients(Collection<IngListDataItem> ingredientElements,
                                                                                Collection<IngRegulatoryListDataItem> ingredientRegulatoryElements) {

        Set<NodeRef> parsedIngRegulatoryElements = ingredientRegulatoryElements.stream()
                .map(IngRegulatoryListDataItem::getIng)
                .collect(Collectors.toSet());

        return ingredientElements.stream().mapMulti((ing, sink) -> {
            NodeRef ingNodeRef = ing.getIng();
            if (!parsedIngRegulatoryElements.contains(ingNodeRef)) {
                ArrayList<NodeRef> sources = Lists.newArrayList(ingNodeRef);
                MLText i18NMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
                sink.accept(createToleratedReqCtrl(sources, i18NMessage, ingNodeRef, null));
            }
        });
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

    private static void readNodeRefs(JSONObject attrs, QName qname, Consumer<List<NodeRef>> consumer) {
        String countriesKey = qnameToString(qname);
        if (attrs.has(countriesKey)) {
            JSONArray countries = attrs.getJSONArray(countriesKey);
            consumer.accept(IntStream.range(0, countries.length())
                    .mapToObj(i -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, countries.getJSONObject(i).getString("id")))
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
    }
}