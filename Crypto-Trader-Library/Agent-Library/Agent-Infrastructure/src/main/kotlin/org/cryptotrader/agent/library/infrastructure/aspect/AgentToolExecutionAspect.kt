package org.cryptotrader.agent.library.infrastructure.aspect

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.cryptotrader.agent.library.entity.AgentToolExecution
import org.cryptotrader.agent.library.model.AgentTool
import org.cryptotrader.agent.library.services.entity.AgentToolExecutionEntityService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
open class AgentToolExecutionAspect @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val agentToolExecutionEntityService: AgentToolExecutionEntityService
) {
    private val log: Logger = LoggerFactory.getLogger(AgentToolExecutionAspect::class.java)

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    fun captureExecution(joinPoint: ProceedingJoinPoint): Any? {
        val signature: MethodSignature = joinPoint.signature as MethodSignature
        val methodName: String = signature.name

        val input: Map<String, Any> = signature.parameterNames
            .mapIndexed { index, parameterName ->
                parameterName to joinPoint.args[index]
            }
            .toMap()

        val startTime: Long = System.nanoTime()
        val agentTool: AgentTool = AgentTool.fromMethodName(methodName)

        val jsonNodeInput: JsonNode? = try {
            this.objectMapper.valueToTree(input)
        } catch (e: Exception) {
            log.error("Failed to serialize input for tool {}", methodName, e)
            null
        }

        return try {
            val output: Any? = joinPoint.proceed()
            val durationMs: Long = (System.nanoTime() - startTime) / 1_000_000

            log.info(
                "Executed tool {} for {}ms",
                methodName,
                durationMs
            )

            val jsonNodeOutput: JsonNode = this.objectMapper.valueToTree(output)

            val agentToolExecutionEntity = AgentToolExecution(
                agentTool,
                LocalDateTime.now(),
                jsonNodeInput,
                jsonNodeOutput,
                durationMs,
                true,
            )

            this.agentToolExecutionEntityService.save(agentToolExecutionEntity)

            output
        } catch (exception: Throwable) {
            val durationMs: Long = (System.nanoTime() - startTime) / 1_000_000
            log.error(
                "Failed to execute tool {}: {}",
                methodName,
                exception.javaClass.simpleName
            )

            val agentToolExecutionEntity = AgentToolExecution(
                agentTool,
                LocalDateTime.now(),
                jsonNodeInput,
                null,
                durationMs,
                false,
            )

            this.agentToolExecutionEntityService.save(agentToolExecutionEntity)
            throw exception
        }
    }
}
