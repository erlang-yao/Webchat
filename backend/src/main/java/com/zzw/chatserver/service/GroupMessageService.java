package com.zzw.chatserver.service;

import com.zzw.chatserver.dao.GroupMessageDao;
import com.zzw.chatserver.pojo.GroupMessage;
import com.zzw.chatserver.pojo.vo.GroupHistoryResultVo;
import com.zzw.chatserver.pojo.vo.GroupMessageResultVo;
import com.zzw.chatserver.pojo.vo.HistoryMsgRequestVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class GroupMessageService {
    @Resource
    private GroupMessageDao groupMessageDao;

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 分页查询群聊历史记录，支持按消息类型、关键词、日期筛选
     * 查询条件组装方式与单聊历史查询相同，仅 MongoDB 集合不同
     */
    public GroupHistoryResultVo getGroupHistoryMessages(HistoryMsgRequestVo groupHistoryVo) {
        Criteria cri1 = new Criteria();
        cri1.and("roomId").is(groupHistoryVo.getRoomId());
        Criteria cri2 = null;
        if (!groupHistoryVo.getType().equals("all")) {
            cri1.and("messageType").is(groupHistoryVo.getType())
                    .and("fileRawName").regex(Pattern.compile("^.*" + groupHistoryVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE));
        } else {
            cri2 = new Criteria().orOperator(Criteria.where("message").regex(Pattern.compile("^.*" + groupHistoryVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE)),
                    Criteria.where("fileRawName").regex(Pattern.compile("^.*" + groupHistoryVo.getQuery() + ".*$", Pattern.CASE_INSENSITIVE)));
        }
        if (groupHistoryVo.getDate() != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(groupHistoryVo.getDate());
            calendar.add(Calendar.DATE, 1);
            Date tomorrow = calendar.getTime();
            cri1.and("time").gte(groupHistoryVo.getDate()).lt(tomorrow);
        }
        Query query = new Query();
        if (cri2 != null) query.addCriteria(new Criteria().andOperator(cri1, cri2));
        else query.addCriteria(cri1);
        long count = mongoTemplate.count(query, GroupMessageResultVo.class, "groupmessages");
        query.skip(groupHistoryVo.getPageIndex() * groupHistoryVo.getPageSize());
        query.limit(groupHistoryVo.getPageSize());
        List<GroupMessageResultVo> messageList = mongoTemplate.find(query, GroupMessageResultVo.class, "groupmessages");
        return new GroupHistoryResultVo(messageList, count);
    }

    public GroupMessageResultVo getGroupLastMessage(String roomId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId))
                .with(Sort.by(Sort.Direction.DESC, "_id"));
        GroupMessageResultVo res = mongoTemplate.findOne(query, GroupMessageResultVo.class, "groupmessages");
        if (res == null)
            res = new GroupMessageResultVo();
        return res;
    }

    public List<GroupMessageResultVo> getRecentGroupMessages(String roomId, Integer pageIndex, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId))
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .skip(pageIndex * pageSize)
                .limit(pageSize);
        return mongoTemplate.find(query, GroupMessageResultVo.class, "groupmessages");
    }

    public void addNewGroupMessage(GroupMessage groupMessage) {
        groupMessageDao.save(groupMessage);
    }
}
