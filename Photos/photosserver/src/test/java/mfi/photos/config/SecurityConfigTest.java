package mfi.photos.config;

import mfi.files.api.DeviceType;
import mfi.files.api.TokenResult;
import mfi.files.api.UserService;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.RequestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;

import java.util.Objects;

import static mfi.photos.util.RequestUtil.COOKIE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;


@SpringBootTest(properties = {"technicalUser=" + SecurityConfigTest.THE_TECHNICAL_USER})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private UserService userService;

    static final String THE_USER_NAME = "theUserName";
    static final String THE_PASSWORD = "thePassword";
    static final String THE_USER_AGENT = "theUserAgent";
    static final String THE_TOKEN = "theToken";
    static final String THE_NEW_TOKEN = "theNewToken";
    static final String THE_TECHNICAL_USER = "theTechnicalUser";
    static final String THE_EXTERNAL_KEY = "theExternalKey";

    @BeforeEach
    public void beforeEach(){

        KeyAccess.getInstance().reset();
        cacheManager.getCacheNames().forEach(c -> Objects.requireNonNull(cacheManager.getCache(c)).clear());

        given(userService.userNameFromLoginCookie(anyString())).willReturn(null);
        given(userService.userNameFromLoginCookie(THE_TOKEN)).willReturn(THE_USER_NAME);
        given(userService.userNameFromLoginCookie(THE_NEW_TOKEN)).willReturn(THE_USER_NAME);

        given(userService.deleteToken(THE_USER_NAME, THE_USER_AGENT, DeviceType.BROWSER)).willReturn(true);

        given(userService.createToken(anyString(), anyString(), anyString(), any(DeviceType.class)))
                .willReturn(new TokenResult(false, null));
        given(userService.createToken(THE_USER_NAME, THE_PASSWORD, THE_USER_AGENT, DeviceType.BROWSER))
                .willReturn(new TokenResult(true, THE_TOKEN));

        given(userService.checkToken(anyString(), anyString(), anyString(), any(DeviceType.class), anyBoolean()))
                .willReturn(new TokenResult(false, null));
        given(userService.checkToken(THE_USER_NAME, THE_TOKEN, THE_USER_AGENT, DeviceType.BROWSER, false))
                .willReturn(new TokenResult(true, null));
        given(userService.checkToken(THE_USER_NAME, THE_TOKEN, THE_USER_AGENT, DeviceType.BROWSER, true))
                .willReturn(new TokenResult(true, THE_NEW_TOKEN));

        given(userService.readExternalKey(anyString(), anyString(), anyString(), any(DeviceType.class), anyString()))
                .willReturn(new TokenResult(false, null));
        given(userService.readExternalKey(THE_USER_NAME, THE_TOKEN, THE_USER_AGENT, DeviceType.BROWSER, THE_TECHNICAL_USER))
                .willReturn(new TokenResult(true, THE_EXTERNAL_KEY));
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
                .param("login_user", THE_USER_NAME).param("login_pass", THE_PASSWORD).param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertThat(KeyAccess.getInstance().getKey(), is(THE_EXTERNAL_KEY));
    }

    @Test
    void testRootAuthFailedHasCookiesNotAccepted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
                .param("login_user", THE_USER_NAME).param("login_pass", THE_PASSWORD).param("cookieok", "null"))
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
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));
    }

    @Test
    void testPhotoAuthFailedWrongCredentials() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
                .param("login_user", "x").param("login_pass", "y").param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testPhotoAuthFailedNoLoginData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testRootAuthWithCredentials() throws Exception {
        // Login with credentials, creates cookie
        mockMvc.perform(MockMvcRequestBuilders.post("/")
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT)
                .param("login_user", THE_USER_NAME).param("login_pass", THE_PASSWORD).param("cookieok", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(cookie().exists(RequestUtil.COOKIE_NAME));
        assertThat(KeyAccess.getInstance().getKey(), is(THE_EXTERNAL_KEY));
        // Request for photo, provides cookie for auth
        Cookie loginCookie = new Cookie(COOKIE_NAME, THE_TOKEN);
        loginCookie.setMaxAge(60 * 60 * 24 * 92);
        mockMvc.perform(MockMvcRequestBuilders.get("/assets/album/photo.jpg")
                .cookie(loginCookie)
                .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @SuppressWarnings("CallToThreadRun")
    @Test
    void testRootAuthWithTokenCaching() throws Exception {
        Runnable r = () -> {
            Cookie loginCookie = new Cookie(COOKIE_NAME, THE_TOKEN);
            loginCookie.setMaxAge(60 * 60 * 24 * 92);
            try {
                mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .cookie(loginCookie)
                        .header(RequestUtil.HEADER_USER_AGENT, THE_USER_AGENT))
                        .andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        Thread t = new Thread(r);
        // first run calls UserService
        t.run();
        verify(userService, times(1))
                .checkToken(anyString(), anyString(), anyString(), any(DeviceType.class), anyBoolean());
        // second run uses cache
        t.run();
        verify(userService, times(1))
                .checkToken(anyString(), anyString(), anyString(), any(DeviceType.class), anyBoolean());
        // wait for evicting cached token
        Thread.sleep(1600L);
        t.run();
        verify(userService, times(2))
                .checkToken(anyString(), anyString(), anyString(), any(DeviceType.class), anyBoolean());
    }
}