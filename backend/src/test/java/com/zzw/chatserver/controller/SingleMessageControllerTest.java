package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.ResultEnum;
import com.zzw.chatserver.pojo.vo.SingleHistoryResultVo;
import com.zzw.chatserver.pojo.vo.SingleMessageResultVo;
import com.zzw.chatserver.service.SingleMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SingleMessageControllerTest {

    @Mock
    private SingleMessageService singleMessageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SingleMessageController controller = new SingleMessageController();
        ReflectionTestUtils.setField(controller, "singleMessageService", singleMessageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getLastMessageReturnsServiceResult() throws Exception {
        SingleMessageResultVo message = new SingleMessageResultVo();
        message.setId("message-1");
        message.setMessage("hello");
        when(singleMessageService.getLastMessage("room-1")).thenReturn(message);

        mockMvc.perform(get("/singleMessage/getLastMessage").param("roomId", "room-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())))
                .andExpect(jsonPath("$.data.singleLastMessage.message", equalTo("hello")));
    }

    @Test
    void getRecentMessagesPassesPagination() throws Exception {
        when(singleMessageService.getRecentMessage("room-1", 0, 20))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/singleMessage/getRecentSingleMessages")
                        .param("roomId", "room-1")
                        .param("pageIndex", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentMessage").isArray());

        verify(singleMessageService).getRecentMessage("room-1", 0, 20);
    }

    @Test
    void markReadDelegatesToService() throws Exception {
        mockMvc.perform(post("/singleMessage/isRead")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"room-1\",\"userId\":\"user-a\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())));

        verify(singleMessageService).userIsReadMessage(any());
    }

    @Test
    void historyReturnsMessagesAndTotal() throws Exception {
        SingleMessageResultVo message = new SingleMessageResultVo();
        message.setId("message-1");
        when(singleMessageService.getSingleHistoryMsg(any()))
                .thenReturn(new SingleHistoryResultVo(Collections.singletonList(message), 1L));

        mockMvc.perform(post("/singleMessage/historyMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":\"room-1\",\"pageIndex\":0,\"pageSize\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", equalTo(1)))
                .andExpect(jsonPath("$.data.msgList[0].id", equalTo("message-1")));
    }
}
