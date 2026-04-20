package com.water.platform.chain.call;

import com.water.platform.chain.bo.*;
import com.water.platform.utils.IOUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

@Service
@NoArgsConstructor
@Data
public class WaterServiceCall {
  public static final String ABI = IOUtil.readResourceAsString("abi/Water.abi");

  public static final String BINARY = IOUtil.readResourceAsString("bin/ecc/Water.bin");

//  public static final String SM_BINARY = utils.com.water.platform.IOUtil.readResourceAsString("bin/sm/Water.bin");

  @Value("${water.contract.waterAddress:}")
  private String address;

  @Autowired(required = false)
  private Client client;

  @Autowired
  private com.water.platform.chain.config.SystemConfig systemConfig;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    if (client != null && systemConfig.isEnabled()) {
      this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().getCryptoKeyPair());
    }
  }

  public CallResponse manager() throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "manager", Arrays.asList());
  }

  public CallResponse companyCertByUserId(WaterCompanyCertByUserIdInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "companyCertByUserId", input.toArgs());
  }

  public TransactionResponse uploadCompanyCert(WaterUploadCompanyCertInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "uploadCompanyCert", input.toArgs());
  }

  public CallResponse waterDataByUserId(WaterWaterDataByUserIdInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "waterDataByUserId", input.toArgs());
  }

  public TransactionResponse insertWaterData(WaterInsertWaterDataInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "insertWaterData", input.toArgs());
  }

  public CallResponse getWaterDataByUserIdAndType(WaterGetWaterDataByUserIdAndTypeInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "getWaterDataByUserIdAndType", input.toArgs());
  }

  public CallResponse getCompanyCertByUserId(WaterGetCompanyCertByUserIdInputBO input) throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "getCompanyCertByUserId", input.toArgs());
  }

  public CallResponse company() throws Exception {
    if (txProcessor == null) throw new RuntimeException("区块链功能已禁用");
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "company", Arrays.asList());
  }
}
