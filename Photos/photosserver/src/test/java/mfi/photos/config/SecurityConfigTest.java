package mfi.photos.config;

import mfi.photos.auth.AuthService;
import mfi.photos.auth.UserAuthentication;
import mfi.photos.auth.UserPrincipal;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.RequestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private static final String THE_USER_AGENT = "theUserAgent";
    private static final String THE_KEY = "theKey";
    private static final String THE_TOKEN = "theToken";
    private static final String THE_NEW_TOKEN = "theNewToken";

    @BeforeEach
    public void beforeEach(){
        KeyAccess.getInstance().reset();
        given(authService.checkUserWithPassword("u", "p", THE_USER_AGENT)).
                willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u", "t"), THE_TOKEN)));
        given(authService.checkUserWithToken(THE_TOKEN, THE_USER_AGENT, true)).
                willReturn(Optional.of(new UserAuthentication(new UserPrincipal("u", "t"), THE_NEW_TOKEN)));
        given(authService.requestSecureKey(anyString(), anyString())).willReturn(Optional.of(THE_KEY));
    }

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
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
                .param("login_user", "u").param("login_pass", "p").param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertThat(KeyAccess.getInstance().getKey(), is(THE_KEY));
    }

    @Test
    void testRootAuthFailedHasCookiesNotAccepted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
                .param("login_user", "u").param("login_pass", "p").param("cookieok", "null"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?reason=cookies"));
    }

    @Test
    void testRootAuthFailedWrongLoginData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
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
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
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