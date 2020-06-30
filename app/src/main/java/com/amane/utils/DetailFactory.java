package com.amane.utils;

import android.content.Context;

import com.amane.bean.SearchDetail;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生成<code>SearchDetail</code>的工厂类
 * @author Amane Hayashi
 */
public final class DetailFactory {

    private DetailFactory(){}

    /**
     * PoiItem转Detail
     * @param context 当前的Activity
     * @param poiItem 待转换的PoiItem
     * @param myLatLng 我的地理坐标
     * @return SearchDetail
     */
    public static SearchDetail fromPoi(Context context, PoiItem poiItem, LatLng myLatLng){
        return fromPoiList(context, Collections.singletonList(poiItem), myLatLng).get(0);
    }

    /**
     * PoiItem列表转Detail列表
     * @param context 当前的Activity
     * @param poiItemList 待转换的PoiItem列表
     * @param myLatLng 我的地理坐标
     * @return SearchDetail
     */
    public static List<SearchDetail> fromPoiList(Context context, List<PoiItem> poiItemList, LatLng myLatLng){
        List<SearchDetail> searchDetailList = new ArrayList<>();
        for(PoiItem poiItem : poiItemList){
            SearchDetail searchDetail = new SearchDetail();
            String typeDes = poiItem.getTypeDes(), name = poiItem.getTitle();
            LatLonPoint latLonPoint = poiItem.getLatLonPoint();
            Float distance = MapUtils.getLineDistance(latLonPoint, myLatLng);
            searchDetail.setAddress(poiItem.getSnippet());
            searchDetail.setPoint(MapUtils.latLonPointToLatLng(poiItem.getLatLonPoint()));
            searchDetail.setTitle(name);
            searchDetail.setType(PresentUtils.getEasyPoiType(typeDes, name));
            searchDetail.setDistance(PresentUtils.getEasyDistance(context, distance));
            searchDetailList.add(searchDetail);
        }
        return searchDetailList;
    }

    /**
     * BusLineItem列表转Detail列表
     * <br>此处是根据Detail各成员在Detail卡片中出现的位置进行转换的，实际上：
     * <br><code>distance</code>放置的是<code>起点站 - 终点站</code>
     * <br><code>address</code>放置的是<code>最低票价 ~ 最高票价</code>
     * <br><code>type</code>放置的是<code>距您最近的车站</code>
     * <br><code>point</code>对应着线路起点站的坐标
     * @param context 当前的Activity
     * @param busLineItemList BusLineItem列表
     * @param myLatLng 我的地理坐标
     * @return SearchDetail
     */
    public static List<SearchDetail> fromBusLine(Context context, List<BusLineItem> busLineItemList, LatLng myLatLng){
        List<SearchDetail> searchDetailList = new ArrayList<>();
        for(BusLineItem busLineItem : busLineItemList){
            SearchDetail searchDetail = new SearchDetail();
            String busLineName = PresentUtils.getEasyBusLineName(busLineItem.getBusLineName());
            Object[] objects = PresentUtils.getEasyNearestStation(context, busLineItem.getBusStations(), myLatLng);
            BusStationItem nearestStation = (BusStationItem) objects[0];
            searchDetail.setDistance(busLineItem.getOriginatingStation() + " - " + busLineItem.getTerminalStation());
            searchDetail.setTitle(busLineName);
            searchDetail.setPoint(MapUtils.latLonPointToLatLng(nearestStation.getLatLonPoint()));
            searchDetail.setAddress(busLineItem.getBasicPrice() + " ~ " + busLineItem.getTotalPrice() + "元");
            searchDetail.setType(objects[1].toString());
            searchDetail.setOverlay(busLineItem);
            searchDetailList.add(searchDetail);
        }
        return searchDetailList;
    }

    /**
     * BusStationItem列表转Detail列表
     * @param context 当前的Activity
     * @param busStationItemList BusStationItem列表
     * @param myLatLng 我的地理坐标
     * @return SearchDetail
     */
    public static List<SearchDetail> fromBusStation(Context context, List<BusStationItem> busStationItemList, LatLng myLatLng){
        List<SearchDetail> searchDetailList = new ArrayList<>();
        for(BusStationItem busStationItem : busStationItemList){
            SearchDetail searchDetail = new SearchDetail();
            LatLonPoint latLonPoint = busStationItem.getLatLonPoint();
            Float distance = MapUtils.getLineDistance(latLonPoint, myLatLng);
            searchDetail.setAddress(PresentUtils.getEasyBusLines(busStationItem.getBusLineItems()));
            searchDetail.setPoint(MapUtils.latLonPointToLatLng(latLonPoint));
            searchDetail.setDistance(PresentUtils.getEasyDistance(context, distance));
            searchDetail.setTitle(busStationItem.getBusStationName());
            searchDetail.setType("");
            searchDetailList.add(searchDetail);
        }
        return searchDetailList;
    }
}
