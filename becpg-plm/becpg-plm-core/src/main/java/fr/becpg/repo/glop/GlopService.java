package fr.becpg.repo.glop;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import fr.becpg.repo.glop.model.GlopContext;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.product.data.ProductData;

/**
 * Recipe optimization by means of a Glop server.
 *
 * <h1>Requirements</h1>
 * <ul>
 * <li>In {@code becpg-plm/becpg-plm-core/src/main/resources/alfresco/module/becpg-plm-core/becpg-config-plm.properties},
 * the data {@code beCPG.glop.serverUrl} must be set to the IP address of this PC on the local network
 * and use the port and HTTP scope of the Glop server (<i>e.g.</i> {@code http://192.168.1.41:5000/compute}).</li>
 * <li>The Python 3 libraries Flask and OR-Tools must be installed ({@code python3 -m pip install ortools Flask}).</li>
 * <li>The Glop server must be running ({@code python3 linrest.py} from the root directory).</li>
 * </ul>
 *
 * @author pierrecolin
 * @version 1.0
 * @see fr.becpg.repo.glop.GlopSpelFunctions
 */
public interface GlopService {
	
	/**
	 * Finds the optimal recipe for a product given a target to optimize and a list of constraints.
	 *
	 * @param productData the data of the product to optimize
	 * @param characts the list of constraints the optimization is subject to
	 * @param target the target function specification
	 * @return the solution computed by the Glop server
	 * @throws fr.becpg.repo.glop.GlopException if the linear program is unfeasible
	 * @throws org.springframework.web.client.RestClientException if an error was met while communicating with the Glop server
	 * @throws java.net.URISyntaxException if the Glop server URL specified is syntactically incorrect
	 * @throws org.json.JSONException if an error was met building one of the JSON objects involved
	 */
	public GlopData optimize(ProductData entity, GlopContext buildGlopContext) throws GlopException, RestClientException, URISyntaxException, JSONException;

	/**
	 * Sends a request to the Glop server. Mostly for testing and may be deprecated in the future.
	 *
	 * @param request the request to be sent to the server (see the server source for syntax)
	 * @return the solution computed by the server
	 * @throws org.springframework.web.client.RestClientException if an error was met while communicating with the Glop server
	 * @throws java.net.URISyntaxException if the server URL specified is syntactically incorrect
	 * @throws org.json.JSONException if an error was met while building one of the JSON objects involved
	 * @throws fr.becpg.repo.glop.GlopException if the linear program is unfeasible
	 */
	public String sendRequest(JSONObject request) throws URISyntaxException, JSONException, RestClientException, GlopException;

	
}
