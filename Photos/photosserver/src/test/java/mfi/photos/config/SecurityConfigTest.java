package mfi.photos.config;

import mfi.photos.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testRootAuthSuccessful() throws Exception {
        given(authService.checkUserWithPassword("u", "p")).willReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .header("user", "u").header("pass", "p"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testRootAuthFailed() throws Exception {
        given(authService.checkUserWithPassword(null, null)).willReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}