package com.zk.projectboot.security;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void publishedArticlesRemainPublic() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk());
    }

    @Test
    void loginCreatesSecuritySessionWithoutStoringUserEntity() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"lisi\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute("user"));

        mockMvc.perform(get("/api/auth/user-info").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("lisi"));
    }

    @Test
    void anonymousMutationIsRejectedAsUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/users/3")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"forged\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("lisi")
    void authenticatedMutationRequiresCsrfToken() throws Exception {
        mockMvc.perform(put("/api/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"safe\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("lisi")
    void regularUserCannotOpenAdminApi() throws Exception {
        mockMvc.perform(patch("/api/admin/users/4/ban").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithUserDetails("lisi")
    void userCanUpdateOwnProfile() throws Exception {
        mockMvc.perform(put("/api/users/3")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"safe\",\"bio\":\"profile\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("safe"));
    }

    @Test
    @WithUserDetails("lisi")
    void userCannotUpdateAnotherProfile() throws Exception {
        mockMvc.perform(put("/api/users/4")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"forged\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotReadAnotherUsersDraft() throws Exception {
        mockMvc.perform(get("/api/articles/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithUserDetails("lisi")
    void articleAuthorComesFromPrincipalInsteadOfAuthorIdParameter() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .with(csrf())
                        .param("authorId", "4")
                        .param("categoryId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Owned by principal\",\"content\":\"content\",\"tagIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorId").value(3));
    }

    @Test
    @WithUserDetails("lisi")
    void userCannotEditAnotherUsersArticle() throws Exception {
        mockMvc.perform(put("/api/articles/5")
                        .with(csrf())
                        .param("categoryId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"forged\",\"content\":\"forged\",\"tagIds\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithUserDetails("wangwu")
    void ownerCanReadOwnPendingArticle() throws Exception {
        setArticleStatus(5, Article.ArticleStatus.pending);

        mockMvc.perform(get("/api/articles/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    @Transactional
    @WithUserDetails("lisi")
    void anotherUserCannotReadPendingArticle() throws Exception {
        setArticleStatus(5, Article.ArticleStatus.pending);

        mockMvc.perform(get("/api/articles/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithUserDetails("lisi")
    void regularUserCannotPublishPendingArticle() throws Exception {
        setArticleStatus(5, Article.ArticleStatus.pending);

        mockMvc.perform(patch("/api/articles/5/publish").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithUserDetails("admin")
    void adminCanPublishPendingArticle() throws Exception {
        setArticleStatus(5, Article.ArticleStatus.pending);

        mockMvc.perform(patch("/api/admin/articles/5/publish").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("published"));
    }

    @Test
    @Transactional
    @WithUserDetails("wangwu")
    void bannedUserMutationInvalidatesExistingPrincipal() throws Exception {
        User user = userRepository.findByUsername("wangwu").orElseThrow(AssertionError::new);
        user.setStatus(User.UserStatus.banned);
        userRepository.saveAndFlush(user);

        mockMvc.perform(put("/api/users/4")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"blocked\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUploadIsRejectedAsUnauthenticated() throws Exception {
        mockMvc.perform(multipart("/api/files/upload/avatar")
                        .file("file", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private void setArticleStatus(int articleId, Article.ArticleStatus status) {
        Article article = articleRepository.findById(articleId).orElseThrow(AssertionError::new);
        article.setStatus(status);
        articleRepository.saveAndFlush(article);
    }
}
