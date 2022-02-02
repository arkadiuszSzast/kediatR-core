package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 * @since 1.0.12
 * @see AsyncPipelineBehavior
 */
interface PipelineBehavior {
    /**
     * Process to invoke before handling any query, command or notification
     *
     * @param request the request to handle
     */
    fun <TRequest, TResponse> process(request: TRequest, act: () -> TResponse): TResponse
}
