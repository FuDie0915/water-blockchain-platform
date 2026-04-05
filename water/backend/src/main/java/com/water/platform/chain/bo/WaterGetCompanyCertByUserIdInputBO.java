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
public class WaterGetCompanyCertByUserIdInputBO {
  private BigInteger userId;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(userId);
    return args;
  }
}
