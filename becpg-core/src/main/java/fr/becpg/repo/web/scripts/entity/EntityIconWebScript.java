package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityIconService;

/**
 * <p>EntityIconWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityIconWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(EntityIconWebScript.class);

	private EntityIconService entityIconService;

	/**
	 * <p>Setter for the field <code>entityIconService</code>.</p>
	 *
	 * @param entityIconService a {@link fr.becpg.repo.entity.EntityIconService} object
	 */
	public void setEntityIconService(EntityIconService entityIconService) {
		this.entityIconService = entityIconService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		try (OutputStream out = resp.getOutputStream()) {
			resp.setContentType("text/css;charset=UTF-8");
			resp.setContentEncoding("UTF-8");
			entityIconService.writeIconCSS(out);

			Cache cache = new Cache();

			cache.setNeverCache(false);
			cache.setMustRevalidate(true);
			cache.setLastModified(new Date());
			cache.setMaxAge(30L);

			resp.setCache(cache);

			resp.setStatus(Status.STATUS_OK);
		} catch (BeCPGException e) {
			logger.error("Cannot generate CSS Entity Icons ", e);
			throw new WebScriptException(e.getMessage());
		} catch (SocketException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		}

	}

}
