package com.amane.utils;

import android.content.Context;

import com.amane.tianyin.R;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteRailwayItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用于前端展示的处理工具类
 * @author Amane Hayashi & AMap
 */
public final class PresentUtils {

    // 公交线路显示的上限
    private final static long BUS_LINE_LIMIT = 3L;

    private final static Map<String, List<String>> AREA_MAP = new HashMap<String, List<String>>(){
        {
            put("省级地名", Arrays.asList("省", "自治区"));
            put("地市级地名", Collections.singletonList("地级市"));
            put("区县级地名", Arrays.asList("辖区", "县级市"));
            put("村庄级地名", Collections.singletonList("村庄"));
        }
    };

    private final static Map<String, String> AREA_CHILD_MAP = new HashMap<String, String>(){
        {
            put("直辖市", "北京市#天津市#重庆市#上海市");
            put("特别行政区", "香港特别行政区#澳门特别行政区");
        }
    };

    private PresentUtils(){}

    /**
     * 获得POI的类型名
     * <br>用于在显示POI资料卡片时，确定POI的类型
     * <br>高德地图中POI的类型字符串(<code>typeDes</code>)一般由三部分组成
     * <br>格式为：<code>[a;b;c]</code>
     * <br>从类型字符串的最短值、POI名称、行政区划级别名中选择一个合适的并返回
     * @param typeDes 类型字符串
     * @param name POI的名字
     * @return String
     */
    static String getEasyPoiType(String typeDes, String name){
        List<String> typeList = Arrays.asList(typeDes.split(";"));
        // 第一步处理：如果最短字符串为"热点地名"，则替换为"商圈"
        String typeName = BaseUtils.getShortestOrElse(typeList, "商圈", "热点地名");
        // 第二步处理：如果高德提供的类型名为null，直接返回POI名
        if(typeName == null){
            return name;
        }
        // 第三步处理：如果类型名为普通地名，代表为行政区划名，从行政区划表中选择合适的值
        if(typeName.equals("普通地名")){
            // 获得类型的c部分(行政区划级地名)
            String sType = typeList.get(2);
            // 查询c部分对应的值(行政区划级别)
            List<String> resList = AREA_MAP.getOrDefault(sType, Collections.singletonList(name));
            // 行政区划级别只有一种可能，直接返回
            if(Objects.requireNonNull(resList).size() == 1){
                return resList.get(0);
            }
            // POI名在直辖市/特别行政区中，直接返回
            for(Map.Entry<String, String> entry : AREA_CHILD_MAP.entrySet()){
                if(entry.getValue().contains(name)){
                    return entry.getKey();
                }
            }
            // 否则返回POI名中有与行政区划级别重叠名字的行政区划级别
            for(String s : resList){
                if(BaseUtils.anySubstring(name, s)){
                    return s;
                }
            }
        }
        return typeName;
    }

    /**
     * 获得逆地理编码后的地点地址
     * <br>地点名=城市名(xx省xx市，即前缀<code>prefix</code>)+街道名(xx区xx路xx号，即后缀<code>suffix</code>)<br>
     * @param regeocodeAddress 逆地理编码得到的地址
     * @return String
     */
    public static String getEasyRegeoAddress(RegeocodeAddress regeocodeAddress){
        StreetNumber streetNumber = regeocodeAddress.getStreetNumber();
        String province = regeocodeAddress.getProvince();
        String city = regeocodeAddress.getCity();
        String prefix, suffix = regeocodeAddress.getDistrict();
        // 此时为直辖市
        if(province.equals(city)){
            prefix = province;
        }
        else {
            prefix = province.concat(city);
        }
        if(streetNumber != null){
            String street = streetNumber.getStreet();
            String number = streetNumber.getNumber();
            String direction = streetNumber.getDirection();
            suffix = suffix.concat(street).concat(number).concat(direction);
        }
        // 此时没有具体的信息，直接使用默认的地址
        else {
            return regeocodeAddress.getFormatAddress();
        }
        return prefix.concat(suffix);
    }

    /**
     * 寻找公交站列表中距离我的位置最近的车站，并返回车站对象与指定格式字符串<br>
     * 格式举例：<code>距您最近的车站：西直门，920米</code>
     * @param context 当前的Activity
     * @param busStationItemList 公交站列表
     * @param myLatLng 我的位置
     * @return Object[] (长度为2，第一个为车站对象<code>BusStationItem</code>，第二个为返回的格式字符串<code>String</code>)
     */
    static Object[] getEasyNearestStation(Context context, List<BusStationItem> busStationItemList, LatLng myLatLng){
        float distance = Float.MAX_VALUE;
        BusStationItem b = new BusStationItem();
        for(BusStationItem busStationItem : busStationItemList){
            Float dist = MapUtils.getLineDistance(busStationItem.getLatLonPoint(), myLatLng);
            if(dist < distance){
                b = busStationItem;
                distance = dist;
            }
        }
        return new Object[]{b, "距您最近的车站：" + b.getBusStationName() + "，" + getEasyDistance(context, distance)};
    }

    /**
     * 获得处理后用于前端显示的距离字符串
     * @param context 当前的Activity
     * @param distance 距离值
     * @return String
     */
    public static String getEasyDistance(Context context, Float distance){
        String prefix, suffix;
        prefix = BaseUtils.getResourceString(context, R.string.poi_distance);
        if(distance >= 1000.0f){
            suffix = String.format(Locale.CHINA, "%.1f公里", distance / 1000);
        }
        else {
            suffix = String.format(Locale.CHINA, "%.0f米", distance);
        }
        return prefix.concat(suffix);
    }

    /**
     * 获得处理后用于前端显示的(公交站对应的)公交线路数
     * <br>一个公交站对应的线路较多时，显示出来的格式不一致，因此统一控制显示的公交线路格式
     * @param address 公交线路值(格式：<code>a;b;c;...</code>)
     * @return String
     */
    public static String getEasyBusLines(String address) {
        StringBuilder sb = new StringBuilder();
        String[] arr = address.split(";");
        if(arr.length > BUS_LINE_LIMIT){
            sb.append("等");
        }
        // 选择最短的车站名，最多不超过BUS_LINE_LIMIT
        sb.insert(0,  Arrays.stream(arr).sorted().limit(BUS_LINE_LIMIT).collect(Collectors.joining(",")));
        return sb.toString();
    }

    /**
     * 获得处理后用于前端显示的(公交站对应的)公交线路数
     * @param busLineItemList 公交线路列表
     * @return String
     */
    static String getEasyBusLines(List<BusLineItem> busLineItemList){
        StringBuilder sb = new StringBuilder();
        if(busLineItemList.size() > BUS_LINE_LIMIT){
            sb.append("等");
        }
        // 选择最短的车站名，最多不超过BUS_LINE_LIMIT
        sb.insert(0, busLineItemList .parallelStream()
                                            .map(BusLineItem::getBusLineName)
                                            .map(PresentUtils::getEasyBusLineName)
                                            .distinct()
                                            .sorted()
                                            .limit(BUS_LINE_LIMIT)
                                            .collect(Collectors.joining(",")));
        return sb.toString();
    }

    /**
     * 获得处理后用于前端显示的公交线路路径
     * @param busPath 公交路段集合
     * @return String
     */
    public static String getEasyBusPath(BusPath busPath) {
        if (busPath == null || busPath.getSteps() == null) {
            return "";
        }
        List<BusStep> busSteps = busPath.getSteps();
        StringBuilder sb = new StringBuilder();
        for (BusStep busStep : busSteps) {
            StringBuilder title = new StringBuilder();
            if (busStep.getBusLines().size() > 0) {
                for (RouteBusLineItem busLine : busStep.getBusLines()) {
                    if (busLine == null) {
                        continue;
                    }
                    String busLineName = getEasyBusLineName(busLine.getBusLineName());
                    title.append(busLineName);
                    title.append(" / ");
                }
                // 去掉最后一个" / "
                sb.append(title.substring(0, title.length() - 3));
                sb.append(" > ");
            }
            // 如果中间需要坐火车
            if (busStep.getRailway() != null) {
                RouteRailwayItem railway = busStep.getRailway();
                sb      .append(railway.getTrip())
                        .append("(")
                        .append(railway.getDeparturestop().getName())
                        .append(" - ")
                        .append(railway.getArrivalstop().getName())
                        .append(")")
                        .append(" > ");
            }
        }
        // 去掉最后一个" > "
        return sb.substring(0, sb.length() - 3);
    }

    /**
     * 获得处理后用于显示的公交路径描述
     * <br>格式：<code>xx小时yy分钟 | 距您a.b公里 | 需步行c.d公里</code>
     * @param context 当前的Activity
     * @param busPath 公交路段集合
     * @return String
     */
    public static String getEasyBusPathDes(Context context, BusPath busPath) {
        if (busPath == null) {
            return "";
        }
        long second = busPath.getDuration();
        String time = getEasyDuration((int) second);
        Float subDistance = busPath.getDistance();
        String subDis = getEasyDistance(context, subDistance);
        float walkDistance = busPath.getWalkDistance();
        String walkDis = getEasyDistance(context, walkDistance).substring(2);
        return time + " | " + subDis + " | 需步行" + walkDis;
    }

    /**
     * 获得处理后用于显示的公交线路名
     * 即只选取<code>busLineName</code>的括号前的值
     * @param busLineName 公交线路名
     * @return String
     */
    static String getEasyBusLineName(String busLineName) {
        if (busLineName == null) {
            return "";
        }
        return busLineName.replaceAll("\\(.*?\\)", "");
    }

    /**
     * 获得处理后用于显示的使用时间
     * 格式：<code>XX小时XX分钟XX秒</code>
     * @param duration 总时长(秒数)
     * @return String
     */
    private static String getEasyDuration(long duration) {
        int second = (int) duration;
        if (second > 3600) {
            int hour = second / 3600;
            int minute = (second % 3600) / 60;
            return hour + "小时" + minute + "分钟";
        }
        if (second >= 60) {
            int minute = second / 60;
            return minute + "分钟";
        }
        return second + "秒";
    }

    /**
     * 获得处理后用于显示的路径信息(XX分钟(距您YY))
     * @param context 当前的Activity
     * @param duration 总时长
     * @param distance 总距离
     * @return String
     */
    public static String getEasyRouteInfo(Context context, long duration, float distance){
        return getEasyDuration(duration)+ "(" +  getEasyDistance(context, distance) + ")";
    }

    /**
     * 获得处理后用于显示的打车信息(打车约W元)
     * @param cost 费用
     * @return String
     */
    public static String getEasyCostInfo(float cost){
        return "打车约" + (int)cost + "元";
    }

    /**
     * 获得处理后用于显示的火车站信息(北京西站 15:00)
     * @param name 车站名
     * @param time 到站时间
     * @return String
     */
    public static String getEasyRailwayInfo(String name, String time){
        return name + "站 "+ time.substring(0, 2) + ":" + time.substring(2);
    }

    /**
     * 根据需要的驾驶方向选择对应的图片
     * @param actionName 弯道类型
     * @return int
     */
    public static int getDriveDrawableID(String actionName) {
        if (actionName == null || actionName.equals("")) {
            return R.drawable.dir3;
        }
        if ("左转".equals(actionName)) {
            return R.drawable.dir2;
        }
        if ("右转".equals(actionName)) {
            return R.drawable.dir1;
        }
        if ("向左前方行驶".equals(actionName) || "靠左".equals(actionName)) {
            return R.drawable.dir6;
        }
        if ("向右前方行驶".equals(actionName) || "靠右".equals(actionName)) {
            return R.drawable.dir5;
        }
        if ("向左后方行驶".equals(actionName) || "左转调头".equals(actionName)) {
            return R.drawable.dir7;
        }
        if ("向右后方行驶".equals(actionName)) {
            return R.drawable.dir8;
        }
        if ("直行".equals(actionName)) {
            return R.drawable.dir3;
        }
        if ("减速行驶".equals(actionName)) {
            return R.drawable.dir4;
        }
        return R.drawable.dir3;
    }

    /**
     * 根据需要的步行方向选择对应的图片
     * @param actionName 弯道类型
     * @return int
     */
    public static int getWalkDrawableID(String actionName) {
        if (actionName == null || actionName.equals("")) {
            return R.drawable.dir13;
        }
        if ("左转".equals(actionName)) {
            return R.drawable.dir2;
        }
        if ("右转".equals(actionName)) {
            return R.drawable.dir1;
        }
        if ("向左前方".equals(actionName) || "靠左".equals(actionName) || actionName.contains("向左前方")) {
            return R.drawable.dir6;
        }
        if ("向右前方".equals(actionName) || "靠右".equals(actionName) || actionName.contains("向右前方")) {
            return R.drawable.dir5;
        }
        if ("向左后方".equals(actionName)|| actionName.contains("向左后方")) {
            return R.drawable.dir7;
        }
        if ("向右后方".equals(actionName)|| actionName.contains("向右后方")) {
            return R.drawable.dir8;
        }
        if ("直行".equals(actionName)) {
            return R.drawable.dir3;
        }
        if ("通过人行横道".equals(actionName)) {
            return R.drawable.dir9;
        }
        if ("通过过街天桥".equals(actionName)) {
            return R.drawable.dir11;
        }
        if ("通过地下通道".equals(actionName)) {
            return R.drawable.dir10;
        }
        return R.drawable.dir13;
    }
}
