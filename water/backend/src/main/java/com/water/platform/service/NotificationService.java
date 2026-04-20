package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.PageResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.NotificationMapper;
import com.water.platform.mapper.UserMapper;
import com.water.platform.model.entity.Notification;
import com.water.platform.model.entity.User;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发送单条通知
     */
    public void send(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(DateUtil.date());
        notificationMapper.insert(notification);
    }

    /**
     * 发送单条通知（无关联ID）
     */
    public void send(Long userId, String title, String content, String type) {
        send(userId, title, content, type, null);
    }

    /**
     * 批量发送通知
     */
    public void sendToUsers(List<Long> userIds, String title, String content, String type) {
        for (Long userId : userIds) {
            send(userId, title, content, type);
        }
    }

    /**
     * 按角色群发通知
     */
    public void sendByRole(String role, String title, String content, String type) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserRole, role)
                        .eq(User::getUserStatus, 1));
        for (User user : users) {
            send(user.getUserId(), title, content, type);
        }
    }

    /**
     * 分页查询当前用户通知列表
     */
    public BaseResponse<PageResponse<Notification>> listMy(Integer isRead, Long pageNum, Long pageSize) {
        Long userId = TokenUtil.getLoginUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getId);
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResultUtils.success(new PageResponse<>(0, page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 获取未读数量
     */
    public BaseResponse<Long> unreadCount() {
        Long userId = TokenUtil.getLoginUserId();
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return ResultUtils.success(count);
    }

    /**
     * 标记单条已读
     */
    public BaseResponse<Boolean> markRead(Long id) {
        Long userId = TokenUtil.getLoginUserId();
        Notification notification = notificationMapper.selectOne(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 全部标记已读
     */
    public BaseResponse<Boolean> markAllRead() {
        Long userId = TokenUtil.getLoginUserId();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 删除通知
     */
    public BaseResponse<Boolean> delete(Long id) {
        Long userId = TokenUtil.getLoginUserId();
        Notification notification = notificationMapper.selectOne(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
        ThrowUtils.throwIf(notification == null, ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        notificationMapper.deleteById(id);
        return ResultUtils.success(Boolean.TRUE);
    }
}
