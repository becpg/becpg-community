package fr.becpg.web.experimental;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * <p>BeCPGWSHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@ServerEndpoint(value = "/becpgws/{store_type}/{store_id}/{id}/{user}")
public class BeCPGWSHandler {

	private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();

	private static final Log logger = LogFactory.getLog(BeCPGWSHandler.class);

	/**
	 * <p>onOpen.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param room a {@link java.lang.String} object.
	 * @param user a {@link java.lang.String} object.
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("id") final String room, @PathParam("user") final String user) {
		logger.debug("Connected ... " + session.getId() + " to room " + room);

		session.getUserProperties().put("room", room);
		session.getUserProperties().put("user", user);
		BeCPGWSHandler.userSessions.put(session.getId(), session);

		try {

			for (Session s : BeCPGWSHandler.userSessions.values()) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					session.getBasicRemote().sendText("{\"type\":\"JOINING\",\"user\":\"" + s.getUserProperties().get("user") + "\"}");
					s.getBasicRemote().sendText("{\"type\":\"JOINING\",\"user\":\"" + user + "\"}");
				}
			}

		} catch (IOException e) {
			logger.error("onMessage failed", e);
		}
	}

	/**
	 * <p>onMessage.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param session a {@link jakarta.websocket.Session} object.
	 */
	@OnMessage
	public void onMessage(String message, Session session) {

		logger.debug("Receiving ... " + session.getId());
		String room = (String) session.getUserProperties().get("room");
		try {
			for (Session s : BeCPGWSHandler.userSessions.values()) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					s.getBasicRemote().sendText(message);
				}
			}
		} catch (IOException e) {
			logger.warn(e.getMessage());
			logger.debug("onMessage failed", e);
		}

	}

	/**
	 * <p>onClose.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param closeReason a {@link jakarta.websocket.CloseReason} object.
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.debug(String.format("Session %s closed because of %s", session.getId(), closeReason));
		BeCPGWSHandler.userSessions.remove(session.getId());
		try {
			String room = (String) session.getUserProperties().get("room");
			String user = (String) session.getUserProperties().get("user");

			for (Session s : BeCPGWSHandler.userSessions.values()) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					s.getBasicRemote().sendText("{\"type\":\"LEAVING\",\"user\":\"" + user + "\"}");
				}
			}

		} catch (IOException e) {
			logger.warn(e.getMessage());
			logger.debug("onClose failed", e);
		}
	}

	/**
	 * <p>onError.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param thr a {@link java.lang.Throwable} object.
	 */
	@OnError
	public void onError(Session session, Throwable thr) {
		if (!session.isOpen()) {
			BeCPGWSHandler.userSessions.remove(session.getId());
		}
		if (logger.isDebugEnabled()) {
			logger.debug(thr, thr);
		}
	}

}
