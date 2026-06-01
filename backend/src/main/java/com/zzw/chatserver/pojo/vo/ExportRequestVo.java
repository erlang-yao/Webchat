package com.zzw.chatserver.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天记录导出请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestVo {
    /** 会话房间ID */
    private String roomId;
    /** 会话类型：FRIEND / GROUP */
    private String conversationType;
    /** 会话名称（用于生成文件夹名和 ZIP 文件名） */
    private String conversationName;
    /** 可选的起始日期 (ISO格式 yyyy-MM-dd) */
    private String startDate;
    /** 可选的结束日期 (ISO格式 yyyy-MM-dd) */
    private String endDate;
    /** 可选的消息类型筛选（空=全部）：text, img, file, voice, video, audio, sys */
    private List<String> types;
}
