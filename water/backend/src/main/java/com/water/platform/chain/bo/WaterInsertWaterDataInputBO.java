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
public class WaterInsertWaterDataInputBO {
  private BigInteger id;

  private BigInteger userId;

  private BigInteger dataType;

  private String data;

  private String status;

  private BigInteger time;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(id);
    args.add(userId);
    args.add(dataType);
    args.add(data);
    args.add(status);
    args.add(time);
    return args;
  }
}
