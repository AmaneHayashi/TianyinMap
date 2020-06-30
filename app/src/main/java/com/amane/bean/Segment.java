package com.amane.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路径轨迹类，用于记录每一步路径的方向与简介
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class Segment {

    // 方向
    private String action;

    // 简介
    private String instruction;
}
