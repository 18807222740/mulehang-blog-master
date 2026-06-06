package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.GitHubAccessTokenDTO;
import com.mulehang.blog.dto.GitHubEmailDTO;
import com.mulehang.blog.dto.GitHubUserInfoDTO;
import com.mulehang.blog.entity.SysRole;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.entity.SysUserRole;
import com.mulehang.blog.mapper.SysRoleMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import com.mulehang.blog.service.GitHubOAuthService;
import com.mulehang.blog.util.JwtUtil;
import com.mulehang.blog.vo.LoginResponse;
import com.mulehang.blog.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * GitHub OAuth 服务实现类
 * 
 * @author mulehang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOAuthServiceImpl implements GitHubOAuthService {
    
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // GitHub OAuth 配置（从 application.yml 注入）
    @Value("${github.oauth.client-id}")
    private String clientId;
    
    @Value("${github.oauth.client-secret}")
    private String clientSecret;
    
    @Value("${github.oauth.redirect-uri}")
    private String redirectUri;
    
    @Value("${github.oauth.authorize-url}")
    private String authorizeUrl;
    
    @Value("${github.oauth.access-token-url}")
    private String accessTokenUrl;
    
    @Value("${github.oauth.user-info-url}")
    private String userInfoUrl;
    
    /**
     * 获取 GitHub OAuth 授权 URL
     */
    @Override
    public String getAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("scope", "user:email")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .toUriString();
    }
    
    /**
     * 通过授权码获取 GitHub Access Token
     */
    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<GitHubAccessTokenDTO> response = restTemplate.postForEntity(
                    accessTokenUrl,
                    request,
                    GitHubAccessTokenDTO.class
            );
            
            GitHubAccessTokenDTO tokenDTO = response.getBody();
            if (tokenDTO == null) {
                throw new RuntimeException("获取 GitHub Access Token 失败");
            }
            if (tokenDTO.getAccessToken() == null) {
                String errorDetail = tokenDTO.getErrorDescription();
                if (errorDetail == null || errorDetail.isBlank()) {
                    errorDetail = tokenDTO.getError();
                }
                if (errorDetail == null || errorDetail.isBlank()) {
                    throw new RuntimeException("获取 GitHub Access Token 失败");
                }
                throw new RuntimeException("获取 GitHub Access Token 失败: " + errorDetail);
            }
            
            return tokenDTO.getAccessToken();
        } catch (Exception e) {
            log.error("获取 GitHub Access Token 失败", e);
            throw new RuntimeException("获取 GitHub Access Token 失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过 Access Token 获取 GitHub 用户信息
     */
    @Override
    public GitHubUserInfoDTO getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<GitHubUserInfoDTO> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    GitHubUserInfoDTO.class
            );
            
            GitHubUserInfoDTO userInfo = response.getBody();
            if (userInfo == null) {
                throw new RuntimeException("获取 GitHub 用户信息失败");
            }
            
            // 如果 GitHub API 没有返回邮箱，尝试从邮箱 API 获取
            if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
                try {
                    String emailsUrl = "https://api.github.com/user/emails";
                    ResponseEntity<GitHubEmailDTO[]> emailsResponse = restTemplate.exchange(
                            emailsUrl,
                            HttpMethod.GET,
                            request,
                            GitHubEmailDTO[].class
                    );
                    
                    GitHubEmailDTO[] emails = emailsResponse.getBody();
                    if (emails != null && emails.length > 0) {
                        // 优先使用主邮箱
                        for (GitHubEmailDTO email : emails) {
                            if (email.isPrimary() && email.isVerified()) {
                                userInfo.setEmail(email.getEmail());
                                break;
                            }
                        }
                        // 如果没有主邮箱，使用第一个已验证的邮箱
                        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
                            for (GitHubEmailDTO email : emails) {
                                if (email.isVerified()) {
                                    userInfo.setEmail(email.getEmail());
                                    break;
                                }
                            }
                        }
                    }
                    log.info("从 GitHub emails API 获取到邮箱: {}", userInfo.getEmail());
                } catch (Exception e) {
                    log.warn("获取 GitHub 邮箱失败: {}", e.getMessage());
                }
            }
            
            return userInfo;
        } catch (Exception e) {
            log.error("获取 GitHub 用户信息失败", e);
            throw new RuntimeException("获取 GitHub 用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * GitHub OAuth 登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(String code) {
        // 1. 获取 Access Token
        String accessToken = getAccessToken(code);
        
        // 2. 获取 GitHub 用户信息
        GitHubUserInfoDTO githubUser = getUserInfo(accessToken);
        
        // 3. 优先通过 GitHub ID 查询用户，其次通过 email（如果有）
        SysUser user = null;
        
        // 先尝试通过 username (GitHub login) 查询
        user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, githubUser.getLogin())
        );
        
        // 如果找不到且 email 不为空，再尝试通过 email 查询
        if (user == null && githubUser.getEmail() != null && !githubUser.getEmail().isEmpty()) {
            user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, githubUser.getEmail())
            );
        }
        
        if (user == null) {
            // 创建新用户
            user = new SysUser();
            user.setUsername(githubUser.getLogin());
            user.setNickname(githubUser.getName() != null ? githubUser.getName() : githubUser.getLogin());
            user.setEmail(githubUser.getEmail()); // 可能为 null
            user.setAvatar(githubUser.getAvatarUrl());
            user.setProfile(githubUser.getBio());
            user.setStatus(1); // 启用状态
            user.setRegisterIp(""); // OAuth 登录暂不记录 IP
            user.setLastLoginTime(LocalDateTime.now());
            
            // GitHub OAuth 登录不需要密码，生成随机密码占位
            user.setPasswordSalt(UUID.randomUUID().toString());
            user.setPasswordHash(UUID.randomUUID().toString());
            
            try {
                userMapper.insert(user);
                log.info("GitHub OAuth 登录：创建新用户 {}", user.getUsername());
            } catch (Exception e) {
                // 如果插入失败（用户名已存在），再次查询
                log.warn("创建用户失败，尝试查询现有用户: {}", e.getMessage());
                user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, githubUser.getLogin())
                );
                if (user == null) {
                    throw new RuntimeException("用户创建失败");
                }
            }
        } else {
            // 更新现有用户信息
            user.setAvatar(githubUser.getAvatarUrl());
            user.setProfile(githubUser.getBio());
            user.setLastLoginTime(LocalDateTime.now());
            
            // 如果原来没有 email，现在有了，则尝试更新（需要检查邮箱是否被占用）
            if ((user.getEmail() == null || user.getEmail().isEmpty()) 
                && githubUser.getEmail() != null && !githubUser.getEmail().isEmpty()) {
                // 检查邮箱是否已被其他用户占用
                SysUser existingEmailUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, githubUser.getEmail())
                        .ne(SysUser::getId, user.getId())
                );
                
                if (existingEmailUser == null) {
                    // 邮箱未被占用，可以更新
                    user.setEmail(githubUser.getEmail());
                } else {
                    log.warn("GitHub OAuth 登录：邮箱 {} 已被其他用户占用，跳过邮箱更新", githubUser.getEmail());
                }
            }
            
            userMapper.updateById(user);
            
            log.info("GitHub OAuth 登录：更新用户 {}", user.getUsername());
        }

        // 确保 GitHub 登录用户拥有 USER 角色
        ensureDefaultUserRole(user);
        
        // 4. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 5. 查询用户角色
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        
        // 6. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
        
        // 7. 构建响应
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .profile(user.getProfile())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .build();
        
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userInfo(userInfo)
                .build();
    }

    /**
     * 确保用户拥有默认 USER 角色
     *
     * @param user 当前用户
     */
    private void ensureDefaultUserRole(SysUser user) {
        if (user == null || user.getId() == null) {
            return;
        }

        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());
        if (roleCodes.contains("USER")) {
            return;
        }

        SysRole userRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, "USER")
                .eq(SysRole::getIsDeleted, 0)
        );
        if (userRole == null) {
            log.warn("GitHub OAuth 登录：未找到 USER 角色，无法为用户 {} 分配默认角色", user.getUsername());
            return;
        }

        SysUserRole userRoleLink = new SysUserRole();
        userRoleLink.setUserId(user.getId());
        userRoleLink.setRoleId(userRole.getId());
        userRoleMapper.insert(userRoleLink);
        log.info("GitHub OAuth 登录：为用户 {} 分配默认 USER 角色", user.getUsername());
    }
}
