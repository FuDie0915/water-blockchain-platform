package com.water.platform.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：devon
 * @date ：2024/10/20 12:49
 * @description：功能描述
 * @version: 1.0
 */
public class ChainPageUtil {
    public static <T> List<T> getPaginationList(List<T> list, int pageNum, int pageSize) {
        // 检查页码和每页大小的有效性
        if (pageNum < 1 || pageSize < 1 || list == null || list.isEmpty()) {
            return new ArrayList<>(); // 返回空列表
        }
        int startIndex = (pageNum - 1) * pageSize;
        if (startIndex >= list.size()) {
            return new ArrayList<>(); // 返回空列表
        }
        int endIndex = Math.min(startIndex + pageSize, list.size());
        // 使用subList方法来获取分页后的列表
        return new ArrayList<>(list.subList(startIndex, endIndex));
    }
}
