package com.amane.bean;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果类，出现在搜索列表中
 * @author Amane Hayashi
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class SearchItem implements Serializable {

    // ID(POI ID)
    private String id;

    // 示意图
    private int imageResId;

    // 名称
    private String name;

    // 详情
    private String detail;

    // 类型
    private int type;

    // 地理坐标(格式：[latitude,longitude])
    private String point;

}
