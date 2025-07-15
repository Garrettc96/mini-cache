package com.minicache.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicache.app.dto.PutCacheDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiServerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPutAndGetAndDeleteKey() throws Exception {
        String key = "foo";
        String value = "bar";
        PutCacheDto putDto = new PutCacheDto(value);
        String putJson = objectMapper.writeValueAsString(putDto);

        // PUT
        mockMvc.perform(put("/cache/" + key)
                .contentType(MediaType.APPLICATION_JSON)
                .content(putJson))
                .andExpect(status().isOk());

        // GET
        mockMvc.perform(get("/cache/" + key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(value));

        // DELETE
        mockMvc.perform(delete("/cache/" + key))
                .andExpect(status().isOk());

        // GET after DELETE
        mockMvc.perform(get("/cache/" + key))
                .andExpect(status().isNotFound());
    }
} 