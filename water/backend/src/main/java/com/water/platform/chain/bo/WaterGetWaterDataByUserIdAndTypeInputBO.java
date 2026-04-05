package com.water.platform.chain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterGetWaterDataByUserIdAndTypeInputBO {
  private BigInteger userId;

  private BigInteger dataType;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(userId);
    args.add(dataType);
    return args;
  }
}
