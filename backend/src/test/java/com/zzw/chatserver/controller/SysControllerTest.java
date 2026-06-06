package com.zzw.chatserver.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SysControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SysController()).build();
    }

    @Test
    void getFaceImagesReturnsConfiguredRegistrationFaces() throws Exception {
        mockMvc.perform(get("/sys/getFaceImages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.files", hasSize(6)))
                .andExpect(jsonPath("$.data.files", contains(
                        "face1.jpg",
                        "face2.jpg",
                        "face3.jpg",
                        "face4.jpg",
                        "face5.jpg",
                        "face6.jpg"
                )));
    }
}
