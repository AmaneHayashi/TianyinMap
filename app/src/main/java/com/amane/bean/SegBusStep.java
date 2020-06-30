package com.amane.bean;

import com.amap.api.services.route.BusStep;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 整理后的公交路段类，添加了了对路段类型的判断
 * @author Amap
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class SegBusStep extends BusStep {

	// 是否为步行
	private boolean isWalk = false;
	// 是否为公交
	private boolean isBus = false;
	// 是否为铁路
	private boolean isRailway = false;
	// 是否为出租
	private boolean isTaxi = false;
	// 是否为起点
	private boolean isStart = false;
	// 是否为终点
	private boolean isEnd = false;

	public SegBusStep(BusStep step) {
		if (step != null) {
			this.setBusLines(step.getBusLines());
			this.setWalk(step.getWalk());
			this.setRailway(step.getRailway());
			this.setTaxi(step.getTaxi());
		}
	}
}
