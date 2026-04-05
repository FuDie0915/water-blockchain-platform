package com.water.platform.chain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterCtorBO {
  private String companyAddress;

  private String managerAddress;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(companyAddress);
    args.add(managerAddress);
    return args;
  }
}
