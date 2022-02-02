package com.trendyol.kediatr

interface CommandBus {
    fun <TQuery : Query<TResponse>, TResponse> executeQuery(query: TQuery): TResponse

    fun <TCommand : Command> executeCommand(command: TCommand)

    fun <TCommand : CommandWithResult<TResult>, TResult> executeCommand(command: TCommand): TResult

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    fun <T : Notification> publishNotification(notification: T)

    suspend fun <TQuery : Query<TResponse>, TResponse> executeQueryAsync(query: TQuery): TResponse

    suspend fun <TCommand : Command> executeCommandAsync(command: TCommand)

    suspend fun <TCommand : CommandWithResult<TResult>, TResult> executeCommandAsync(command: TCommand): TResult

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    suspend fun <T : Notification> publishNotificationAsync(notification: T)

    fun <TRequest, TResponse> processPipeline(
        pipelineBehavior: PipelineBehavior,
        request: TRequest,
        act: () -> TResponse
    ): TResponse {
        return pipelineBehavior.process(request) { act() }
    }

    suspend fun <TRequest, TResponse> processAsyncPipeline(
        asyncPipelineBehavior: AsyncPipelineBehavior,
        request: TRequest,
        act: suspend () -> TResponse
    ): TResponse {
        return asyncPipelineBehavior.process(request) { act() }

    }
}

