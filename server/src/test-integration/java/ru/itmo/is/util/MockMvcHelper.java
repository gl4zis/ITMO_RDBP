package ru.itmo.is.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.itmo.is.entity.user.User;

@Component
public class MockMvcHelper {

    @Autowired(required = false)
    private MockMvc mockMvc;
    @Autowired
    private JwtTestHelper jwtHelper;

    public MockHttpServletRequestBuilder get(String url, User user) {
        return MockMvcRequestBuilders.get(url).header("Authorization", jwtHelper.generateAuthHeader(user));
    }

    public MockHttpServletRequestBuilder get(String url, String login, User.Role role) {
        return MockMvcRequestBuilders.get(url).header("Authorization", jwtHelper.generateAuthHeader(login, role));
    }

    public MockHttpServletRequestBuilder post(String url, User user) {
        return MockMvcRequestBuilders.post(url).header("Authorization", jwtHelper.generateAuthHeader(user));
    }

    public MockHttpServletRequestBuilder post(String url, String login, User.Role role) {
        return MockMvcRequestBuilders.post(url).header("Authorization", jwtHelper.generateAuthHeader(login, role));
    }

    public MockHttpServletRequestBuilder put(String url, User user) {
        return MockMvcRequestBuilders.put(url).header("Authorization", jwtHelper.generateAuthHeader(user));
    }

    public MockHttpServletRequestBuilder put(String url, String login, User.Role role) {
        return MockMvcRequestBuilders.put(url).header("Authorization", jwtHelper.generateAuthHeader(login, role));
    }

    public MockHttpServletRequestBuilder delete(String url, User user) {
        return MockMvcRequestBuilders.delete(url).header("Authorization", jwtHelper.generateAuthHeader(user));
    }

    public MockHttpServletRequestBuilder delete(String url, String login, User.Role role) {
        return MockMvcRequestBuilders.delete(url).header("Authorization", jwtHelper.generateAuthHeader(login, role));
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public boolean isMockMvcAvailable() {
        return mockMvc != null;
    }
}

