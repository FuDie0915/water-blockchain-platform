package com.water.platform.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import com.water.platform.mapper.*;
import com.water.platform.model.entity.*;
import com.water.platform.model.export.*;
import com.water.platform.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired private UserMapper userMapper;
    @Autowired private PondMapper pondMapper;
    @Autowired private WaterDataMapper waterDataMapper;
    @Autowired private SeedRecordMapper seedRecordMapper;
    @Autowired private FeedRecordMapper feedRecordMapper;
    @Autowired private MedicineRecordMapper medicineRecordMapper;
    @Autowired private HarvestRecordMapper harvestRecordMapper;
    @Autowired private ManagerFarmerService managerFarmerService;

    private static final Map<Integer, String> DATA_TYPE_MAP = new java.util.HashMap<>();
    private static final Map<Integer, String> AUDIT_STATUS_MAP = new java.util.HashMap<>();
    private static final Map<Integer, String> POND_STATUS_MAP = new java.util.HashMap<>();
    private static final Map<Integer, String> USER_STATUS_MAP = new java.util.HashMap<>();
    static {
        DATA_TYPE_MAP.put(0, "TDS"); DATA_TYPE_MAP.put(1, "浑浊度"); DATA_TYPE_MAP.put(2, "水温");
        DATA_TYPE_MAP.put(3, "盐度"); DATA_TYPE_MAP.put(4, "pH"); DATA_TYPE_MAP.put(5, "溶解氧");
        DATA_TYPE_MAP.put(6, "氨氮"); DATA_TYPE_MAP.put(7, "亚硝酸盐");
        AUDIT_STATUS_MAP.put(0, "待审核"); AUDIT_STATUS_MAP.put(1, "已通过"); AUDIT_STATUS_MAP.put(2, "已拒绝");
        POND_STATUS_MAP.put(0, "空闲"); POND_STATUS_MAP.put(1, "养殖中"); POND_STATUS_MAP.put(2, "休整");
        USER_STATUS_MAP.put(0, "禁用"); USER_STATUS_MAP.put(1, "启用");
    }

    private void setExcelHeaders(HttpServletResponse response, String fileName) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
    }

    /**
     * 管理员导出用户列表
     */
    public void exportUsers(HttpServletResponse response) throws Exception {
        setExcelHeaders(response, "用户列表");
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getUserId));
        List<UserExportModel> data = users.stream().map(u -> {
            UserExportModel m = new UserExportModel();
            m.setUserId(String.valueOf(u.getUserId()));
            m.setUserAccount(u.getUserAccount());
            m.setUserName(u.getUserName());
            m.setUserRole(u.getUserRole());
            m.setUserStatusText(USER_STATUS_MAP.getOrDefault(u.getUserStatus(), "未知"));
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), UserExportModel.class).sheet("用户列表").doWrite(data);
    }

    /**
     * 监管局导出养殖池
     */
    public void exportPonds(HttpServletResponse response, Long farmerId) throws Exception {
        setExcelHeaders(response, "养殖池列表");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<Pond> wrapper = new LambdaQueryWrapper<Pond>().in(Pond::getUserId, farmerIds).orderByDesc(Pond::getId);
        if (farmerId != null) wrapper.eq(Pond::getUserId, farmerId);
        List<Pond> ponds = pondMapper.selectList(wrapper);
        List<PondExportModel> data = ponds.stream().map(p -> {
            PondExportModel m = new PondExportModel();
            m.setId(p.getId()); m.setPondName(p.getPondName()); m.setBreedType(p.getBreedType());
            m.setArea(p.getArea()); m.setDepth(p.getDepth()); m.setUserId(p.getUserId());
            m.setStatusText(POND_STATUS_MAP.getOrDefault(p.getStatus(), "未知"));
            m.setCreateTime(p.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), PondExportModel.class).sheet("养殖池").doWrite(data);
    }

    /**
     * 监管局导出水质数据
     */
    public void exportWaterData(HttpServletResponse response, Long farmerId, Integer dataType) throws Exception {
        setExcelHeaders(response, "水质数据");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<WaterData> wrapper = new LambdaQueryWrapper<WaterData>().in(WaterData::getUserId, farmerIds).orderByDesc(WaterData::getId);
        if (farmerId != null) wrapper.eq(WaterData::getUserId, farmerId);
        if (dataType != null) wrapper.eq(WaterData::getDataType, dataType);
        List<WaterData> list = waterDataMapper.selectList(wrapper);
        List<WaterDataExportModel> data = list.stream().map(w -> {
            WaterDataExportModel m = new WaterDataExportModel();
            m.setId(w.getId()); m.setUserId(w.getUserId()); m.setData(w.getData());
            m.setStatus(w.getStatus()); m.setTime(w.getTime());
            m.setDataTypeText(DATA_TYPE_MAP.getOrDefault(w.getDataType(), "未知"));
            m.setOnChainText(w.getIsOnChain() ? "是" : "否");
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), WaterDataExportModel.class).sheet("水质数据").doWrite(data);
    }

    /**
     * 监管局导出苗种记录
     */
    public void exportSeedRecords(HttpServletResponse response, Long farmerId) throws Exception {
        setExcelHeaders(response, "苗种投放记录");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<SeedRecord> wrapper = new LambdaQueryWrapper<SeedRecord>().in(SeedRecord::getUserId, farmerIds).orderByDesc(SeedRecord::getId);
        if (farmerId != null) wrapper.eq(SeedRecord::getUserId, farmerId);
        List<SeedRecord> list = seedRecordMapper.selectList(wrapper);
        List<SeedExportModel> data = list.stream().map(r -> {
            SeedExportModel m = new SeedExportModel();
            m.setId(r.getId()); m.setPondId(r.getPondId()); m.setUserId(r.getUserId());
            m.setRecordDate(r.getRecordDate()); m.setSeedType(r.getSeedType());
            m.setSeedSpec(r.getSeedSpec()); m.setWeight(r.getWeight());
            m.setAuditStatusText(AUDIT_STATUS_MAP.getOrDefault(r.getAuditStatus(), "未知"));
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), SeedExportModel.class).sheet("苗种记录").doWrite(data);
    }

    /**
     * 监管局导出投喂记录
     */
    public void exportFeedRecords(HttpServletResponse response, Long farmerId) throws Exception {
        setExcelHeaders(response, "投喂记录");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<FeedRecord> wrapper = new LambdaQueryWrapper<FeedRecord>().in(FeedRecord::getUserId, farmerIds).orderByDesc(FeedRecord::getId);
        if (farmerId != null) wrapper.eq(FeedRecord::getUserId, farmerId);
        List<FeedRecord> list = feedRecordMapper.selectList(wrapper);
        List<FeedExportModel> data = list.stream().map(r -> {
            FeedExportModel m = new FeedExportModel();
            m.setId(r.getId()); m.setPondId(r.getPondId()); m.setUserId(r.getUserId());
            m.setFeedDate(r.getFeedDate()); m.setFeedBrand(r.getFeedBrand());
            m.setFeedAmount(r.getFeedAmount());
            m.setAuditStatusText(AUDIT_STATUS_MAP.getOrDefault(r.getAuditStatus(), "未知"));
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), FeedExportModel.class).sheet("投喂记录").doWrite(data);
    }

    /**
     * 监管局导出用药记录
     */
    public void exportMedicineRecords(HttpServletResponse response, Long farmerId) throws Exception {
        setExcelHeaders(response, "用药记录");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<MedicineRecord> wrapper = new LambdaQueryWrapper<MedicineRecord>().in(MedicineRecord::getUserId, farmerIds).orderByDesc(MedicineRecord::getId);
        if (farmerId != null) wrapper.eq(MedicineRecord::getUserId, farmerId);
        List<MedicineRecord> list = medicineRecordMapper.selectList(wrapper);
        List<MedicineExportModel> data = list.stream().map(r -> {
            MedicineExportModel m = new MedicineExportModel();
            m.setId(r.getId()); m.setPondId(r.getPondId()); m.setUserId(r.getUserId());
            m.setMedicineDate(r.getMedicineDate()); m.setMedicineName(r.getMedicineName());
            m.setPurpose(r.getPurpose()); m.setDosage(r.getDosage());
            m.setAuditStatusText(AUDIT_STATUS_MAP.getOrDefault(r.getAuditStatus(), "未知"));
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), MedicineExportModel.class).sheet("用药记录").doWrite(data);
    }

    /**
     * 监管局导出收获记录
     */
    public void exportHarvestRecords(HttpServletResponse response, Long farmerId) throws Exception {
        setExcelHeaders(response, "收获记录");
        Long managerId = TokenUtil.getLoginUserId();
        List<Long> farmerIds = managerFarmerService.getManagedFarmerIds(managerId);
        ThrowUtils.throwIf(farmerIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "您没有管辖的养殖户");
        LambdaQueryWrapper<HarvestRecord> wrapper = new LambdaQueryWrapper<HarvestRecord>().in(HarvestRecord::getUserId, farmerIds).orderByDesc(HarvestRecord::getId);
        if (farmerId != null) wrapper.eq(HarvestRecord::getUserId, farmerId);
        List<HarvestRecord> list = harvestRecordMapper.selectList(wrapper);
        List<HarvestExportModel> data = list.stream().map(r -> {
            HarvestExportModel m = new HarvestExportModel();
            m.setId(r.getId()); m.setPondId(r.getPondId()); m.setUserId(r.getUserId());
            m.setHarvestDate(r.getHarvestDate()); m.setBatchNo(r.getBatchNo());
            m.setSpec(r.getSpec()); m.setTotalWeight(r.getTotalWeight());
            m.setSurvivalRate(r.getSurvivalRate());
            m.setAuditStatusText(AUDIT_STATUS_MAP.getOrDefault(r.getAuditStatus(), "未知"));
            return m;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream(), HarvestExportModel.class).sheet("收获记录").doWrite(data);
    }
}
