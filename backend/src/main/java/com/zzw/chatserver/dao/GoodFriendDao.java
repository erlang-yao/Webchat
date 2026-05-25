// TODO: 成员3认领好友管理模块
package com.zzw.chatserver.dao;

import com.zzw.chatserver.pojo.GoodFriend;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface GoodFriendDao extends MongoRepository<GoodFriend, ObjectId> {
}

