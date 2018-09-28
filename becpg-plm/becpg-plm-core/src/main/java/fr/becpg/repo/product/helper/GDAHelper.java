package fr.becpg.repo.product.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;

@Service("gdaHelper")
public class GDAHelper {
	protected static final Log logger = LogFactory.getLog(GDAHelper.class);

	private static final Locale DEFAULT_LOCALE = Locale.FRENCH;

	private static JSONArray GDAProfiles = new JSONArray();

	public void readGDAProfiles(RepoService repoService,
			NodeService nodeService, ContentService contentService) {

		JSONArray gdaProfiles = null;

		try {
			NodeRef gdaProfilesFolderNodeRef = repoService
					.getFolderByPath(RepoConsts.PATH_SYSTEM
							+ RepoConsts.PATH_SEPARATOR
							+ PlmRepoConsts.PATH_GDA_PROFILES);
			NodeRef gdaFileNodeRef = nodeService.getChildByName(
					gdaProfilesFolderNodeRef, ContentModel.ASSOC_CONTAINS,
					"gdaProfiles.json");

			ContentReader reader = contentService.getReader(gdaFileNodeRef,
					ContentModel.PROP_CONTENT);

			BufferedReader streamReader = new BufferedReader(
					new InputStreamReader(reader.getContentInputStream(),
							"UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null)
				responseStrBuilder.append(inputStr);

			JSONObject obj = new JSONObject(responseStrBuilder.toString());

			JSONObject jsonObject = (JSONObject) obj;

			gdaProfiles = (JSONArray) jsonObject.get("gdaProfiles");

		} catch (Exception e) {
			e.printStackTrace();
		}
		GDAProfiles = gdaProfiles;

	}

	public static String computeGDA(String nutCode, Double nutValue) {
		JSONArray ret = new JSONArray();
		JSONArray gdaProfiles = GDAProfiles;

		for (int i = 0; i < gdaProfiles.length(); i++) {
			JSONObject gdaProfile;
			JSONObject obj = new JSONObject();

			try {
				gdaProfile = gdaProfiles.getJSONObject(i);

				obj.put("nutCode", nutCode);
				obj.put("id", gdaProfile.get("id"));
				obj.put("locale", gdaProfile.get("locale"));
				obj.put("default", gdaProfile.get("default"));

				// Calcul gdaPerc
				Double gdaValue = gdaProfile.getJSONObject("values").getDouble(
						nutCode);
				Double gdaPerc;
				if (gdaValue != null) {
					gdaPerc = (100 * nutValue / gdaValue);
					obj.put("value", gdaPerc);
					ret.put(obj);
				}
			} catch (JSONException e) {
				logger.error("coudn't parse JSON");
			}
		}
		return ret.toString();
	}

	public static Double getGdaPerc(String computedGdaPerc) {
		try {
			JSONArray gdaProfiles = new JSONArray(computedGdaPerc);

			for (int i = 0; i < gdaProfiles.length(); i++) {
				JSONObject obj = gdaProfiles.getJSONObject(i);
				if (((obj.get("default") != null))
						&& (obj.get("default").equals("true"))
						&& (obj.get("locale").equals(DEFAULT_LOCALE))
						&& (obj.getDouble("value") != 0d)) {
					return obj.getDouble("value");
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Double getGdaPercByLocale(String computedGdaPerc,
			Locale locale) {

		try {
			JSONArray gdaProfiles = new JSONArray(computedGdaPerc);

			for (int i = 0; i < gdaProfiles.length(); i++) {
				JSONObject obj = gdaProfiles.getJSONObject(i);
				
				if ((obj.get("default") != null)
						&& (obj.getString("default").equals("true"))
						&& (locale.equals(MLTextHelper.parseLocale(obj.getString("locale"))
								))
						&& (obj.getDouble("value") != 0d)) {
					return obj.getDouble("value");
				}
			}
			return getGdaPerc(computedGdaPerc);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Double getGdaPercByProfile(String computedGdaPerc,
			String profileId) {
		try {
			JSONArray gdaProfiles = new JSONArray(computedGdaPerc);
			for (int i = 0; i < gdaProfiles.length(); i++) {
				JSONObject obj = gdaProfiles.getJSONObject(i);
				if ((obj.get("id") != null)
						&& (obj.get("id").equals(profileId) && (obj
								.getDouble("value") != 0d)
						&& (obj.get("value") != null))) {
					return obj.getDouble("value");
				}
			}
			return getGdaPerc(computedGdaPerc);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


}