package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.AnnouncementMapper;
import com.water.platform.model.dto.req.AnnouncementPublishReq;
import com.water.platform.model.entity.Announcement;
import com.water.platform.service.ChainStoreService;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChainStoreService chainStoreService;

    public BaseResponse<Boolean> publish(AnnouncementPublishReq req) {
        Long publisherId = TokenUtil.getLoginUserId();
        Announcement announcement = new Announcement();
        announcement.setTitle(req.getTitle());
        announcement.setContent(req.getContent());
        announcement.setTargetRole(req.getTargetRole());
        announcement.setPublisherId(publisherId);
        announcement.setCreateTime(DateUtil.date());
        announcementMapper.insert(announcement);
        if ("all".equals(req.getTargetRole())) {
            notificationService.sendByRole("farmers", "系统公告：" + req.getTitle(), req.getContent(), "ANNOUNCEMENT");
            notificationService.sendByRole("manager", "系统公告：" + req.getTitle(), req.getContent(), "ANNOUNCEMENT");
        } else {
            notificationService.sendByRole(req.getTargetRole(), "系统公告：" + req.getTitle(), req.getContent(), "ANNOUNCEMENT");
        }
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<List<Announcement>> listAll() {
        List<Announcement> list = announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getId));
        return ResultUtils.success(list);
    }

    public BaseResponse<Boolean> delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        ThrowUtils.throwIf(announcement == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        ThrowUtils.throwIf(announcement.getChainTxHash() != null, ErrorCode.OPERATION_ERROR, "该公告已上链，不能删除");
        announcementMapper.deleteById(id);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<List<Announcement>> listByRole(String role) {
        List<Announcement> list = announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .in(Announcement::getTargetRole, role, "all")
                .orderByDesc(Announcement::getId));
        return ResultUtils.success(list);
    }

    @com.water.platform.annotation.Lock(lockClass = org.fisco.bcos.sdk.client.Client.class)
    public BaseResponse<Boolean> upChain(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        ThrowUtils.throwIf(announcement == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        String hash = chainStoreService.upChain("announcement", announcement.getId(), announcement);
        announcement.setChainTxHash(hash);
        announcementMapper.updateById(announcement);
        return ResultUtils.success(Boolean.TRUE);
    }
}
