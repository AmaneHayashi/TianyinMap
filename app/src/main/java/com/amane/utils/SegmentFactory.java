package com.amane.utils;

import com.amane.bean.Segment;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.WalkStep;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成<code>Segment</code>的工厂类
 * @author Amane Hayashi
 */
public class SegmentFactory {

    private SegmentFactory(){}

    /**
     * DriveStep转Segment
     * @param driveStepList 待转换的DriveStep列表
     * @return List
     */
    public static List<Segment> fromDriveSteps(List<DriveStep> driveStepList){
        return driveStepList.stream().map(d -> new Segment(d.getAction(), d.getInstruction())).collect(Collectors.toList());
    }

    /**
     * WalkStep转Segment
     * @param walkStepList 待转换的WalkStep列表
     * @return List
     */
    public static List<Segment> fromWalkSteps(List<WalkStep> walkStepList){
        return walkStepList.stream().map(w -> new Segment(w.getAction(), w.getInstruction())).collect(Collectors.toList());
    }
}