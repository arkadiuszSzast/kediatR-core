package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

var asyncPipelineProcessCounter = 0
var pipelineProcessCounter = 0

class PipelineBehaviorTest {

    init {
        asyncPipelineProcessCounter = 0
        pipelineProcessCounter = 0
    }

    @Test
    fun `should process command with pipeline`() {
        val handler = MyCommandHandler()
        val pipeline = MyPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandHandler::class.java, handler), Pair(MyPipelineBehavior::class.java, pipeline))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(MyCommand())

        assertTrue { pipelineProcessCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        val handler = AsyncMyCommandHandler()
        val pipeline = MyAsyncPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandHandler::class.java, handler), Pair(MyAsyncPipelineBehavior::class.java, pipeline))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        runBlocking {
            bus.executeCommandAsync(MyAsyncCommand())

        }

        assertTrue { asyncPipelineProcessCounter == 1 }
    }
}

class MyBrokenCommand : Command

class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

class MyBrokenAsyncHandler : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        throw Exception()
    }

}

class MyPipelineBehavior : PipelineBehavior {
    override fun <TRequest, TResponse> process(request: TRequest, act: () -> TResponse): TResponse {
        pipelineProcessCounter++
        return act()
    }
}

class MyAsyncPipelineBehavior : AsyncPipelineBehavior {
    override suspend fun <TRequest, TResponse> process(request: TRequest, act: suspend () -> TResponse): TResponse {
        asyncPipelineProcessCounter++
        return act()
    }
}
