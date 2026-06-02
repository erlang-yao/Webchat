package com.zzw.chatserver.service;

import com.zzw.chatserver.dao.SingleMessageDao;
import com.zzw.chatserver.pojo.SingleMessage;
import com.zzw.chatserver.pojo.vo.HistoryMsgRequestVo;
import com.zzw.chatserver.pojo.vo.IsReadMessageRequestVo;
import com.zzw.chatserver.pojo.vo.SingleHistoryResultVo;
import com.zzw.chatserver.pojo.vo.SingleMessageResultVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SingleMessageService {
    @Resource
    private SingleMessageDao singleMessageDao;
    @Resource
    private MongoTemplate mongoTemplate;

    public SingleMessageResultVo getLastMessage(String roomId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId))
                .with(Sort.by(Sort.Direction.DESC, "_id"));
        SingleMessageResultVo message = mongoTemplate.findOne(query, SingleMessageResultVo.class, "singlemessages");
        if (message == null) {
            message = new SingleMessageResultVo();
            message.setRoomId(roomId);
            message.setMessageType("text");
            message.setMessage("");
        }
        return message;
    }

    //获取好友之间的聊天记录，通过房间id来获取，房间id是由两个好友id组成，所以是唯一的
    public List<SingleMessageResultVo> getRecentMessage(String roomId, Integer pageIndex, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId))
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .skip(pageIndex * pageSize)
                .limit(pageSize);
        return mongoTemplate.find(query, SingleMessageResultVo.class, "singlemessages");
    }

    //在用户切换到某条会话之后将给会话下的所有消息设置为已读
    public void userIsReadMessage(IsReadMessageRequestVo ivo) {
        Update update = new Update();
        update.addToSet("isReadUser", ivo.getUserId());
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(ivo.getRoomId()));
        mongoTemplate.updateMulti(query, update, "singlemessages");
    }

    /**
     * 分页查询单聊历史记录，支持按消息类型、关键词、日期筛选
     * 筛选逻辑与群聊历史查询保持一致，便于前端复用同一套 UI
     */
    public SingleHistoryResultVo getSingleHistoryMsg(HistoryMsgRequestVo historyMsgRequestVo) {
        // cri1：必选条件（房间 id，以及可选的日期范围）
        Criteria cri1 = new Criteria();
        cri1.and("roomId").is(historyMsgRequestVo.getRoomId());
        // cri2：type=all 时的关键词 OR 条件（匹配文本内容或文件名）
        Criteria cri2 = null;
        if (!historyMsgRequestVo.getType().equals("all")) {
            // 指定类型（img/file）时，按 messageType 过滤，并在 fileRawName 中模糊搜索
            cri1.and("messageType").is(historyMsgRequestVo.getType())
                    .and("fileRawName").regex(Pattern.compile("^.*" + historyMsgRequestVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE));
        } else {
            // 全部类型时，关键词同时匹配 message 文本与 fileRawName
            cri2 = new Criteria().orOperator(Criteria.where("message").regex(Pattern.compile("^.*" + historyMsgRequestVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE)),
                    Criteria.where("fileRawName").regex(Pattern.compile("^.*" + historyMsgRequestVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE)));
        }
        if (historyMsgRequestVo.getDate() != null) {
            // 按选定日期查询当天消息：[date, date+1)
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(historyMsgRequestVo.getDate());
            calendar.add(Calendar.DATE, 1);
            Date tomorrow = calendar.getTime();
            cri1.and("time").gte(historyMsgRequestVo.getDate()).lt(tomorrow);
        }
        Query query = new Query();
        if (cri2 != null) query.addCriteria(new Criteria().andOperator(cri1, cri2));
        else query.addCriteria(cri1);
        long total = mongoTemplate.count(query, SingleMessageResultVo.class, "singlemessages");
        query.skip(historyMsgRequestVo.getPageIndex() * historyMsgRequestVo.getPageSize());
        query.limit(historyMsgRequestVo.getPageSize());
        List<SingleMessageResultVo> messageList = mongoTemplate.find(query, SingleMessageResultVo.class, "singlemessages");
        return new SingleHistoryResultVo(messageList, total);
    }

    public void addNewSingleMessage(SingleMessage message) {
        singleMessageDao.save(message);
    }

}
