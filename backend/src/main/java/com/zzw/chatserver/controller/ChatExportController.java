package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.R;
import com.zzw.chatserver.pojo.vo.ExportRequestVo;
import com.zzw.chatserver.service.ChatExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * 聊天记录导出控制器
 */
@RestController
@RequestMapping("/chatExport")
public class ChatExportController {
    private static final Logger logger = LoggerFactory.getLogger(ChatExportController.class);

    @Resource
    private ChatExportService chatExportService;

    /**
     * 导出聊天记录为 ZIP 文件下载
     */
    @PostMapping("/export")
    public void exportChatHistory(@RequestBody ExportRequestVo requestVo,
                                  HttpServletResponse resp) {
        // 参数校验
        if (requestVo.getRoomId() == null || requestVo.getRoomId().isEmpty()) {
            writeErrorJson(resp, "房间ID不能为空");
            return;
        }
        if (requestVo.getConversationType() == null || requestVo.getConversationType().isEmpty()) {
            writeErrorJson(resp, "会话类型不能为空");
            return;
        }

        try {
            byte[] zipBytes = chatExportService.exportChatHistory(requestVo);

            // 设置响应头
            String fileName = (requestVo.getConversationName() != null
                    ? requestVo.getConversationName()
                    : "聊天记录") + "_chat_records.zip";
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/zip; charset=UTF-8");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setContentLength(zipBytes.length);

            ServletOutputStream outputStream = resp.getOutputStream();
            outputStream.write(zipBytes);
            outputStream.flush();
        } catch (Exception e) {
            logger.error("导出聊天记录失败", e);
            writeErrorJson(resp, "导出失败: " + e.getMessage());
        }
    }

    /** 在响应已提交前输出 JSON 错误 */
    private void writeErrorJson(HttpServletResponse resp, String message) {
        try {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json; charset=UTF-8");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            R error = R.error().message(message);
            // 简单 JSON 序列化（避免依赖 fastjson 的完整序列化）
            String json = "{\"success\":false,\"code\":" + error.getCode()
                    + ",\"message\":\"" + message + "\",\"data\":{}}";
            resp.getWriter().write(json);
        } catch (Exception ignored) {
        }
    }
}
