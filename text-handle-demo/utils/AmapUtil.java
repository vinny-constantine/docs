package com.dover.util;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author dover
 * @since 2021/8/19
 */
public class AmapUtil {

    public static final String KEY = "10174822b15c6a22d4f02171ece9a93e";
    public static final String GEO_API = "https://restapi.amap.com/v3/geocode/geo";
    public static final String DISTANCE_API = "https://restapi.amap.com/v3/distance";
    public static final String REGEO_API = "https://restapi.amap.com/v3/geocode/regeo";
    public static final String DISTRICT_API = "https://restapi.amap.com/v3/config/district";
    public static final Double EARTH_RADIUS = 6371004D;
    public static final Double PI_RATE = Math.PI / 180;
    public static final String CENTER_NAME = "市政府";

    @Test
    public void testCalcDistance() {
        //116.481028,39.989643|114.481028,39.989643|115.481028,39.989643
        List<AmapPoint> originPointList = new ArrayList<>();
        originPointList.add(new AmapPoint("116.481028", "39.989643"));
        originPointList.add(new AmapPoint("114.481028", "39.989643"));
        originPointList.add(new AmapPoint("115.481028", "39.989643"));
        final StringJoiner adDoverointJoiner = new StringJoiner("|");
        for (AmapPoint point : originPointList) {
            adDoverointJoiner.add(point.toString());
        }
        AmapPoint destinationPoint = new AmapPoint("114.465302", "40.004717");
        AmapDistancePageResp amapDistancePageResp = calcDistance(destinationPoint, originPointList);
        System.out.println(amapDistancePageResp);
        System.out.println(calcDistance(destinationPoint, originPointList.get(0)));
        System.out.println(calcDistance(destinationPoint, originPointList.get(1)));
        System.out.println(calcDistance(destinationPoint, originPointList.get(2)));
    }

    @Test
    public void testQueryByGeo() {
        System.out.println(queryByGeo(new AmapPoint("115.481028", "39.989643")));
    }


    /**
     * 根据行政区划编码查询坐标等信息
     *
     * @param code 行政区划编码
     * @return 坐标等信息
     */
    public static AmapGeoResp queryGeoByDistrictCode(String code) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("key", DoverProperty.get(Constant.AMAP_API_KEY, KEY));
        params.put("address", CENTER_NAME);
        params.put("city", code);
        String resp = DoverHttpClientUtils.get(GEO_API + Constant.QUESTION_MARK + params.entrySet()
            .stream()
            .map(x -> x.getKey() + Constant.EQUAL + x.getValue())
            .collect(Collectors.joining(Constant.AND)));
        DoverLog.info("amap return:{}", resp);
        AmapGeoResp amapGeoResp = JSON.parseObject(resp, AmapGeoResp.class);
        ValidateUtil.check(Constant.ZERO.equals(amapGeoResp.getStatus()), CommonResultCode.FAILED_TO_INVOKE_AMAP_API);
        return amapGeoResp;
    }

    /**
     * 根据行政区划名称查询行政区
     *
     * @param name 行政区划名称
     * @return 行政区
     */
    public static AmapDistrictResp queryDistrictByName(String name) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("key", DoverProperty.get(Constant.AMAP_API_KEY, KEY));
        params.put("keywords", name);
        params.put("subdistrict", Constant.ZERO);
        String resp = DoverHttpClientUtils.get(DISTRICT_API + Constant.QUESTION_MARK + params.entrySet()
            .stream()
            .map(x -> x.getKey() + Constant.EQUAL + x.getValue())
            .collect(Collectors.joining(Constant.AND)));
        DoverLog.info("amap return:{}", resp);
        AmapDistrictResp amapDistrictResp = JSON.parseObject(resp, AmapDistrictResp.class);
        ValidateUtil.check(Constant.ZERO.equals(amapDistrictResp.getStatus()),
            CommonResultCode.FAILED_TO_INVOKE_AMAP_API);
        return amapDistrictResp;
    }

    /**
     * 根据目标坐标查询目标所在省市区县
     *
     * @param amapPoint 坐标点
     * @return 省市区县信息
     */
    public static AmapRegeoResp queryByGeo(AmapPoint amapPoint) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("key", DoverProperty.get(Constant.AMAP_API_KEY, KEY));
        params.put("location", amapPoint.toString());
        String resp = DoverHttpClientUtils.get(REGEO_API + Constant.QUESTION_MARK + params.entrySet()
            .stream()
            .map(x -> x.getKey() + Constant.EQUAL + x.getValue())
            .collect(Collectors.joining(Constant.AND)));
        DoverLog.info("amap return:{}", resp);
        AmapRegeoResp amapRegeoResp = JSON.parseObject(resp, AmapRegeoResp.class);
        ValidateUtil.check(Constant.ZERO.equals(amapRegeoResp.getStatus()), CommonResultCode.FAILED_TO_INVOKE_AMAP_API);
        return amapRegeoResp;
    }


    /**
     * 根据经纬度计算距离（使用高德API）
     *
     * @param target    目标坐标
     * @param pointList 待计算距离坐标
     * @return 距离列表
     */
    public static AmapDistancePageResp calcDistance(AmapPoint target, List<AmapPoint> pointList) {
        StringJoiner adDoverointJoiner = new StringJoiner("|");
        for (AmapPoint point : pointList) {
            adDoverointJoiner.add(point.toString());
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("key", DoverProperty.get(Constant.AMAP_API_KEY, KEY));
        params.put("origins", adDoverointJoiner.toString());
        params.put("destination", target.toString());
        params.put("type", "0");
//        String sign = URLEncoder.encode(DigestUtils.md5Hex(params.toString() + SECRET), StandardCharsets.UTF_8.name());
//        params.put("sig", sign);
//        params.add("sig=" + sign);
        String s = DoverHttpClientUtils.get(DISTANCE_API + Constant.QUESTION_MARK + params.entrySet()
            .stream()
            .map(x -> x.getKey() + Constant.EQUAL + x.getValue())
            .collect(Collectors.joining(Constant.AND)));
        AmapDistancePageResp amapDistancePageResp = JSON.parseObject(s, AmapDistancePageResp.class);
        DoverLog.info("amap return:{}", s);
        ValidateUtil.check(Constant.ZERO.equals(amapDistancePageResp.getStatus()),
            CommonResultCode.FAILED_TO_INVOKE_AMAP_API);
        return amapDistancePageResp;
    }


    /**
     * 根据经纬度计算距离（不使用高德API）
     *
     * @param target 目标经纬度
     * @param origin 源经纬度
     * @return 距离
     */
    public static Double calcDistance(AmapPoint target, AmapPoint origin) {
        double targetLat = Double.parseDouble(target.getLatitude()) * PI_RATE;
        double targetLng = Double.parseDouble(target.getLongitude()) * PI_RATE;
        double originLat = Double.parseDouble(origin.getLatitude()) * PI_RATE;
        double originLng = Double.parseDouble(origin.getLongitude()) * PI_RATE;
        return EARTH_RADIUS * Math.acos(
            Math.sin(targetLat) * Math.sin(originLat) + Math.cos(targetLat) * Math.cos(originLat) * Math.cos(
                targetLng - originLng));

    }
}
