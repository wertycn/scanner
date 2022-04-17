package org.archguard.scanner.common.container

import kotlinx.serialization.Serializable

@Serializable
data class ContainerServiceBase(
    // component name, only if is a component
    var name: String = "",
    var demands: List<HttpContainerDemand> = listOf(),
    var resources: List<HttpContainerResource> = listOf()
)

/**
 * 服务地图基本要素
 *   调用协议
 *   服务提供者
 *   服务消费者
 * ：
 *
 *  调用协议基本要素：
 *     协议名（HTTP,GRPC,DUBBO,...）
 *     服务名
 *     服务域 (HTTP 域名，Dubbo类名等)
 *     服务资源路径 (HTTP  URI,Dubbo 方法)
 *     请求参数
 *     返回值
 *     服务匹配标识 (基于服务域、服务资源路径及协议相关的其他特性生成的用于匹配调用关系的路径)
 *  服务提供者要素：
 *     请求协议
 *     服务提供类名
 *     代码定位信息(文件路径)
 *     代码语言
 *  服务消费者要素：
 *     请求协议
 *     发起调用类名
 *     代码定位信息(文件路径)
 *     代码语言
 *
 */

@Serializable
data class Protocol(
    var name: String = "",
    var domain: String = "",
    var path: String = "",
    // TODO : PARAMS 对象
    var params: List<String> = listOf(),
    var returns: List<String> = listOf(),
    // 匹配标识
    var relation: String = "",
)

@Serializable
class ContainerDemandBase(
    var protocol: Protocol,
    var consumer: Contatiner,
    var language: String = "",
    // todo: 定位信息对象（可用于基于调用关系直接跳转到源码位置）
    var position: String = ""
)

@Serializable
data class ContainerResourceBase(
    var protocol: Protocol,
    var provider: Contatiner,
    var language: String = "",
    var position: String = ""
)

@Serializable
data class Contatiner(
    var packageName: String = "",
    var className: String = "",
    var methodName: String = ""
)
