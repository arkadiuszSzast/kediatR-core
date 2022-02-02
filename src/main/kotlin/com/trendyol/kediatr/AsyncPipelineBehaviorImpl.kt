package com.trendyol.kediatr

class AsyncPipelineBehaviorImpl : AsyncPipelineBehavior {
    override suspend fun <TRequest, TResponse> process(request: TRequest, act: suspend () -> TResponse): TResponse {
        return act()
    }
}