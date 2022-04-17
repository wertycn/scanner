package org.archguard.scanner.common.container

import kotlinx.serialization.Serializable

@Serializable
data class DubboContainerService(
    // component name, only if is a component
    var name: String = "",
    var demands: List<HttpContainerDemand> = listOf(),
    var resources: List<HttpContainerResource> = listOf()
)

@Serializable
data class DubboContainerDemand(
    var source_caller: String = "",
    var call_routes: List<String> = listOf(),
    var base: String = "",
    var target_url: String = "",
    var target_http_method: String = "",
    var call_data: String = ""
)

@Serializable
data class DubboContainerResource(
    var sourceUrl: String = "",
    var sourceHttpMethod: String = "",
    var packageName: String = "",
    var className: String = "",
    var methodName: String = ""
)
