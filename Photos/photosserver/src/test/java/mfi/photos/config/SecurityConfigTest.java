package mfi.photos.config;

import mfi.photos.auth.AuthService;
import mfi.photos.auth.UserAuthentication;
import mfi.photos.auth.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testStaticResourcesSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/staticresources/script.js"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testLoginSiteSuccessful() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testRootAuthSuccessful() throws Exception {
        given(authService.checkUserWithPassword("u", "p")).willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u"))));
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .param("login_user", "u").param("login_pass", "p").param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testRootAuthFailedHasCookiesNotAccepted() throws Exception {
        given(authService.checkUserWithPassword("u", "p")).willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u"))));
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .param("login_user", "u").param("login_pass", "p").param("cookieok", "null"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?reason=cookies"));
    }

    @Test
    void testRootAuthFailedWrongLoginData() throws Exception {
        given(authService.checkUserWithPassword("u", "p")).willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u"))));
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .param("login_user", "x").param("login_pass", "y").param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?reason=credentials"));
    }

    @Test
    void testRootAuthFailedNoLoginData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("*://*/login"));
    }

    @Test
    void testPhotoAuthFailedWrongLoginData() throws Exception {
        given(authService.checkUserWithPassword("u", "p")).willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u"))));
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg")
                .param("login_user", "x").param("login_pass", "y").param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?reason=credentials"));
    }

    @Test
    void testPhotoAuthFailedNoLoginData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("*://*/login"));
    }
}