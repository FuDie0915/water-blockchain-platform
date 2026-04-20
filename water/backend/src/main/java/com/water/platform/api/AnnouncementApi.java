package com.water.platform.api;

import com.water.platform.annotation.AuthCheck;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.constant.UserRole;
import com.water.platform.model.dto.req.AnnouncementPublishReq;
import com.water.platform.model.entity.Announcement;
import com.water.platform.service.AnnouncementService;
import com.water.platform.utils.TokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcement")
@Slf4j
@Api(tags = "公告管理模块")
public class AnnouncementApi {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/admin/publish")
    @ApiOperation("管理员发布公告")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> publish(@RequestBody @Validated AnnouncementPublishReq req) {
        return announcementService.publish(req);
    }

    @GetMapping("/admin/list")
    @ApiOperation("管理员查看所有公告")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<List<Announcement>> adminList() {
        return announcementService.listAll();
    }

    @PostMapping("/admin/delete/{id}")
    @ApiOperation("管理员删除公告")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> delete(@PathVariable("id") Long id) {
        return announcementService.delete(id);
    }

    @GetMapping("/list")
    @ApiOperation("用户根据角色查看公告")
    public BaseResponse<List<Announcement>> listByRole() {
        String role = TokenUtil.getLoginUser().getUserRole();
        return announcementService.listByRole(role);
    }

    @PostMapping("/admin/upChain")
    @ApiOperation("管理员上链系统公告")
    @AuthCheck(roleType = UserRole.ADMIN)
    public BaseResponse<Boolean> upChain(@RequestParam Long id) {
        return announcementService.upChain(id);
    }
}
