package org.archguard.scanner.sourcecode.backend

import chapi.app.analyser.JavaAnalyserApp
import chapi.domain.core.CodeDataStruct
import org.archguard.scanner.common.backend.analyser.dubbo.JavaDubboAnalyser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class JavaDubboAnalyserTest {

    private fun analyserSourceCode(sourceResourcePath: String): Array<CodeDataStruct> {
        val resource = this.javaClass.classLoader.getResource(sourceResourcePath)!!
        val path = Paths.get(resource.toURI()).toFile().absolutePath

        val nodes = JavaAnalyserApp().analysisNodeByPath(path)
        return nodes
    }

    @Test
    fun testParseDubboProvider() {
        val nodes = analyserSourceCode("protocol/dubbo/annotation/AnnotationGreetingServiceImpl.java")
        val javaApiAnalyser = JavaDubboAnalyser()

        nodes.forEach {
            javaApiAnalyser.analysisByNode(it, "")
        }

        val services = javaApiAnalyser.toContainerServices()
        val resources = services[0].resources
        assertEquals(1, resources.size)
        assertEquals("org.apache.dubbo.samples.annotation.api.GreetingService:greeting", resources[0].sourceUrl)
        assertEquals("", resources[0].sourceHttpMethod)
        assertEquals("org.apache.dubbo.samples.annotation.impl", resources[0].packageName)
        assertEquals("AnnotationGreetingServiceImpl", resources[0].className)
        assertEquals("greeting", resources[0].methodName)
    }

    @Test
    fun testParseDubboConsumer() {
        val nodes = analyserSourceCode("protocol/dubbo/annotation/AnnotationAction.java")
        val javaDubboAnalyser = JavaDubboAnalyser()

        nodes.forEach {
            javaDubboAnalyser.analysisByNode(it, "")
        }
        val services = javaDubboAnalyser.toContainerServices()
        val demands = services[0].demands
        assertEquals(4, demands.size)
        var consumerList = listOf<String>()
        consumerList = consumerList + "org.apache.dubbo.samples.annotation.api.GreetingService:greeting"
        consumerList = consumerList + "org.apache.dubbo.samples.annotation.api.GreetingService:replyGreeting"
        consumerList = consumerList + "org.apache.dubbo.samples.annotation.api.HelloService:sayHello"
        consumerList = consumerList + "org.apache.dubbo.samples.annotation.api.HelloService:sayGoodbye"
        demands.forEach {
            assertTrue(consumerList.contains(it.target_url))
        }


    }

}