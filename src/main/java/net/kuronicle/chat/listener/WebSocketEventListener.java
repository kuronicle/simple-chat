package net.kuronicle.chat.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;
import net.kuronicle.chat.context.ConnectedUserManager;
import net.kuronicle.chat.form.ChatForm;

@Component
@Slf4j
public class WebSocketEventListener {

	private static final String MESSAGE_FORMAT_CONNECT = ">> [%s] in.";

	private static final String MESSAGE_FORMAT_DISCONNECT = "<< [%s] out.";

	private SimpMessagingTemplate template;

	private ConnectedUserManager userManager;

	@Autowired
	public WebSocketEventListener(SimpMessagingTemplate template, ConnectedUserManager userManager) {
		this.template = template;
		this.userManager = userManager;
	}

	@EventListener
	public void handleSessionConnectedEvent(SessionConnectedEvent event) {

		// 接続ユーザを接続ユーザ一覧に追加する。
		String login = getLogin(event);
		String sessionId = getSimpSessionId(event);
		if (sessionId != null) {
			userManager.putUser(sessionId, login);
		}
		log.debug("sessionId={}, login={}", sessionId, login);

		ChatForm chatForm = new ChatForm();
		chatForm.setName(ChatForm.NAME_SYSTEM);
		chatForm.setDate(new SimpleDateFormat(ChatForm.DATE_FORMAT).format(new Date(event.getTimestamp())));
		chatForm.setMessage(String.format(MESSAGE_FORMAT_CONNECT, login));
		chatForm.setUserList(userManager.getUserList());
		log.info(chatForm.toString());

		// 入室通知を送信する。
		this.template.convertAndSend("/topic/messages", chatForm);
	}

	private String getSimpSessionId(SessionConnectedEvent event) {
		Message<byte[]> message = event.getMessage();
		if (message == null) {
			return null;
		}

		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor.getAccessor(message,
				SimpMessageHeaderAccessor.class);
		if (simpMessageHeaderAccessor == null) {
			return null;
		}

		String sessionId = (String) simpMessageHeaderAccessor.getHeader(SimpMessageHeaderAccessor.SESSION_ID_HEADER);

		return sessionId;
	}

	private String getLogin(SessionConnectedEvent event) {
		Message<byte[]> message = event.getMessage();
		if (message == null) {
			return null;
		}

		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor.getAccessor(message,
				SimpMessageHeaderAccessor.class);
		if (simpMessageHeaderAccessor == null) {
			return null;
		}

		GenericMessage<?> genericMessage = (GenericMessage<?>) simpMessageHeaderAccessor
				.getHeader(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER);
		if (genericMessage == null) {
			return null;
		}

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.getAccessor(genericMessage,
				StompHeaderAccessor.class);
		if (stompHeaderAccessor == null) {
			return null;
		}

		String login = stompHeaderAccessor.getFirstNativeHeader(StompHeaderAccessor.STOMP_LOGIN_HEADER);

		return login;
	}

	@EventListener
	public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {

		// 接続ユーザを接続ユーザ一覧から削除する。
		String sessionId = event.getSessionId();
		String login = (sessionId == null) ? null : userManager.removeUser(sessionId);
		log.debug("sessionId={}, login={}", sessionId, login);

		ChatForm chatForm = new ChatForm();
		chatForm.setName(ChatForm.NAME_SYSTEM);
		chatForm.setDate(new SimpleDateFormat(ChatForm.DATE_FORMAT).format(new Date(event.getTimestamp())));
		chatForm.setMessage(String.format(MESSAGE_FORMAT_DISCONNECT, login));
		chatForm.setUserList(userManager.getUserList());
		log.info(chatForm.toString());

		// 退室通知を送信する。
		this.template.convertAndSend("/topic/messages", chatForm);
	}
}