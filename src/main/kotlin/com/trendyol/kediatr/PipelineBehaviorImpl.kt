package com.trendyol.kediatr

class PipelineBehaviorImpl : PipelineBehavior {
    override fun <TRequest, TResponse> process(request: TRequest, act: () -> TResponse): TResponse {
        return act()
    }
}
