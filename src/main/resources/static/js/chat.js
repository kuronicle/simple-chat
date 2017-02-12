const
EMPTY_NAME = 'ななし';

/**
 * 初期化処理
 */
var ChatStomp = function() {
	this.endpoint = 'ws://' + location.host + '/endpoint';

	this.connectButton = document.getElementById('connect');
	this.disconnectButton = document.getElementById('disconnect');
	this.sendButton = document.getElementById('send');
	this.messageText = document.getElementById('message');

	// イベントハンドラの登録
	this.connectButton.addEventListener('click', this.connect.bind(this));
	this.disconnectButton.addEventListener('click', this.disconnect.bind(this));
	this.sendButton.addEventListener('click', this.sendMessage.bind(this));
	this.messageText.addEventListener('input', this.setSendableStatus
			.bind(this));
};

/**
 * エンドポイントへの接続処理
 */
ChatStomp.prototype.connect = function() {
	var socket = new WebSocket(this.endpoint); // エンドポイントのURL
	this.stompClient = Stomp.over(socket); // WebSocketを使ったStompクライアントを作成
	this.stompClient.connect({}, this.onConnected.bind(this)); // エンドポイントに接続し、接続した際のコールバックを登録
};

/**
 * エンドポイントへ接続したときの処理
 */
ChatStomp.prototype.onConnected = function(frame) {
	console.log('Connected: ' + frame);
	// 宛先が'/topic/messages'のメッセージを購読し、コールバック処理を登録
	this.stompClient.subscribe('/topic/messages', this.onSubscribeMessage
			.bind(this));
	this.setConnected(true);
};

/**
 * 受信したメッセージを画面に表示する処理
 */
ChatStomp.prototype.onSubscribeMessage = function(message) {

	// 名前:messageを分解

	var messages = document.getElementById('messages');
	var dl = document.createElement('dl');
	messages.appendChild(dl);

	var dt = document.createElement('dt');
	var span_date = document.createElement('span');
	dt.appendChild(document.createTextNode(JSON.parse(message.body).date + ' '
			+ JSON.parse(message.body).name));
	dl.appendChild(dt);

	var dd = document.createElement('dd');
	dd.appendChild(document.createTextNode(JSON.parse(message.body).message));
	dl.appendChild(dd);

	// 一番下にスクロールする。
	var scrollHeight = document.body.scrollHeight;
	window.scrollTo(0, scrollHeight);
};

/**
 * 宛先'/app/message'へのメッセージ送信処理
 */
ChatStomp.prototype.sendMessage = function() {
	var name = document.getElementById('name').value;
	if (!name)
		name = EMPTY_NAME;
	var today = new Date();
	var json_message = {
		date : today.toLocaleString(),
		name : name,
		message : document.getElementById('message').value
	};
	this.stompClient.send("/app/message", {}, JSON.stringify(json_message));
	// 入力メッセージをクリアして、フォーカスを入力メッセージに戻す。
	document.getElementById('message').value = '';
	document.getElementById('message').focus();
};

/**
 * メッセージ入力に応じた送信ボタン表示の切り替え
 */
ChatStomp.prototype.setSendableStatus = function() {
	var message = document.getElementById('message').value || '';
	var connected = this.connectButton.disabled;
	this.canSubmit(connected && message.length > 0);
};

/**
 * 接続切断処理
 */
ChatStomp.prototype.disconnect = function() {
	if (this.stompClient) {
		this.stompClient.disconnect();
		this.stompClient = null;
	}
	this.setConnected(false);
};

/**
 * 有効/無効切り替え
 */
ChatStomp.prototype.setConnected = function(connected) {
	this.connectButton.disabled = connected;
	this.disconnectButton.disabled = !connected;
	this.setSendableStatus();
};

/**
 * 送信ボタン有効/無効切り替え
 */
ChatStomp.prototype.canSubmit = function(enabled) {
	this.sendButton.disabled = !enabled;
};

/**
 * 「Enter」キー押下で送信する
 */
shortcut.add("Enter",function(){
	document.getElementById('send').click();
});

new ChatStomp();