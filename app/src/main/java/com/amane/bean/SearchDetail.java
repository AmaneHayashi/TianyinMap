package com.amane.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.busline.BusLineItem;

import java.io.Serializable;

import lombok.Data;

/**
 * 搜索详情类，出现在详情卡片中
 * @author Amane Hayashi
 */

@Data
public class SearchDetail implements Serializable {

    // 标题
    private String title;

    // (与我的)距离
    private String distance;

    // 类型
    private String type;

    // 地址
    private String address;

    // 坐标
    private LatLng point;

    // Overlay
    private BusLineItem overlay;
}
