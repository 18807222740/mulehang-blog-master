package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.dto.CommentUpdateDTO;
import com.mulehang.blog.dto.NotificationDTO;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogComment;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.enums.CommentStatusEnum;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCommentMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.metrics.BlogMetrics;
import com.mulehang.blog.mq.producer.CommentNotifyProducer;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.security.SensitiveWordService;
import com.mulehang.blog.service.CommentService;
import com.mulehang.blog.service.WebSocketNotificationService;
import com.mulehang.blog.util.IpRegionService;
import com.mulehang.blog.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论 Service 实现。
 *
 * @author mulehang
 * @since 2026-01-17
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogArticleMapper articleMapper;
    private final CommentNotifyProducer commentNotifyProducer;
    private final SensitiveWordService sensitiveWordService;
    private final IpRegionService ipRegionService;
    private final BlogMetrics blogMetrics;
    private final WebSocketNotificationService wsNotificationService;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserMapper userMapper;

    /**
     * 构造函数。
     *
     * @param commentMapper         评论 Mapper
     * @param articleMapper         文章 Mapper
     * @param commentNotifyProducer 评论通知 Producer
     * @param sensitiveWordService  敏感词服务
     * @param ipRegionService       IP 归属地服务
     * @param blogMetrics           业务指标
     * @param wsNotificationService WebSocket 通知服务
     * @param redissonClient        Redisson 客户端
     * @param redisTemplate         Redis 操作模板
     * @param userMapper            用户 Mapper
     */
    public CommentServiceImpl(BlogCommentMapper commentMapper,
            BlogArticleMapper articleMapper,
            CommentNotifyProducer commentNotifyProducer,
            SensitiveWordService sensitiveWordService,
            IpRegionService ipRegionService,
            BlogMetrics blogMetrics,
            WebSocketNotificationService wsNotificationService,
            RedissonClient redissonClient,
            RedisTemplate<String, Object> redisTemplate,
            SysUserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.commentNotifyProducer = commentNotifyProducer;
        this.sensitiveWordService = sensitiveWordService;
        this.ipRegionService = ipRegionService;
        this.blogMetrics = blogMetrics;
        this.wsNotificationService = wsNotificationService;
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
    }

    /**
     * 发表评论/回复。
     *
     * @param dto       评论创建 DTO
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @return 评论 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CommentCreateDTO dto, String ipAddress, String userAgent) {
        if (dto == null) {
            throw BusinessException.badRequest("dto 为空");
        }
        if (dto.getArticleId() == null) {
            throw BusinessException.badRequest("articleId 为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw BusinessException.badRequest("content 为空");
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }

        BlogArticle article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw BusinessException.notFound("文章未找到: " + dto.getArticleId());
        }

        String content = dto.getContent().trim();
        if (sensitiveWordService.contains(content)) {
            content = sensitiveWordService.replace(content);
        }

        BlogComment comment = new BlogComment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(normalizeId(dto.getParentId()));
        comment.setReplyToUser(dto.getReplyToUser());
        comment.setContent(content);
        comment.setStatus(CommentStatusEnum.APPROVED.getCode());
        comment.setLikeCount(0);
        comment.setIsTop(0);
        comment.setUserId(userId);
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);
        comment.setLocation(resolveLocation(ipAddress));

        if (!Objects.equals(comment.getParentId(), 0L)) {
            BlogComment parent = commentMapper.selectById(comment.getParentId());
            if (parent == null || !Objects.equals(parent.getArticleId(), dto.getArticleId())) {
                throw BusinessException.badRequest("父评论不存在或不属于当前文章: parentId=" + comment.getParentId());
            }
            if (parent.getRootId() != null && parent.getRootId() != 0L) {
                comment.setRootId(parent.getRootId());
            } else {
                comment.setRootId(parent.getId());
            }
        } else {
            comment.setRootId(0L);
        }

        commentMapper.insert(comment);
        Long commentId = comment.getId();
        if (commentId == null) {
            throw BusinessException.internalError("评论创建失败：未返回 id");
        }

        articleMapper.incrementCommentCount(dto.getArticleId());

        blogMetrics.incrementComment();

        // 发送 MQ 异步邮件通知
        commentNotifyProducer.sendNotify(dto.getArticleId(), commentId);

        // 发送 WebSocket 实时通知给文章作者
        sendWebSocketNotification(article, comment);

        return commentId;
    }

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<CommentVO> listByArticle(Long articleId, Long pageNo, Long pageSize) {
        if (articleId == null) {
            throw BusinessException.badRequest("articleId 为空");
        }
        long pn = pageNo == null ? 1L : pageNo;
        long ps = pageSize == null ? 10L : pageSize;

        Page<BlogComment> page = new Page<>(pn, ps);
        Page<BlogComment> result = commentMapper.selectPage(page, new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getArticleId, articleId)
                .in(BlogComment::getStatus,
                        CommentStatusEnum.APPROVED.getCode(),
                        CommentStatusEnum.DELETED.getCode())
                .orderByDesc(BlogComment::getIsTop)
                .orderByDesc(BlogComment::getCreateTime));

        Long currentUserId = UserContext.getCurrentUserId();
        List<CommentVO> list = result.getRecords().stream()
                .map(comment -> toVO(comment, currentUserId))
                .toList();
        PageResult<CommentVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPageNo(pn);
        pageResult.setPageSize(ps);
        pageResult.setTotal(result.getTotal());
        return pageResult;
    }

    /**
     * 按用户分页查询评论列表。
     *
     * @param userId   用户 ID
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<CommentVO> listByUser(Long userId, Long pageNo, Long pageSize) {
        if (userId == null) {
            throw BusinessException.badRequest("userId 为空");
        }
        long pn = pageNo == null ? 1L : pageNo;
        long ps = pageSize == null ? 10L : pageSize;

        Page<BlogComment> page = new Page<>(pn, ps);
        Page<BlogComment> result = commentMapper.selectPage(page, new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getUserId, userId)
                .in(BlogComment::getStatus,
                        CommentStatusEnum.APPROVED.getCode(),
                        CommentStatusEnum.DELETED.getCode())
                .orderByDesc(BlogComment::getCreateTime));

        List<BlogComment> records = result.getRecords();
        Map<Long, BlogArticle> articleMap = loadArticleMap(records);

        List<CommentVO> list = records.stream()
                .map(comment -> {
                    CommentVO vo = toVO(comment, userId);
                    BlogArticle article = articleMap.get(comment.getArticleId());
                    if (article != null) {
                        vo.setArticleTitle(article.getTitle());
                        vo.setArticleSlug(article.getSlug());
                    }
                    return vo;
                })
                .toList();

        PageResult<CommentVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPageNo(pn);
        pageResult.setPageSize(ps);
        pageResult.setTotal(result.getTotal());
        return pageResult;
    }

    /**
     * 点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=点赞成功；false=已点赞或未获取到锁
     */
    @Override
    public boolean likeComment(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            throw BusinessException.badRequest("参数 userId/commentId 不能为空");
        }

        String lockKey = RedisKeys.LOCK_COMMENT_LIKE_PREFIX + commentId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String likeKey = RedisKeys.COMMENT_LIKE_SET_PREFIX + commentId;
                    Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
                    if (Boolean.TRUE.equals(hasLiked)) {
                        return false;
                    }

                    redisTemplate.opsForSet().add(likeKey, userId.toString());
                    commentMapper.incrementLikeCount(commentId);
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * 查询用户是否已点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=已点赞；false=未点赞
     */
    @Override
    public boolean hasLiked(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            throw BusinessException.badRequest("参数 userId/commentId 不能为空");
        }

        String likeKey = RedisKeys.COMMENT_LIKE_SET_PREFIX + commentId;
        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 取消点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=取消成功；false=未点赞或未获取到锁
     */
    @Override
    public boolean unlikeComment(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            throw BusinessException.badRequest("参数 userId/commentId 不能为空");
        }

        String lockKey = RedisKeys.LOCK_COMMENT_LIKE_PREFIX + commentId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String likeKey = RedisKeys.COMMENT_LIKE_SET_PREFIX + commentId;
                    Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
                    if (!Boolean.TRUE.equals(hasLiked)) {
                        return false;
                    }

                    redisTemplate.opsForSet().remove(likeKey, userId.toString());
                    commentMapper.decrementLikeCount(commentId);
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * 编辑评论内容。
     *
     * @param commentId 评论 ID
     * @param dto       评论更新 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long commentId, CommentUpdateDTO dto) {
        if (commentId == null) {
            throw BusinessException.badRequest("commentId 为空");
        }
        if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
            throw BusinessException.badRequest("content 为空");
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }

        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw BusinessException.notFound("评论未找到: " + commentId);
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw BusinessException.forbidden("无权限编辑该评论");
        }
        if (Objects.equals(comment.getStatus(), CommentStatusEnum.DELETED.getCode())) {
            throw BusinessException.badRequest("评论已删除，无法编辑");
        }

        String content = dto.getContent().trim();
        if (sensitiveWordService.contains(content)) {
            content = sensitiveWordService.replace(content);
        }
        comment.setContent(content);
        commentMapper.updateById(comment);
    }

    /**
     * 删除评论。
     *
     * @param commentId 评论 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId) {
        if (commentId == null) {
            throw BusinessException.badRequest("commentId 为空");
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }

        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw BusinessException.notFound("评论未找到: " + commentId);
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw BusinessException.forbidden("无权限删除该评论");
        }
        if (Objects.equals(comment.getStatus(), CommentStatusEnum.DELETED.getCode())) {
            return;
        }

        comment.setStatus(CommentStatusEnum.DELETED.getCode());
        comment.setContent("该评论已删除");
        comment.setLikeCount(0);
        commentMapper.updateById(comment);

        articleMapper.decrementCommentCount(comment.getArticleId());
    }

    /**
     * 将评论实体转换为 VO。
     *
     * @param comment       评论实体
     * @param currentUserId 当前用户 ID（可为空）
     * @return 评论 VO
     */
    private CommentVO toVO(BlogComment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setRootId(comment.getRootId());
        vo.setParentId(comment.getParentId());
        vo.setUserId(comment.getUserId());
        vo.setReplyToUser(comment.getReplyToUser());
        if (Objects.equals(comment.getStatus(), CommentStatusEnum.DELETED.getCode())) {
            vo.setContent("该评论已删除");
        } else {
            vo.setContent(comment.getContent());
        }
        vo.setLikeCount(comment.getLikeCount());
        vo.setStatus(comment.getStatus());
        vo.setLocation(comment.getLocation());
        vo.setIsTop(comment.getIsTop());
        vo.setCreateTime(comment.getCreateTime());
        if (currentUserId != null) {
            String likeKey = RedisKeys.COMMENT_LIKE_SET_PREFIX + comment.getId();
            Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId.toString());
            vo.setLiked(Boolean.TRUE.equals(hasLiked));
        } else {
            vo.setLiked(false);
        }

        // 查询并填充用户信息
        if (comment.getUserId() != null) {
            SysUser user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }

        return vo;
    }

    /**
     * 批量加载评论关联的文章信息。
     *
     * @param comments 评论列表
     * @return 文章映射
     */
    private Map<Long, BlogArticle> loadArticleMap(List<BlogComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> articleIds = comments.stream()
                .map(BlogComment::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                        .in(BlogArticle::getId, articleIds))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BlogArticle::getId, article -> article));
    }

    /**
     * 解析 IP 归属地。
     *
     * @param ipAddress IP 地址
     * @return 归属地字符串（可能为 null）
     */
    private String resolveLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }
        return ipRegionService.getShortRegion(ipAddress);
    }

    /**
     * 规范化 ID（null -> 0）。
     *
     * @param id 原始 ID
     * @return 规范化后的 ID
     */
    private Long normalizeId(Long id) {
        return id == null ? 0L : id;
    }

    /**
     * 发送 WebSocket 实时通知给文章作者
     *
     * @param article 文章实体
     * @param comment 评论实体
     */
    private void sendWebSocketNotification(BlogArticle article, BlogComment comment) {
        try {
            // 如果评论者是文章作者自己，不需要通知
            if (Objects.equals(comment.getUserId(), article.getAuthorId())) {
                return;
            }

            // 构造通知消息
            NotificationDTO notification = NotificationDTO.builder()
                    .type("COMMENT")
                    .title("新评论通知")
                    .content(comment.getContent())
                    .articleId(article.getId())
                    .articleTitle(article.getTitle())
                    .commentId(comment.getId())
                    .senderId(comment.getUserId())
                    .receiverId(article.getAuthorId())
                    .url("/articles/" + article.getSlug())
                    .timestamp(LocalDateTime.now())
                    .read(false)
                    .build();

            // 发送通知
            wsNotificationService.sendToUser(article.getAuthorId(), notification);
            log.debug("已发送 WebSocket 通知给文章作者: authorId={}, commentId={}",
                    article.getAuthorId(), comment.getId());
        } catch (Exception e) {
            // WebSocket 通知失败不影响主流程
            log.error("WebSocket 通知发送失败", e);
        }
    }
}
