package net.kuronicle.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.kuronicle.chat.context.ConnectedUserManager;
import net.kuronicle.chat.form.ChatForm;

@RestController
@Slf4j
public class ChatController {

	/**
	 * ユーザ管理。
	 */
	private ConnectedUserManager userManager;

	@Autowired
	public ChatController(ConnectedUserManager userManager) {
		this.userManager = userManager;
	}

	/**
	 * メッセージを受付し、/topic/messagesにメッセージを送信するメソッドです
	 *
	 * @param chatForm
	 *            接続者名とメッセージ(json形式)
	 * @return chatForm 接続者名とメッセージ(json形式)
	 */
	@MessageMapping(value = "/message" /* 宛先名 */)
	@SendTo(value = "/topic/messages") // 処理結果の送り先
	ChatForm receiveMessage(ChatForm chatForm) {
		log.info(chatForm.toString());
		chatForm.setUserList(userManager.getUserList());
		return chatForm;
	}

	@RequestMapping(value = "/users")
	List<String> getUserList() {
		return userManager.getUserList();
	}
}
