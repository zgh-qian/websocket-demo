package com.qian.ws;

import com.alibaba.fastjson.JSON;
import com.qian.config.GetHttpSessionConfig;
import com.qian.utils.MessageUtils;
import com.qian.ws.pojo.Message;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfig.class)
public class ChatEndpoint {

    private static final Map<String, Session> onlineUsers = new ConcurrentHashMap<>();

    private HttpSession httpSession;

    /**
     * 建立 websocket 连接后被调用
     *
     * @param session 会话
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 1. 保存 session
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        onlineUsers.put(httpSession.getAttribute("user").toString(), session);
        // 2. 广播通知
        broadcastToAll(MessageUtils.getMessage(true, null, getFriendsNameList()));
    }

    /**
     * 发送消息被调用
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            // 将消息推送给指定用户
            Message msg = JSON.parseObject(message, Message.class);
            // 获取 session 对象
            Session session = onlineUsers.get(msg.getToName());
            // 发送 同步 消息
            session.getBasicRemote().sendText(
                    MessageUtils.getMessage(false,
                            this.httpSession.getAttribute("user").toString(),
                            msg.getMessage())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭 websocket 连接后被调用
     *
     * @param session 会话
     */
    @OnClose
    public void onClose(Session session) {
        // 1. 移除 session
        onlineUsers.remove(httpSession.getAttribute("user").toString());
        // 2. 广播通知
        broadcastToAll(MessageUtils.getMessage(true, null, getFriendsNameList()));
    }

    /**
     * 广播消息
     *
     * @param message 消息
     */
    private void broadcastToAll(String message) {
        try {
            Set<Map.Entry<String, Session>> entries = onlineUsers.entrySet();
            for (Map.Entry<String, Session> entry : entries) {
                // 获取用户的 session 对象
                Session session = entry.getValue();
                // 发送 同步 消息
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    private Set<String> getFriendsNameList() {
        return onlineUsers.keySet();
    }
}
