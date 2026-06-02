package com.zzw.chatserver.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 单聊历史记录查询结果 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleHistoryResultVo {
    /** 当前页消息列表 */
    private List<SingleMessageResultVo> msgList;
    /** 符合筛选条件的消息总数，用于前端分页 */
    private Long total;
}
