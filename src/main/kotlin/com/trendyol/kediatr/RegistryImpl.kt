package com.trendyol.kediatr

import com.trendyol.kediatr.common.AsyncNotificationProvider
import com.trendyol.kediatr.common.AsyncPipelineProvider
import com.trendyol.kediatr.common.AsyncQueryProvider
import com.trendyol.kediatr.common.NotificationProvider
import com.trendyol.kediatr.common.PipelineProvider
import com.trendyol.kediatr.common.QueryProvider
import java.lang.reflect.ParameterizedType

class RegistryImpl(
    dependencyProvider: DependencyProvider
) : Registry {
    private val queryMap = HashMap<Class<out Query<*>>, QueryProvider<QueryHandler<*, *>>>()
    private val commandMap = HashMap<Class<out Command>, CommandProvider<CommandHandler<*>>>()
    private val notificationMap = HashMap<Class<out Notification>, MutableList<NotificationProvider<NotificationHandler<*>>>>()
    private val pipelineSet = HashSet<PipelineProvider<PipelineBehavior>>()
    private val commandWithResultMap = HashMap<Class<out CommandWithResult<*>>, CommandWithResultProvider<CommandWithResultHandler<*, *>>>()

    private val asyncCommandMap = HashMap<Class<out Command>, AsyncCommandProvider<AsyncCommandHandler<*>>>()
    private val asyncQueryMap = HashMap<Class<out Query<*>>, AsyncQueryProvider<AsyncQueryHandler<*, *>>>()
    private val asyncNotificationMap = HashMap<Class<out Notification>, MutableList<AsyncNotificationProvider<AsyncNotificationHandler<*>>>>()
    private val asyncPipelineSet = HashSet<AsyncPipelineProvider<AsyncPipelineBehavior>>()
    private val asyncCommandWithResultMap = HashMap<Class<out CommandWithResult<*>>, AsyncCommandWithResultProvider<AsyncCommandWithResultHandler<*, *>>>()

    init {
        dependencyProvider.getSubTypesOf(QueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == QueryHandler::class.java) {
                    val queryClazz = genericInterface.actualTypeArguments[0]

                    queryMap[queryClazz as Class<out Query<*>>] = QueryProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(CommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == CommandHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    commandMap[commandClazz as Class<out Command>] = CommandProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(CommandWithResultHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == CommandWithResultHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    commandWithResultMap[commandClazz as Class<out CommandWithResult<*>>] = CommandWithResultProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(NotificationHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == NotificationHandler::class.java) {
                    val notificationClazz = genericInterface.actualTypeArguments.first() as Class<out Notification>

                    notificationMap.getOrPut(notificationClazz) { mutableListOf() }
                        .add(NotificationProvider(dependencyProvider, it))
                }
            }
        }

        dependencyProvider.getSubTypesOf(AsyncQueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncQueryHandler::class.java) {
                    val queryClazz = genericInterface.actualTypeArguments[0]

                    asyncQueryMap[queryClazz as Class<out Query<*>>] = AsyncQueryProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(AsyncCommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncCommandHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    asyncCommandMap[commandClazz as Class<out Command>] = AsyncCommandProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(AsyncCommandWithResultHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncCommandWithResultHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    asyncCommandWithResultMap[commandClazz as Class<out CommandWithResult<*>>] = AsyncCommandWithResultProvider(dependencyProvider, it)
                }
            }
        }

        dependencyProvider.getSubTypesOf(AsyncNotificationHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncNotificationHandler::class.java) {
                    val notificationClazz = genericInterface.actualTypeArguments.first() as Class<out Notification>

                    asyncNotificationMap.getOrPut(notificationClazz) { mutableListOf() }
                        .add(AsyncNotificationProvider(dependencyProvider, it))
                }
            }
        }

        dependencyProvider.getSubTypesOf(PipelineBehavior::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if (genericInterface as Class<*> == PipelineBehavior::class.java) {
                    pipelineSet.add(PipelineProvider(dependencyProvider, it))
                }
            }
        }

        dependencyProvider.getSubTypesOf(AsyncPipelineBehavior::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if (genericInterface as Class<*> == AsyncPipelineBehavior::class.java) {
                    asyncPipelineSet.add(AsyncPipelineProvider(dependencyProvider, it))
                }
            }
        }
    }

    override fun <TCommand : Command> resolveCommandHandler(commandClass: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[commandClass]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${commandClass.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveCommandWithResultHandler(classOfCommand: Class<TCommand>): CommandWithResultHandler<TCommand, TResult> {
        val handler = commandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveNotificationHandlers(classOfNotification: Class<TNotification>): Collection<NotificationHandler<TNotification>> {
        val notificationHandlers = mutableListOf<NotificationHandler<TNotification>>()
        notificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { notificationHandlers.add(it.get() as NotificationHandler<TNotification>) }
            }
        }
        return notificationHandlers
    }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult> {
        val handler = queryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TQuery, TResult>
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveAsyncCommandWithResultHandler(classOfCommand: Class<TCommand>): AsyncCommandWithResultHandler<TCommand, TResult> {
        val handler = asyncCommandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveAsyncNotificationHandlers(classOfNotification: Class<TNotification>): Collection<AsyncNotificationHandler<TNotification>> {
        val asyncNotificationHandlers = mutableListOf<AsyncNotificationHandler<TNotification>>()
        asyncNotificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { asyncNotificationHandlers.add(it.get() as AsyncNotificationHandler<TNotification>) }
            }
        }
        return asyncNotificationHandlers
    }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TQuery, TResult> {
        val handler = asyncQueryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TQuery, TResult>
    }

    override fun getPipelineBehavior(): PipelineBehavior {
        return pipelineSet.firstOrNull()?.get() ?: PipelineBehaviorImpl()
    }

    override fun getAsyncPipelineBehavior(): AsyncPipelineBehavior {
        return asyncPipelineSet.firstOrNull()?.get() ?: AsyncPipelineBehaviorImpl()
    }
}