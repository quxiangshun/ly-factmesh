package com.ly.factmesh.iot.application.service;

import java.util.Map;

/**
 * 遥测数据清洗接口
 * 对上报的遥测数据进行校验、归一化、异常值过滤
 *
 * @author LY-FactMesh
 */
public interface TelemetryDataCleaner {

    /**
     * 清洗遥测数据
     *
     * @param raw 原始数据 key=测点名称, value=数值
     * @return 清洗后的数据，无效数据会被过滤
     */
    Map<String, Number> clean(Map<String, Number> raw);
}
