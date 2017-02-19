$(function() {

	// ユーザリストを更新する。
	$.getJSON('http://' + location.host + '/users').done(function(data) {
		var userList = '<ul><li>' + data.join('</li><li>') + '</li></ul>';
		$('#user-list').html(userList);
	});

	/**
	 * 初期化処理
	 */
	var ChatStomp = function() {
		this.endpoint = 'ws://' + location.host + '/endpoint';

		this.notificationFlag = false; // 非フォーカス時の通知ON/OFFフラグ
		this.isFocused = true; // メッセージ入力フォーム フォーカス判定

		this.connectButton = document.getElementById('connect');
		this.disconnectButton = document.getElementById('disconnect');
		this.sendButton = document.getElementById('send');
		this.messageText = document.getElementById('message');
		this.notificationCheckBox = document
				.getElementById("notification-checkbox");

		// イベントハンドラの登録
		this.connectButton.addEventListener('click', this.connect.bind(this));
		this.disconnectButton.addEventListener('click', this.disconnect
				.bind(this));
		this.sendButton.addEventListener('click', this.sendMessage.bind(this));
		this.messageText.addEventListener('input', this.setSendableStatus
				.bind(this));
		this.messageText.addEventListener('focus', this.focus.bind(this));
		this.messageText.addEventListener('blur', this.blur.bind(this));
		this.notificationCheckBox.addEventListener('click',
				this.setNotification.bind(this));
	};

	/**
	 * エンドポイントへの接続処理
	 */
	ChatStomp.prototype.connect = function() {
		var socket = new WebSocket(this.endpoint); // エンドポイントのURL
		this.stompClient = Stomp.over(socket); // WebSocketを使ったStompクライアントを作成
		var name = document.getElementById('name').value;
		var headers = {
			login : name,
			passcode : null,
		};
		this.stompClient.connect(headers, this.onConnected.bind(this)); // エンドポイントに接続し、接続した際のコールバックを登録
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

		// フォーカスを入力メッセージに移す。
		document.getElementById('message').focus();
		this.setSendableStatus();
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
		dt.appendChild(document.createTextNode(JSON.parse(message.body).date
				+ ' ' + JSON.parse(message.body).name));
		dl.appendChild(dt);

		var dd = document.createElement('dd');
		var messageText = JSON.parse(message.body).message.replace(/\r?\n/g,
				'<br />');
		dd.innerHTML = messageText;
		dl.appendChild(dd);

		// 接続ユーザリストの更新
		var userList = '<ul><li>'
				+ JSON.parse(message.body).userList.join('</li><li>')
				+ '</li></ul>';
		var divUserList = document.getElementById('user-list');
		divUserList.innerHTML = userList;

		// 一番下にスクロールする。
		var scrollHeight = document.body.scrollHeight;
		window.scrollTo(0, scrollHeight);

		// 非フォーカス状態で通知が有効である場合は通知する。
		if (this.isFocused == false && this.notificationFlag == true) {
			var n = new Notification(JSON.parse(message.body).name + ": "
					+ JSON.parse(message.body).message);
		}
	};

	/**
	 * 宛先'/app/message'へのメッセージ送信処理
	 */
	ChatStomp.prototype.sendMessage = function() {
		var name = document.getElementById('name').value;
		var today = formatDate(new Date());
		var json_message = {
			date : today,
			name : name,
			message : document.getElementById('message').value
		};
		this.stompClient.send("/app/message", {}, JSON.stringify(json_message));
		// 入力メッセージをクリアして、フォーカスを入力メッセージに戻す。
		document.getElementById('message').value = '';
		document.getElementById('message').focus();
		this.setSendableStatus();
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
	 * 通知切り替え
	 */
	ChatStomp.prototype.setNotification = function() {
		// CheckboxがOFFの場合はfalse
		if (this.notificationCheckBox.checked == false) {
			this.notificationFlag = false;
			return;
		}

		// Notificationが無効な場合はfalse
		if (!window.Notification) {
			console.log('not support Notification.');
			this.notificationFlag = false;
			return;
		}

		// Notificationに許可が出ていればtrue
		if (Notification.permission === 'granted') {
			this.notificationFlag = true;
			return;
		}

		// 許可が取れていない場合はNotificationの許可を取る
		Notification.requestPermission(function(result) {
			if (result === 'granted') {
				console.log('Notification permittion is granted.');
				this.notificationFlag = true;
				return;
			}
			console.log('Notification permittion is denied.');
			this.notificationFlag = false;
			return;
		})
	}

	/**
	 * フォーカス時処理
	 */
	ChatStomp.prototype.focus = function() {
		this.isFocused = true;
	}

	/**
	 * アンフォーカス時処理
	 */
	ChatStomp.prototype.blur = function() {
		this.isFocused = false;
	}

	/**
	 * 日付をフォーマットする
	 */
	var formatDate = function(date) {
		format = 'yyyy-MM-dd HH:mm:ss';
		format = format.replace(/yyyy/g, date.getFullYear());
		format = format.replace(/MM/g, ('0' + (date.getMonth() + 1)).slice(-2));
		format = format.replace(/dd/g, ('0' + date.getDate()).slice(-2));
		format = format.replace(/HH/g, ('0' + date.getHours()).slice(-2));
		format = format.replace(/mm/g, ('0' + date.getMinutes()).slice(-2));
		format = format.replace(/ss/g, ('0' + date.getSeconds()).slice(-2));
		return format;
	};

	/**
	 * 「Enter」キー押下で送信する
	 */
	shortcut.add("Enter", function() {
		document.getElementById('send').click();
	});

	new ChatStomp();
});