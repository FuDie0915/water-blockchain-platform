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
public class WaterUploadCompanyCertInputBO {
  private BigInteger id;

  private BigInteger userId;

  private String imageUrl;

  private BigInteger createTime;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(id);
    args.add(userId);
    args.add(imageUrl);
    args.add(createTime);
    return args;
  }
}
