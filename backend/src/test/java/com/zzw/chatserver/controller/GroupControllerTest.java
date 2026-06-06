package com.zzw.chatserver.controller;

import com.zzw.chatserver.common.ResultEnum;
import com.zzw.chatserver.pojo.Group;
import com.zzw.chatserver.service.GroupService;
import com.zzw.chatserver.service.GroupUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupUserService groupUserService;
    @Mock
    private GroupService groupService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GroupController controller = new GroupController();
        ReflectionTestUtils.setField(controller, "groupUserService", groupUserService);
        ReflectionTestUtils.setField(controller, "groupService", groupService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-a", null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createGroupReturnsGeneratedCode() throws Exception {
        when(groupService.createGroup(any())).thenReturn("10000002");

        mockMvc.perform(post("/group/createGroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"test\",\"desc\":\"desc\",\"holderName\":\"usera\","
                                + "\"holderUserId\":\"507f1f77bcf86cd799439011\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())))
                .andExpect(jsonPath("$.data.groupCode", equalTo("10000002")));
    }

    @Test
    void getGroupInfoReturnsGroupAndMembers() throws Exception {
        Group group = new Group();
        group.setGid("group-1");
        group.setTitle("test-group");
        when(groupService.getGroupInfo("group-1")).thenReturn(group);
        when(groupUserService.getGroupUsersByGroupId("group-1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/group/getGroupInfo").param("groupId", "group-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupInfo.title", equalTo("test-group")))
                .andExpect(jsonPath("$.data.users").isArray());
    }

    @Test
    void listGroupsReturnsCurrentResponseShape() throws Exception {
        when(groupService.getAllGroup()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/group/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allGroup").isArray());
    }

    @Test
    void quitGroupAllowsCurrentUser() throws Exception {
        mockMvc.perform(post("/group/quitGroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"group-1\",\"userId\":\"user-a\",\"holder\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.SUCCESS.getCode())));

        verify(groupService).quitGroup(any());
    }

    @Test
    void quitGroupRejectsDifferentUser() throws Exception {
        mockMvc.perform(post("/group/quitGroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"group-1\",\"userId\":\"user-b\",\"holder\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(ResultEnum.ILLEGAL_OPERATION.getCode())));

        verify(groupService, never()).quitGroup(any());
    }

    @Test
    void updateGroupNoticeUsesAuthenticatedUser() throws Exception {
        Group updated = new Group();
        updated.setGid("group-1");
        updated.setNotice("new notice");
        when(groupService.updateGroupNotice("group-1", "new notice", "user-a")).thenReturn(updated);

        mockMvc.perform(post("/group/updateGroupNotice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"group-1\",\"notice\":\"new notice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupInfo.notice", equalTo("new notice")));
    }
}
