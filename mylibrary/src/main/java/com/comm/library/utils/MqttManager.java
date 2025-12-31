package com.comm.library.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;


/**
 * @author xqm
 * @date 2025/7/18 17:01
 * @description MqttManager 类功能说明
 */
public class MqttManager {
    private static final String TAG = "MqttManager";
    private static MqttManager instance;
    private final MqttAndroidClient mqttAndroidClient;
    private final MqttConnectOptions connectOptions;

    private String serverUri = "tcp://broker.hivemq.co:1883"; // 可替换为你自己的服务器
    private final String clientId = "AndroidClient_" + System.currentTimeMillis();
    private final Context context;

    private OnMessageReceiveListener onMessageReceiveListener;

    private MutableLiveData<MqttMessageEvent> messageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> onReconnectLiveData = new MutableLiveData<>();

    public MqttManager(Context context,String serverUriAdd,String clientIdAdd,String userName,String pwd) {
        this.context = context.getApplicationContext();
        mqttAndroidClient = new MqttAndroidClient(context, serverUriAdd, clientIdAdd);
        connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(userName);
        connectOptions.setPassword(pwd.toCharArray());
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);

        initCallback();
    }

    // 单例获取方法
    public static synchronized MqttManager getInstance(Context context, String serverUriAdd, String clientIdAdd, String userName, String pwd) {
        if (instance == null) {
            instance = new MqttManager(context, serverUriAdd, clientIdAdd, userName, pwd);
        }
        return instance;
    }

    // 可选：无参数获取（需提前初始化）
    public static synchronized MqttManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MqttManager 尚未初始化！");
        }
        return instance;
    }

    public boolean isConnected() {
        return mqttAndroidClient != null && mqttAndroidClient.isConnected();
    }

    private void initCallback() {
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "MQTT 重连成功");
                    if (onReconnectLiveData != null) {
                        onReconnectLiveData.postValue(true);
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "连接丢失，准备重连", cause);
//                reconnectWithDelay();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                Log.d(TAG, "收到消息: topic=" + topic + ", message=" + payload);
                if (onMessageReceiveListener != null) {
                    onMessageReceiveListener.onMessage(topic, payload);
                }
                // 使用 LiveData 发送消息到所有观察者
                MqttMessageEvent event = new MqttMessageEvent(topic, payload);
                messageLiveData.postValue(event);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "消息发送完成");
            }
        });
    }

    public void connect() {
        if(mqttAndroidClient.isConnected()){
            Log.d(TAG, "已经连接，无需重复连接");
        }
        try {
            mqttAndroidClient.connect(connectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "连接成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "连接失败", exception);
//                    reconnectWithDelay();
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "连接异常", e);
        }
    }

    private void reconnectWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(this::connect, 3000);
    }

    public void subscribe(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "订阅成功: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "订阅失败: " + topic, exception);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "订阅异常", e);
        }
    }

    public void unsubscribe(final String topic) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                Log.w(TAG, "取消订阅失败，MQTT 未连接");
                return;
            }

            mqttAndroidClient.unsubscribe(topic, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "取消订阅成功: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "取消订阅失败: " + topic, exception);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "取消订阅异常", e);
        }
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
            mqttAndroidClient.publish(topic, mqttMessage);
            Log.d(TAG, "发送消息: topic=" + topic + ", message=" + message);
        } catch (MqttException e) {
            Log.e(TAG, "发送失败", e);
        }
    }

    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
            Log.d(TAG, "断开连接");
        } catch (MqttException e) {
            Log.e(TAG, "断开异常", e);
        }
    }

    public void setOnMessageReceiveListener(OnMessageReceiveListener listener) {
        this.onMessageReceiveListener = listener;
    }

    public interface OnMessageReceiveListener {
        void onMessage(String topic, String message);
    }

    public static boolean isInitialized() {
        return instance != null;
    }
    public void removeOnMessageReceiveListener() {
        this.onMessageReceiveListener = null;
    }


    // 提供 LiveData 给外部观察
    public LiveData<MqttMessageEvent> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Boolean> getOnReconnectLiveData() {
        return onReconnectLiveData;
    }
    // 消息事件类，包含主题和内容
    public static class MqttMessageEvent {
        private String topic;
        private String message;

        public MqttMessageEvent(String topic, String message) {
            this.topic = topic;
            this.message = message;
        }

        public String getTopic() { return topic; }
        public String getMessage() { return message; }
    }


}
