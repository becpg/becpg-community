package fr.becpg.repo.regulatory.becpg.regulatory;

import com.google.common.collect.Lists;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.AbstractRegulatoryService;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final Log log = LogFactory.getLog(ProductDataEntityJsonService.class);

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
        if (json != null) {
            deserialized.setIngRegulatoryList(parseIngredientRegulations(json).collect(Collectors.toCollection(ArrayList::new)));
            deserialized.setReqCtrlList(parseReqCtrlList(json).collect(Collectors.toCollection(ArrayList::new)));
        }
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
        return json == null ? Stream.empty() :
                (Stream<T>) listDeserializerRegistry.getOrDefault(datalistElementClass, ignored -> Stream.empty()).apply(json);
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
        readString(attrs, PLMModel.PROP_REGULATORY_CODE, item::setRegulatoryCode);
        readString(attrs, PLMModel.PROP_RCL_FORMULATION_CHAIN_ID, item::setFormulationChainId);
        readString(attrs, PLMModel.PROP_RCL_ERROR_LOG, item::setErrorLog);

        readMlString(attrs, PLMModel.PROP_RCL_REQ_MESSAGE, item::setReqMlMessage);

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
     * Extracts ingredient id:regulatoryCode pairs to update ingredient characts
     */
    public Map<String, String> extractIngIdToRegulatoryCodes(JSONObject ingRegulatoryListJson) {
        JSONObject datalists = ingRegulatoryListJson.getJSONObject("datalists");

        String listTypeName = qnameToString(PLMModel.TYPE_ING_REGULATORY_LIST);
        String ingAssocTypeName = qnameToString(PLMModel.ASSOC_IRL_ING);
        String regCodeTypeName = qnameToString(PLMModel.PROP_REGULATORY_CODE);

        if (datalists.has(listTypeName)) {
            JSONArray array = datalists.getJSONArray(listTypeName);
            return IntStream.range(0, array.length())
                    .mapToObj(i -> array.getJSONObject(i).optJSONObject("attributes"))
                    .filter(attributes -> attributes != null && attributes.has(ingAssocTypeName))
                    .<Pair<String, String>>mapMulti((jsonAttributes, sink) -> {
                        JSONObject ingAssoc = jsonAttributes.optJSONObject(ingAssocTypeName);
                        if (ingAssoc != null) {
                            String id = ingAssoc.optString("id");
                            JSONObject assocAttrs = ingAssoc.optJSONObject("attributes");
                            String regCode = assocAttrs != null ? assocAttrs.optString(regCodeTypeName) : "";
                            if (StringUtils.hasText(id) && StringUtils.hasText(regCode)) {
                                sink.accept(Pair.of(id, regCode));
                            }
                        }
                    }).collect(Collectors.toMap(
                            Pair::getKey,
                            Pair::getValue,
                            (v1, v2) -> {
                                if (StringUtils.hasText(v1) && StringUtils.hasText(v2)) {
                                    if (v1.equals(v2))
                                        return v1;
                                    log.warn("becpg-regulatory returned different regulatory code sets for the same ingredient: " + v1 + " and " + v2);
                                }
                                return StringUtils.hasText(v1) ? v1 : StringUtils.hasText(v2) ? v2 : "";
                            }
                    ));
        }
        return Map.of();
    }

    /**
     * Creates a list of tolerated reqCtrl elements for
     * each country that was not listed in IngRegulatoryList x
     * each regulatory usage ever specified for this country.
     * As product regulatory usage is only one of many criteria used to pick relevant regulatory requirements -
     * raising by-pair alerts makes no sense.
     *
     * @param regulatoryElements    regulatory list contents, as defined in the product
     * @param ingRegulatoryElements only ones, directly deserialized from JSON
     * @return a stream of {@link RequirementListDataItem} alerts for each not-covered country x usages this country ever linked with
     */
    public Stream<RequirementListDataItem> createAlertsForNotCoveredCountryToUsagePairs(Collection<RegulatoryListDataItem> regulatoryElements,
                                                                                        Collection<IngRegulatoryListDataItem> ingRegulatoryElements) {
        if (regulatoryElements == null || ingRegulatoryElements == null)
            return Stream.empty();

        Map<NodeRef, Set<NodeRef>> countryToUsages = new HashMap<>();
        // squash usages for each country from different regulatoryElements
        for (RegulatoryListDataItem item : regulatoryElements) {
            for (NodeRef country : item.getRegulatoryCountriesRef()) {
                countryToUsages.computeIfAbsent(country, ignored -> new HashSet<>()).addAll(item.getRegulatoryUsagesRef());
            }
        }
        // remove pairs if country was handled in any ingRegulatory element
        for (IngRegulatoryListDataItem item : ingRegulatoryElements) {
            for (NodeRef country : item.getRegulatoryCountries()) {
                countryToUsages.remove(country);
            }
        }
        // everything handled - return
        if (countryToUsages.isEmpty())
            return Stream.empty();

        Map<NodeRef, String> codeByRef = fillNodeRefDictionary(regulatoryElements);
        MLText i18NMessage = MLTextHelper.getI18NMessage(MESSAGE_COUNTRY_USAGE_PAIR_NOT_FOUND);

        return countryToUsages.entrySet().stream().flatMap(entry -> {
            NodeRef country = entry.getKey();
            String countryRegCode = codeByRef.get(country);

            return entry.getValue().stream().map(usage -> {
                String code = countryRegCode + " - " + codeByRef.get(usage);
                return createToleratedReqCtrl(List.of(country, usage), i18NMessage, null, code);
            });
        });
    }

    /**
     * Creates a list of tolerated reqCtrl elements for each ingredient that was not listed in IngRegulatoryList
     *
     * @param ingredientElements           {@code ingList} contents, as defined in the product
     * @param ingredientRegulatoryElements deserialized from JSON
     * @return a stream of {@link RequirementListDataItem} alerts for each ingredient, for which {@link IngRegulatoryListDataItem} was not provided
     */
    public Stream<RequirementListDataItem> createAlertsForNotCoveredIngredients(Collection<IngListDataItem> ingredientElements,
                                                                                Collection<IngRegulatoryListDataItem> ingredientRegulatoryElements) {

        if (ingredientElements == null || ingredientRegulatoryElements == null || ingredientElements.isEmpty() || ingredientRegulatoryElements.isEmpty())
            return Stream.empty();

        Set<NodeRef> parsedIngRegulatoryElements = ingredientRegulatoryElements.stream()
                .map(IngRegulatoryListDataItem::getIng)
                .collect(Collectors.toSet());

        return ingredientElements.stream().mapMulti((ing, sink) -> {
            NodeRef ingNodeRef = ing.getIng();
            if (ingNodeRef != null && !parsedIngRegulatoryElements.contains(ingNodeRef)) {
                ArrayList<NodeRef> sources = Lists.newArrayList(ingNodeRef);
                MLText i18NMessage = MLTextHelper.getI18NMessage(MESSAGE_NOTLISTED_ING);
                sink.accept(createToleratedReqCtrl(sources, i18NMessage, ingNodeRef, null));
            }
        });
    }

    public Map<NodeRef, String> fillNodeRefDictionary(Collection<RegulatoryListDataItem> regulatoryElements) {
        if (regulatoryElements == null)
            return Collections.emptyMap();
        // distinct() ensures that each noderef is only looked-up once, all standard map-based solutions look-up and then dedup.
        // A HashMap (rather than Collectors.toMap) is used on purpose: the regulatory code may be null and toMap rejects null values.
        Map<NodeRef, String> codeByRef = new HashMap<>();
        regulatoryElements.stream().flatMap(item -> Stream.concat(
                item.getRegulatoryCountriesRef().stream(), item.getRegulatoryUsagesRef().stream()
        )).distinct().forEach(nodeRef ->
                codeByRef.put(nodeRef, (String) nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_CODE))
        );
        return codeByRef;
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

        readMlString(attrs, PLMModel.PROP_IRL_CITATION, item::setCitation);
        readMlString(attrs, PLMModel.PROP_IRL_RESTRICTION_LEVELS, item::setRestrictionLevels);
        readMlString(attrs, PLMModel.PROP_IRL_PRECAUTIONS, item::setPrecautions);
        readMlString(attrs, PLMModel.PROP_IRL_RESULT_INDICATOR, item::setResultIndicator);
        readMlString(attrs, PLMModel.PROP_REGULATORY_COMMENT, item::setComment);
        readMlString(attrs, PLMModel.PROP_IRL_USAGES, item::setUsages);

        readNodeRefs(attrs, PLMModel.ASSOC_REGULATORY_COUNTRIES, item::setRegulatoryCountries);
        readNodeRefs(attrs, PLMModel.ASSOC_REGULATORY_USAGE_REF, item::setRegulatoryUsages);
        return item;
    }

    private static RequirementListDataItem createToleratedReqCtrl(List<NodeRef> sources, MLText message, NodeRef charact, String code) {

        RequirementListDataItem item = new RequirementListDataItem();
        item.setReqType(RequirementType.Tolerated);
        item.setReqDataType(RequirementDataType.Specification);
        item.setReqMlMessage(message);
        item.setSources(sources);
        item.setFormulationChainId(AbstractRegulatoryService.REGULATORY_KEY);

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

    private static void readMlString(JSONObject attrs, QName qname, Consumer<MLText> consumer) {
        String baseKey = qnameToString(qname);
        String localePrefix = baseKey + "_";
        MLText value = null;
        for (String key : attrs.keySet()) {
            Locale locale;
            if (key.equals(baseKey)) {
                locale = MLText.getDefaultLocale();
            } else if (key.startsWith(localePrefix)) {
                String localeString = key.substring(localePrefix.length());
                locale = MLTextHelper.parseLocale(localeString);
            } else {
                continue;
            }
            if (locale != null) {
                if (value == null) {
                    value = new MLText(locale, attrs.getString(key));
                } else {
                    value.addValue(locale, attrs.getString(key));
                }
            }
        }
        if (value != null) {
            addRegionalLocales(value);
            consumer.accept(value);
        }
    }

    private static void addRegionalLocales(MLText value) {
        for (Map.Entry<Locale, String> entry : List.copyOf(value.entrySet())) {
            Locale messageRawLocale = entry.getKey();
            if (!messageRawLocale.getCountry().isEmpty())
                continue;

            for (Locale standard : MLTextHelper.getSupportedLocales()) {
                if (messageRawLocale.getLanguage().equals(standard.getLanguage())
                        && !standard.getCountry().isEmpty()
                        && !value.containsKey(standard)) {

                    value.addValue(standard, entry.getValue());
                }
            }
        }
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