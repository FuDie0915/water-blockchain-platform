package com.water.platform.api;

import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.PageResponse;
import com.water.platform.model.entity.Notification;
import com.water.platform.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Slf4j
@Api(tags = "消息通知模块")
public class NotificationApi {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    @ApiOperation("分页查询我的通知列表")
    public BaseResponse<PageResponse<Notification>> listMy(
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return notificationService.listMy(isRead, pageNum, pageSize);
    }

    @GetMapping("/unreadCount")
    @ApiOperation("获取未读通知数量")
    public BaseResponse<Long> unreadCount() {
        return notificationService.unreadCount();
    }

    @PostMapping("/read/{id}")
    @ApiOperation("标记单条通知已读")
    public BaseResponse<Boolean> markRead(@PathVariable("id") Long id) {
        return notificationService.markRead(id);
    }

    @PostMapping("/readAll")
    @ApiOperation("全部标记已读")
    public BaseResponse<Boolean> markAllRead() {
        return notificationService.markAllRead();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除通知")
    public BaseResponse<Boolean> delete(@PathVariable("id") Long id) {
        return notificationService.delete(id);
    }
}
