package net.kuronicle.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.kuronicle.chat.form.ChatForm;

@RestController
@Slf4j
public class ChatController {

	/**
	 * メッセージを受付し、/topic/messagesにメッセージを送信するメソッドです
	 *
	 * @param chatForm
	 *            接続者名とメッセージ(json形式)
	 * @return chatForm 接続者名とメッセージ(json形式)
	 */
	@MessageMapping(value = "/message" /* 宛先名 */)
	@SendTo(value = "/topic/messages") // 処理結果の送り先
	ChatForm greet(ChatForm chatForm) {
		log.info("received date:{}, name:{}, message:{}", chatForm.getDate(), chatForm.getName(), chatForm.getMessage());
		return chatForm;
	}
}
