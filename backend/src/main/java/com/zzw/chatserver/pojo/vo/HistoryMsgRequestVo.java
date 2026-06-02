package com.zzw.chatserver.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 历史聊天记录查询请求参数（单聊/群聊共用）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryMsgRequestVo {
    /** 会话房间 id，单聊为两用户 id 组合，群聊为群 id */
    private String roomId;
    /** 消息类型筛选：all / img / file */
    private String type;
    /** 关键词，模糊匹配消息文本或文件名 */
    private String query;
    /** 日期筛选，为空时不限制；有值时查询该自然日内的消息 */
    private Date date;
    /** 页码，从 0 开始 */
    private Integer pageIndex;
    /** 每页条数 */
    private Integer pageSize;
}
