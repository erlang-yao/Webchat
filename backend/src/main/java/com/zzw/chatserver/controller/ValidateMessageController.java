// TODO: 成员3认领好友管理模块
package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.R;
import com.zzw.chatserver.pojo.ValidateMessage;
import com.zzw.chatserver.pojo.vo.ValidateMessageResponseVo;
import com.zzw.chatserver.service.ValidateMessageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/validate")
public class ValidateMessageController {
    @Resource
    private ValidateMessageService validateMessageService;

    /**
     * 获取我的验证消息列表
     */
    @GetMapping("/getMyValidateMessageList")
    public R getMyValidateMessageList(String userId) {
        List<ValidateMessageResponseVo> validateMessageList = validateMessageService.getMyValidateMessageList(userId);
        return R.ok().data("validateMessageList", validateMessageList);
    }

    /**
     * 查询某条验证消息
     */
    @GetMapping("/getValidateMessage")
    public R getValidateMessage(String roomId, Integer status, Integer validateType) {
        ValidateMessage validateMessage = validateMessageService.findValidateMessage(roomId, status, validateType);
        return R.ok().data("validateMessage", validateMessage);
    }
    /**
     * 重新发送好友验证消息
     */
    @PostMapping("/resend")
    public R resendValidateMessage(@RequestBody Map<String, String> params) {

        // 接收前端传来的消息主键 ID 和 新的留言内容
        String messageId = params.get("id");
        String additionMessage = params.get("additionMessage");

        // 调用 service 层（注意变量名与你 Controller 里注入的保持一致）
        validateMessageService.resendMessage(messageId, additionMessage);

        return R.ok().data("message", "重新发送成功");
    }
}
