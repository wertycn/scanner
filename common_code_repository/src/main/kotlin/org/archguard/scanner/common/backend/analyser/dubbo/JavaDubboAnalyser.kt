package org.archguard.scanner.common.backend.analyser.dubbo

import chapi.domain.core.CodeAnnotation
import chapi.domain.core.CodeDataStruct
import chapi.domain.core.CodeField
import org.archguard.scanner.common.container.HttpContainerDemand
import org.archguard.scanner.common.container.HttpContainerResource
import org.archguard.scanner.common.container.HttpContainerService

/**
 *
 * Dubbo 代码示例 git@github.com:apache/dubbo-samples.git
 *
 */
class JavaDubboAnalyser {
    var demands: List<HttpContainerDemand> = listOf()
    var resources: List<HttpContainerResource> = listOf()

    private var dubboPackagePrefix = "org.apache.dubbo.config.annotation."

    fun analysisByNode(node: CodeDataStruct, _workspace: String) {

        // 当前类导入的Dubbo相关注解存入map,
        val dubboAnnotationMap = node.Imports.filter { it.Source.startsWith(dubboPackagePrefix) }.associate {
            Pair(it.Source.replaceFirst(dubboPackagePrefix, ""), it.Source)
        }
        // 导入的包为空不进行分析
        if (dubboAnnotationMap.isEmpty()) {
            return
        }
        // 判断是否存在 DubboService ,Service 为Dubbo旧版本注解
        val serviceAnnotation = node.filterAnnotations("DubboService", "Service")
        // 筛选出包含DubboService注解且导入了官方包的Class
        val dubboServiceAnnotation = serviceAnnotation.filter {
            dubboAnnotationMap.containsKey(it.Name) || dubboAnnotationMap.containsKey("*")
        }

        // 获取DubboService的接口
        createResource(dubboServiceAnnotation, node)

        val fieldMap = node.Fields.filter {
            // 注解为DubboReference 或Reference 且在Import 的Dubbo注解列表内
            it.Annotaitons
                .filter { it.Name == "DubboReference" || it.Name == "Reference" }
                .filter { dubboAnnotationMap.containsKey(it.Name) || dubboAnnotationMap.containsKey("*") }
                .isNotEmpty()
        }.associate { Pair(it.TypeType, it) }
        this.createDemand(fieldMap, node)


    }


    /**
      * 基于dubbo接口一定实现Interface的方法，因此存在Override注解
      * TODO:
      *   存在例外情况： 方法在父类中实现，子类只继承则无法识别 (存在跨文件甚至跨项目的情况，如父类可能基于maven等依赖工具引入，精准识别需要基于编译后包含所有依赖的完整jar包的扫描结果);
      *   此外，还需要判断方法的可见性 ，只针对public方法生效
      */
    private fun createResource(
        dubboServiceAnnotation: List<CodeAnnotation>,
        node: CodeDataStruct
    ) {
        if (dubboServiceAnnotation.isNotEmpty() && node.Implements.isNotEmpty()) {
            node.Implements.forEach {
                val dubboInterface = it
                node.Functions
                    //
                    .filter { it.Annotations.filter { it.Name == "Override" }.isNotEmpty() }
                    .forEach {
                        // TODO: demo暂时借用HTTP API 的对象申明 ，后续需要调整为更标准的服务提供者对象
                        this.resources = this.resources + HttpContainerResource(
                            sourceUrl = dubboInterface + ":" + it.Name,
                            sourceHttpMethod = "",
                            packageName = node.Package,
                            className = node.NodeName,
                            methodName = it.Name
                        )
                    }
            }
        }
    }

    private fun createDemand(referenceField: Map<String, CodeField>, node: CodeDataStruct) {

        // 遍历import，找到与reference 字段匹配的import
        val importMap = node.Imports.filter { referenceField.containsKey(it.Source.split(".").last()) }
            .associate { Pair(it.Source.split(".").last(), it.Source) }

        // 分析代码调用获取
        node.Functions.forEach {
            it.FunctionCalls.filter { importMap.containsKey(it.NodeName) }.forEach {
                demands = demands + HttpContainerDemand(
                    source_caller = node.NodeName,
                    target_url = importMap.get(it.NodeName) + ":" + it.FunctionName
                )
            }
        }
    }

    fun toContainerServices(): Array<HttpContainerService> {
        var componentCalls: Array<HttpContainerService> = arrayOf()

        val componentRef = HttpContainerService(name = "")
        componentRef.resources = this.resources
        componentRef.demands = this.demands

        componentCalls += componentRef
        return componentCalls
    }
}