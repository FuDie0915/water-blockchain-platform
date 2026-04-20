package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.WarningThresholdMapper;
import com.water.platform.model.dto.req.WarningThresholdCreateReq;
import com.water.platform.model.dto.req.WarningThresholdSaveReq;
import com.water.platform.model.entity.WarningThreshold;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarningThresholdService {

    @Autowired
    private WarningThresholdMapper warningThresholdMapper;

    /**
     * 新建预警阈值配置（每个监管局只能创建一次）
     */
    public BaseResponse<Boolean> createThreshold(WarningThresholdCreateReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        WarningThreshold existing = warningThresholdMapper.selectOne(new LambdaQueryWrapper<WarningThreshold>()
                .eq(WarningThreshold::getManagerId, managerId));
        ThrowUtils.throwIf(existing != null, ErrorCode.PARAMS_ERROR, "您已设置预警阈值，请使用修改接口");
        WarningThreshold threshold = new WarningThreshold();
        BeanUtils.copyProperties(req, threshold);
        threshold.setManagerId(managerId);
        threshold.setCreateTime(DateUtil.date());
        threshold.setUpdateTime(DateUtil.date());
        warningThresholdMapper.insert(threshold);
        return ResultUtils.success(Boolean.TRUE);
    }

    /**
     * 修改预警阈值配置（只更新传入的非null字段）
     */
    public BaseResponse<Boolean> updateThreshold(WarningThresholdSaveReq req) {
        Long managerId = TokenUtil.getLoginUserId();
        WarningThreshold existing = warningThresholdMapper.selectOne(new LambdaQueryWrapper<WarningThreshold>()
                .eq(WarningThreshold::getManagerId, managerId));
        ThrowUtils.throwIf(existing == null, ErrorCode.PARAMS_ERROR, "预警阈值不存在，请先使用新建接口创建");
        if (req.getWaterTempMin() != null) existing.setWaterTempMin(req.getWaterTempMin());
        if (req.getWaterTempMax() != null) existing.setWaterTempMax(req.getWaterTempMax());
        if (req.getSalinityMin() != null) existing.setSalinityMin(req.getSalinityMin());
        if (req.getSalinityMax() != null) existing.setSalinityMax(req.getSalinityMax());
        if (req.getPhMin() != null) existing.setPhMin(req.getPhMin());
        if (req.getPhMax() != null) existing.setPhMax(req.getPhMax());
        if (req.getDoMin() != null) existing.setDoMin(req.getDoMin());
        if (req.getDoMax() != null) existing.setDoMax(req.getDoMax());
        if (req.getNh3nMin() != null) existing.setNh3nMin(req.getNh3nMin());
        if (req.getNh3nMax() != null) existing.setNh3nMax(req.getNh3nMax());
        if (req.getNo2Min() != null) existing.setNo2Min(req.getNo2Min());
        if (req.getNo2Max() != null) existing.setNo2Max(req.getNo2Max());
        existing.setUpdateTime(DateUtil.date());
        warningThresholdMapper.updateById(existing);
        return ResultUtils.success(Boolean.TRUE);
    }

    public BaseResponse<WarningThreshold> getThreshold() {
        Long managerId = TokenUtil.getLoginUserId();
        WarningThreshold threshold = warningThresholdMapper.selectOne(new LambdaQueryWrapper<WarningThreshold>()
                .eq(WarningThreshold::getManagerId, managerId));
        return ResultUtils.success(threshold);
    }

    /**
     * 根据 dataType 和 managerId 检查数据值是否超出阈值范围
     */
    public boolean isAbnormal(Integer dataType, Double value, Long managerId) {
        if (managerId == null || value == null) {
            return false;
        }
        WarningThreshold threshold = warningThresholdMapper.selectOne(new LambdaQueryWrapper<WarningThreshold>()
                .eq(WarningThreshold::getManagerId, managerId));
        if (threshold == null) {
            return false;
        }
        Double min = null, max = null;
        switch (dataType) {
            case 2: min = threshold.getWaterTempMin(); max = threshold.getWaterTempMax(); break;
            case 3: min = threshold.getSalinityMin(); max = threshold.getSalinityMax(); break;
            case 4: min = threshold.getPhMin(); max = threshold.getPhMax(); break;
            case 5: min = threshold.getDoMin(); max = threshold.getDoMax(); break;
            case 6: min = threshold.getNh3nMin(); max = threshold.getNh3nMax(); break;
            case 7: min = threshold.getNo2Min(); max = threshold.getNo2Max(); break;
            default: return false;
        }
        if (min == null || max == null) {
            return false;
        }
        return value < min || value > max;
    }
}
