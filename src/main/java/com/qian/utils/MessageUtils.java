package com.qian.utils;

import com.alibaba.fastjson.JSON;
import com.qian.ws.pojo.ResultMessage;

public class MessageUtils {

    /**
     * @param isSystemMessage 是否是系统消息。只有广播消息才是系统消息。如果是私聊消息的话，就不是系统消息
     * @param fromName        给谁发消息，如果是系统消息的话，这个参数不需要指定
     * @param message         消息的具体内容
     * @return 结果
     */
    public static String getMessage(boolean isSystemMessage, String fromName, Object message) {
        ResultMessage result = new ResultMessage();
        result.setSystem(isSystemMessage);
        result.setMessage(message);
        if (fromName != null) {
            result.setFromName(fromName);
        }
        return JSON.toJSONString(result);
    }
}
