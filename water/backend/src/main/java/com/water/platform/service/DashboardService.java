package com.water.platform.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.BaseResponse;
import com.water.platform.base.common.ResultUtils;
import com.water.platform.constant.UserRole;
import com.water.platform.mapper.*;
import com.water.platform.model.dto.resp.*;
import com.water.platform.model.entity.*;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WaterDataMapper waterDataMapper;
    @Autowired
    private PondMapper pondMapper;
    @Autowired
    private SeedRecordMapper seedRecordMapper;
    @Autowired
    private FeedRecordMapper feedRecordMapper;
    @Autowired
    private MedicineRecordMapper medicineRecordMapper;
    @Autowired
    private HarvestRecordMapper harvestRecordMapper;
    @Autowired
    private CompanyCertMapper companyCertMapper;
    @Autowired
    private AnnouncementMapper announcementMapper;
    @Autowired
    private ManagerFarmerService managerFarmerService;

    public BaseResponse<AdminDashboardResp> adminDashboard() {
        long userTotal = userMapper.selectCount(new LambdaQueryWrapper<>());
        long farmerCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserRole, UserRole.FARMERS));
        long managerCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserRole, UserRole.MANAGER));
        long adminCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserRole, UserRole.ADMIN));
        long onChainCount = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getIsOnChain, true));
        long certOnChainCount = companyCertMapper.selectCount(new LambdaQueryWrapper<CompanyCert>().eq(CompanyCert::getStatus, 1));
        long announcementCount = announcementMapper.selectCount(new LambdaQueryWrapper<>());
        long pendingCertCount = companyCertMapper.selectCount(new LambdaQueryWrapper<CompanyCert>().eq(CompanyCert::getStatus, 0));

        AdminDashboardResp resp = new AdminDashboardResp(
                userTotal, farmerCount, managerCount, adminCount,
                onChainCount, certOnChainCount, announcementCount, pendingCertCount
        );
        return ResultUtils.success(resp);
    }

    public BaseResponse<ManagerDashboardResp> managerDashboard() {
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        if (farmerIds.isEmpty()) {
            ManagerDashboardResp empty = new ManagerDashboardResp(
                    0L, 0L, 0L,
                    0L, 0L, 0.0, new ArrayList<>(),
                    0L, 0L, 0L, new LinkedHashMap<>(),
                    0L, 0L, 0L, 0L
            );
            return ResultUtils.success(empty);
        }

        long pondCount = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds));
        long pondActiveCount = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds).eq(Pond::getStatus, 1));

        long waterDataTotal = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds));
        long waterAbnormalCount = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds).eq(WaterData::getStatus, "异常"));
        double waterAbnormalRate = waterDataTotal > 0 ? NumberUtil.round(waterAbnormalCount * 100.0 / waterDataTotal, 2).doubleValue() : 0.0;

        List<WaterTrendItem> waterTrend = buildWaterTrend(farmerIds);

        long pondPending = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds).eq(Pond::getAuditStatus, 0));
        long pondApproved = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds).eq(Pond::getAuditStatus, 1));
        long pondRejected = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds).eq(Pond::getAuditStatus, 2));

        long seedPending = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().in(SeedRecord::getUserId, farmerIds).eq(SeedRecord::getAuditStatus, 0));
        long seedApproved = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().in(SeedRecord::getUserId, farmerIds).eq(SeedRecord::getAuditStatus, 1));
        long seedRejected = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().in(SeedRecord::getUserId, farmerIds).eq(SeedRecord::getAuditStatus, 2));

        long feedPending = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().in(FeedRecord::getUserId, farmerIds).eq(FeedRecord::getAuditStatus, 0));
        long feedApproved = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().in(FeedRecord::getUserId, farmerIds).eq(FeedRecord::getAuditStatus, 1));
        long feedRejected = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().in(FeedRecord::getUserId, farmerIds).eq(FeedRecord::getAuditStatus, 2));

        long medicinePending = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().in(MedicineRecord::getUserId, farmerIds).eq(MedicineRecord::getAuditStatus, 0));
        long medicineApproved = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().in(MedicineRecord::getUserId, farmerIds).eq(MedicineRecord::getAuditStatus, 1));
        long medicineRejected = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().in(MedicineRecord::getUserId, farmerIds).eq(MedicineRecord::getAuditStatus, 2));

        long harvestPending = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().in(HarvestRecord::getUserId, farmerIds).eq(HarvestRecord::getAuditStatus, 0));
        long harvestApproved = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().in(HarvestRecord::getUserId, farmerIds).eq(HarvestRecord::getAuditStatus, 1));
        long harvestRejected = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().in(HarvestRecord::getUserId, farmerIds).eq(HarvestRecord::getAuditStatus, 2));

        long waterPending = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds).eq(WaterData::getAuditStatus, 0));
        long waterApproved = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds).eq(WaterData::getAuditStatus, 1));
        long waterRejected = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds).eq(WaterData::getAuditStatus, 2));

        long pendingAuditCount = pondPending + seedPending + feedPending + medicinePending + harvestPending + waterPending;
        long approvedCount = pondApproved + seedApproved + feedApproved + medicineApproved + harvestApproved + waterApproved;
        long rejectedCount = pondRejected + seedRejected + feedRejected + medicineRejected + harvestRejected + waterRejected;

        Map<String, AuditTypeStat> auditByType = new LinkedHashMap<>();
        auditByType.put("pond", AuditTypeStat.of(pondPending, pondApproved, pondRejected));
        auditByType.put("seed", AuditTypeStat.of(seedPending, seedApproved, seedRejected));
        auditByType.put("feed", AuditTypeStat.of(feedPending, feedApproved, feedRejected));
        auditByType.put("medicine", AuditTypeStat.of(medicinePending, medicineApproved, medicineRejected));
        auditByType.put("harvest", AuditTypeStat.of(harvestPending, harvestApproved, harvestRejected));
        auditByType.put("water", AuditTypeStat.of(waterPending, waterApproved, waterRejected));

        long seedRecordCount = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().in(SeedRecord::getUserId, farmerIds));
        long feedRecordCount = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().in(FeedRecord::getUserId, farmerIds));
        long medicineRecordCount = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().in(MedicineRecord::getUserId, farmerIds));
        long harvestRecordCount = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().in(HarvestRecord::getUserId, farmerIds));

        ManagerDashboardResp resp = new ManagerDashboardResp(
                (long) farmerIds.size(), pondCount, pondActiveCount,
                waterDataTotal, waterAbnormalCount, waterAbnormalRate, waterTrend,
                pendingAuditCount, approvedCount, rejectedCount, auditByType,
                seedRecordCount, feedRecordCount, medicineRecordCount, harvestRecordCount
        );
        return ResultUtils.success(resp);
    }

    public BaseResponse<FarmerDashboardResp> farmerDashboard() {
        Long userId = TokenUtil.getLoginUserId();

        long pondCount = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().eq(Pond::getUserId, userId));
        long pondActiveCount = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().eq(Pond::getUserId, userId).eq(Pond::getStatus, 1));

        long waterDataTotal = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getUserId, userId));
        long waterAbnormalCount = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getUserId, userId).eq(WaterData::getStatus, "异常"));
        double waterAbnormalRate = waterDataTotal > 0 ? NumberUtil.round(waterAbnormalCount * 100.0 / waterDataTotal, 2).doubleValue() : 0.0;

        List<WaterTrendItem> waterTrend = buildWaterTrend(Collections.singletonList(userId));

        long pondPending = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().eq(Pond::getUserId, userId).eq(Pond::getAuditStatus, 0));
        long pondApproved = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().eq(Pond::getUserId, userId).eq(Pond::getAuditStatus, 1));
        long pondRejected = pondMapper.selectCount(new LambdaQueryWrapper<Pond>().eq(Pond::getUserId, userId).eq(Pond::getAuditStatus, 2));

        long seedPending = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().eq(SeedRecord::getUserId, userId).eq(SeedRecord::getAuditStatus, 0));
        long seedApproved = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().eq(SeedRecord::getUserId, userId).eq(SeedRecord::getAuditStatus, 1));
        long seedRejected = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().eq(SeedRecord::getUserId, userId).eq(SeedRecord::getAuditStatus, 2));

        long feedPending = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().eq(FeedRecord::getUserId, userId).eq(FeedRecord::getAuditStatus, 0));
        long feedApproved = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().eq(FeedRecord::getUserId, userId).eq(FeedRecord::getAuditStatus, 1));
        long feedRejected = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().eq(FeedRecord::getUserId, userId).eq(FeedRecord::getAuditStatus, 2));

        long medicinePending = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().eq(MedicineRecord::getUserId, userId).eq(MedicineRecord::getAuditStatus, 0));
        long medicineApproved = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().eq(MedicineRecord::getUserId, userId).eq(MedicineRecord::getAuditStatus, 1));
        long medicineRejected = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().eq(MedicineRecord::getUserId, userId).eq(MedicineRecord::getAuditStatus, 2));

        long harvestPending = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().eq(HarvestRecord::getUserId, userId).eq(HarvestRecord::getAuditStatus, 0));
        long harvestApproved = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().eq(HarvestRecord::getUserId, userId).eq(HarvestRecord::getAuditStatus, 1));
        long harvestRejected = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().eq(HarvestRecord::getUserId, userId).eq(HarvestRecord::getAuditStatus, 2));

        long waterPending = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getUserId, userId).eq(WaterData::getAuditStatus, 0));
        long waterApproved = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getUserId, userId).eq(WaterData::getAuditStatus, 1));
        long waterRejected = waterDataMapper.selectCount(new LambdaQueryWrapper<WaterData>().eq(WaterData::getUserId, userId).eq(WaterData::getAuditStatus, 2));

        long pendingAuditCount = pondPending + seedPending + feedPending + medicinePending + harvestPending + waterPending;
        long approvedCount = pondApproved + seedApproved + feedApproved + medicineApproved + harvestApproved + waterApproved;
        long rejectedCount = pondRejected + seedRejected + feedRejected + medicineRejected + harvestRejected + waterRejected;

        long seedRecordCount = seedRecordMapper.selectCount(new LambdaQueryWrapper<SeedRecord>().eq(SeedRecord::getUserId, userId));
        long feedRecordCount = feedRecordMapper.selectCount(new LambdaQueryWrapper<FeedRecord>().eq(FeedRecord::getUserId, userId));
        long medicineRecordCount = medicineRecordMapper.selectCount(new LambdaQueryWrapper<MedicineRecord>().eq(MedicineRecord::getUserId, userId));
        long harvestRecordCount = harvestRecordMapper.selectCount(new LambdaQueryWrapper<HarvestRecord>().eq(HarvestRecord::getUserId, userId));

        FarmerDashboardResp resp = new FarmerDashboardResp(
                pondCount, pondActiveCount,
                waterDataTotal, waterAbnormalCount, waterAbnormalRate, waterTrend,
                pendingAuditCount, approvedCount, rejectedCount,
                seedRecordCount, feedRecordCount, medicineRecordCount, harvestRecordCount
        );
        return ResultUtils.success(resp);
    }

    private List<WaterTrendItem> buildWaterTrend(List<Long> userIds) {
        Date sevenDaysAgo = DateUtil.offsetDay(new Date(), -7);
        List<WaterData> recentData = waterDataMapper.selectList(new LambdaQueryWrapper<WaterData>()
                .in(WaterData::getUserId, userIds)
                .ge(WaterData::getTime, sevenDaysAgo));

        Map<String, List<WaterData>> grouped = recentData.stream()
                .collect(Collectors.groupingBy(d -> DateUtil.format(d.getTime(), "yyyy-MM-dd")));

        List<WaterTrendItem> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = DateUtil.format(DateUtil.offsetDay(new Date(), -i), "yyyy-MM-dd");
            List<WaterData> dayData = grouped.getOrDefault(date, Collections.emptyList());
            int total = dayData.size();
            int abnormal = (int) dayData.stream().filter(d -> "异常".equals(d.getStatus())).count();
            trend.add(new WaterTrendItem(date, total, abnormal));
        }
        return trend;
    }
}
