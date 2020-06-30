package com.amane.utils;

import android.graphics.Point;
import android.location.Location;

import com.amane.tianyin.R;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 地图工具类
 * @author Amane Hayashi
 */

public class MapUtils {

    private MapUtils(){}

    /**
     * 屏幕坐标转地理坐标
     * @param aMap 地图
     * @param point 屏幕坐标
     * @return LatLng(地理坐标)
     */
    public static LatLng screenToGeoLocation(AMap aMap, Point point){
        return Objects.requireNonNull(aMap.getProjection().fromScreenLocation(point));
    }

    /**
     * 地理坐标转地理坐标点
     * @param latLng 地理坐标
     * @return LatLonPoint(地理坐标点)
     */
    public static LatLonPoint latLngToLatLonPoint(LatLng latLng){
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    /**
     * 地理坐标点转地理坐标
     * @param latLonPoint 地理坐标点
     * @return LatLng(地理坐标)
     */
    public static LatLng latLonPointToLatLng(LatLonPoint latLonPoint){
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude(), false);
    }

    /**
     * 地理坐标点集合转地理坐标列表
     * @param latLonPoints 地理坐标点集合
     * @return List(地理坐标列表)
     */
    public static List<LatLng> latLonPointsToLatLngList(Collection<LatLonPoint> latLonPoints) {
        return latLonPoints.stream().map(MapUtils::latLonPointToLatLng).collect(Collectors.toList());
    }

    /**
     * 位置转地理坐标
     * @param location 位置
     * @return LatLng(地理坐标)
     */
    public static LatLng locationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude(), false);
    }

    /**
     * 字符串转地理坐标
     * @param s 字符串
     * @return 地理坐标
     */
    private static LatLonPoint stringToLatLonPoint(String s){
        try {
            String[] arr = s.split(",");
            return new LatLonPoint(Double.valueOf(arr[0]), Double.valueOf(arr[1]));
        } catch (NumberFormatException e){
            throw new RuntimeException("invalid method");
        }
    }

    public static LatLng stringToLatLng(String s){
        return latLonPointToLatLng(stringToLatLonPoint(s));
    }

    /**
     * 添加单一的地图标记(用于标注单一的POI或地理位置)
     * @param aMap 地图
     * @param latLng 标记的地理坐标
     */
    public static void addSingleMapMarker(AMap aMap, LatLng latLng){
        addMarker(aMap, latLng, BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_pressed_big));
    }

    /**
     * 按序添加多个地图标记(用于标注多个POI或地理位置)
     * @param aMap 地图
     * @param latLng 标记的地理坐标
     * @param order 次序
     */
    public static void addSeriesMapMarker(AMap aMap, LatLng latLng, int order){
        int imgResId = BaseUtils.getDrawableId("poi_marker_" + order);
        addMarker(aMap, latLng, BitmapDescriptorFactory.fromResource(imgResId));
    }

    /**
     * 添加地图标记
     * @param aMap 地图
     * @param latLng 标记的地理坐标
     * @param bitmapDescriptor 标记的样式图
     */
    public static void addMarker(AMap aMap, LatLng latLng, BitmapDescriptor bitmapDescriptor){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).icon(bitmapDescriptor);
        aMap.addMarker(markerOptions);
    }

    /**
     * 获得两点之间的直线距离
     * @param latLngX 地理坐标
     * @param latLngY 地理坐标
     * @return Float
     */
    public static Float getLineDistance(LatLng latLngX, LatLng latLngY){
        return AMapUtils.calculateLineDistance(latLngX, latLngY);
    }

    /**
     * 获得两点之间的直线距离
     * @param latLonX 地理坐标点
     * @param latLngY 地理坐标
     * @return Float
     */
    static Float getLineDistance(LatLonPoint latLonX, LatLng latLngY){
        LatLng latLngX = latLonPointToLatLng(latLonX);
        return getLineDistance(latLngX, latLngY);
    }
}
