package com.trendyol.kediatr

class CommandBusImpl(private val registry: Registry, private val publishStrategy: PublishStrategy = StopOnExceptionPublishStrategy()) : CommandBus {
    override fun <TQuery : Query<TResponse>, TResponse> executeQuery(query: TQuery): TResponse = processPipeline(registry.getPipelineBehavior(), query) {
        registry.resolveQueryHandler(query.javaClass).handle(query)
    }

    override fun <TCommand : Command> executeCommand(command: TCommand) = processPipeline(registry.getPipelineBehavior(), command) {
        registry.resolveCommandHandler(command.javaClass).handle(command)
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> executeCommand(command: TCommand): TResult = processPipeline(registry.getPipelineBehavior(), command) {
        registry.resolveCommandWithResultHandler(command.javaClass).handle(command)
    }

    override fun <T : Notification> publishNotification(notification: T) = processPipeline(registry.getPipelineBehavior(), notification) {
        publishStrategy.publish(notification, registry.resolveNotificationHandlers(notification.javaClass))
    }

    override suspend fun <TQuery : Query<TResponse>, TResponse> executeQueryAsync(query: TQuery): TResponse = processAsyncPipeline(registry.getAsyncPipelineBehavior(), query) {
        registry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
    }

    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand) = processAsyncPipeline(registry.getAsyncPipelineBehavior(), command) {
        registry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
    }

    override suspend fun <TCommand : CommandWithResult<TResult>, TResult> executeCommandAsync(command: TCommand): TResult = processAsyncPipeline(registry.getAsyncPipelineBehavior(), command) {
        registry.resolveAsyncCommandWithResultHandler(command.javaClass).handleAsync(command)
    }

    override suspend fun <T : Notification> publishNotificationAsync(notification: T) = processAsyncPipeline(registry.getAsyncPipelineBehavior(), notification) {
        publishStrategy.publishAsync(notification, registry.resolveAsyncNotificationHandlers(notification.javaClass))
    }
}