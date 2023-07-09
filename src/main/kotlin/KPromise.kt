import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import pub.telephone.javapromise.async.Async
import pub.telephone.javapromise.async.promise.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

open class KPromiseScope(
    private val cancelledBroadcast: PromiseCancelledBroadcast,
) {
    val isActive get() = cancelledBroadcast.IsActive.get()
    fun <T> promise(semaphore: PromiseSemaphore? = null, job: KPromiseJob<T>.() -> Unit) =
        cancelledBroadcast.promise(semaphore = semaphore, job = job)

    fun process(semaphore: PromiseSemaphore? = null, job: KPromiseJob.KPromiseProcedure.() -> Unit) =
        promise(semaphore = semaphore) {
            KPromiseJob.KPromiseProcedure(this).job()
        }

    fun <T> resolved(value: T) = cancelledBroadcast.resolved(value)
    fun resolved() = resolved(Unit)
    fun <T> rejected(reason: Throwable?) = cancelledBroadcast.rejected<T>(reason)
    fun failed(reason: Throwable?) = rejected<Unit>(reason)
    fun <T> cancelled() = cancelledBroadcast.cancelled<T>()
    fun terminated() = cancelled<Unit>()
    fun delay(d: Duration) = cancelledBroadcast.delay(d)
    fun <S, R, O> thenAll(
        requiredPromiseList: List<Promise<R>>,
        optionalPromiseList: List<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Any?
    ) = cancelledBroadcast.thenAll<S, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = optionalPromiseList,
        onFulfilled = onFulfilled
    )

    fun <R, O> thenAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Unit
    ) = thenAll<Unit, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, R> thenAll(
        requiredPromiseList: List<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<R, Unit>.() -> Any?
    ) = thenAll<S, R, Unit>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = emptyList(),
        onFulfilled = onFulfilled
    )

    fun <R> thenAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<R, Unit>.() -> Unit
    ) = thenAll<Unit, R>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, O> thenAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseList: List<Promise<O>>,
        onFulfilled: KPromiseCompoundOnFulfilled<Unit, O>.() -> Any?
    ) = thenAll<S, Unit, O>(
        semaphore = semaphore,
        requiredPromiseList = emptyList(),
        optionalPromiseList = optionalPromiseList,
        onFulfilled = onFulfilled
    )

    fun <O> thenAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onFulfilled: KPromiseCompoundOnFulfilled<Unit, O>.() -> Unit
    ) = thenAll<Unit, O>(
        semaphore = semaphore,
        optionalPromiseList = optionalPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, R, O> catchAll(
        requiredPromiseList: List<Promise<R>>,
        optionalPromiseList: List<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected.() -> Any?
    ) = cancelledBroadcast.catchAll<S, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = optionalPromiseList,
        onRejected = onRejected
    )

    fun <R, O> catchAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected.() -> Unit
    ) = catchAll<Unit, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <S, R> catchAll(
        requiredPromiseList: List<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected.() -> Any?
    ) = catchAll<S, R, Unit>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = emptyList(),
        onRejected = onRejected
    )

    fun <R> catchAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected.() -> Unit
    ) = catchAll<Unit, R>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <S, O> catchAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseList: List<Promise<O>>,
        onRejected: KPromiseOnRejected.() -> Any?
    ) = catchAll<S, Unit, O>(
        semaphore = semaphore,
        requiredPromiseList = emptyList(),
        optionalPromiseList = optionalPromiseList,
        onRejected = onRejected
    )

    fun <O> catchAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onRejected: KPromiseOnRejected.() -> Unit
    ) = catchAll<Unit, O>(
        semaphore = semaphore,
        optionalPromiseList = optionalPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <R, O> cancelAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onCancelled: () -> Unit
    ) = cancelledBroadcast.cancelAll<Unit, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        onCancelled = onCancelled
    )

    fun <R> cancelAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onCancelled: () -> Unit
    ) = cancelAll<R, Unit>(
        semaphore = semaphore,
        requiredPromiseArray = requiredPromiseArray,
        optionalPromiseArray = emptyArray(),
        onCancelled = onCancelled
    )

    fun <O> cancelAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onCancelled: () -> Unit
    ) = cancelAll<Unit, O>(
        semaphore = semaphore,
        requiredPromiseArray = emptyArray(),
        optionalPromiseArray = optionalPromiseArray,
        onCancelled = onCancelled
    )

    private fun wrapUnitOnSettled(onSettled: KPromiseOnSettled.() -> Unit): KPromiseOnSettled.() -> Promise<*>? = {
        onSettled()
        null
    }

    fun <R, O> finallyAll(
        requiredPromiseList: List<Promise<R>>,
        optionalPromiseList: List<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onSettled: KPromiseOnSettled.() -> Promise<*>?
    ) = cancelledBroadcast.finallyAll<Unit, R, O>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = optionalPromiseList,
        onSettled = onSettled
    )

    fun <R, O> finallyAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onSettled: KPromiseOnSettled.() -> Unit
    ) = finallyAll(
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        semaphore = semaphore,
        onSettled = wrapUnitOnSettled(onSettled)
    )

    fun <R> finallyAll(
        requiredPromiseList: List<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onSettled: KPromiseOnSettled.() -> Promise<*>?
    ) = finallyAll<R, Unit>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = emptyList(),
        onSettled = onSettled
    )

    fun <R> finallyAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onSettled: KPromiseOnSettled.() -> Unit
    ) = finallyAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        onSettled = wrapUnitOnSettled(onSettled)
    )

    fun <O> finallyAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseList: List<Promise<O>>,
        onSettled: KPromiseOnSettled.() -> Promise<*>?
    ) = finallyAll<Unit, O>(
        semaphore = semaphore,
        requiredPromiseList = emptyList(),
        optionalPromiseList = optionalPromiseList,
        onSettled = onSettled
    )

    fun <O> finallyAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onSettled: KPromiseOnSettled.() -> Unit
    ) = finallyAll(
        semaphore = semaphore,
        optionalPromiseList = optionalPromiseArray.asList(),
        onSettled = wrapUnitOnSettled(onSettled)
    )
}

class KPromiseOnFulfilled<T>(
    val value: T,
    cancelledBroadcast: PromiseCancelledBroadcast,
) : KPromiseScope(cancelledBroadcast)

class KPromiseCompoundOnFulfilled<R, O>(
    val value: PromiseCompoundResult<R, O>,
    cancelledBroadcast: PromiseCancelledBroadcast,
) : KPromiseScope(cancelledBroadcast)

class KPromiseOnRejected(
    val reason: Throwable,
    cancelledBroadcast: PromiseCancelledBroadcast,
) : KPromiseScope(cancelledBroadcast)

class KPromiseOnSettled(
    cancelledBroadcast: PromiseCancelledBroadcast,
) : KPromiseScope(cancelledBroadcast)

private fun Job?.toBroadcast() = this?.let { parentJob ->
    object : PromiseCancelledBroadcast() {
        override fun Listen(r: Runnable?): Any? {
            return r?.let { runnable ->
                parentJob.invokeOnCompletion {
                    runnable.run()
                }
            }
        }

        override fun UnListen(key: Any?) {
            (key as? DisposableHandle)?.dispose()
        }
    }
}

private fun <T> PromiseCancelledBroadcast?.promise(
    semaphore: PromiseSemaphore? = null,
    job: KPromiseJob<T>.() -> Unit
): Promise<T> {
    return Promise(
        this,
        { resolver, rejector, cancelledBroadcast ->
            KPromiseJob(
                resolver = resolver,
                rejector = rejector,
                cancelledBroadcast = cancelledBroadcast,
            ).job()
        },
        semaphore
    )
}

private fun <T> PromiseCancelledBroadcast?.resolved(
    value: T
): Promise<T> {
    return Promise.Resolve(
        this,
        value
    )
}

private fun <T> PromiseCancelledBroadcast?.rejected(
    reason: Throwable?
): Promise<T> {
    return Promise.Reject(
        this,
        reason
    )
}

private fun <T> PromiseCancelledBroadcast?.cancelled(): Promise<T> {
    return Promise.Cancelled(
        this,
    )
}

private fun <S, R, O> PromiseCancelledBroadcast?.thenAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Any?
): Promise<S> {
    return Promise.ThenAll(
        this,
        semaphore,
        { value, cancelledBroadcast ->
            KPromiseCompoundOnFulfilled(value, cancelledBroadcast).onFulfilled()
        },
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun <S, R, O> PromiseCancelledBroadcast?.catchAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onRejected: KPromiseOnRejected.() -> Any?
): Promise<S> {
    return Promise.CatchAll(
        this,
        semaphore,
        { reason, cancelledBroadcast ->
            KPromiseOnRejected(reason, cancelledBroadcast).onRejected()
        },
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun <S, R, O> PromiseCancelledBroadcast?.cancelAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onCancelled: () -> Unit
): Promise<S> {
    return Promise.ForCancelAll(
        this,
        semaphore,
        {
            onCancelled()
        },
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun <S, R, O> PromiseCancelledBroadcast?.finallyAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onSettled: KPromiseOnSettled.() -> Promise<*>?
): Promise<S> {
    return Promise.FinallyAll(
        this,
        semaphore,
        { cancelledBroadcast ->
            KPromiseOnSettled(cancelledBroadcast).onSettled()
        },
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun PromiseCancelledBroadcast?.delay(d: Duration) = Async.Delay(this, d.toJavaDuration()) as Promise<Unit>

fun <T> CoroutineScope.promise(semaphore: PromiseSemaphore? = null, job: KPromiseJob<T>.() -> Unit) =
    this.coroutineContext[Job].toBroadcast().promise(semaphore = semaphore, job = job)

fun CoroutineScope.process(semaphore: PromiseSemaphore? = null, job: KPromiseJob.KPromiseProcedure.() -> Unit) =
    promise(semaphore = semaphore) {
        KPromiseJob.KPromiseProcedure(this).job()
    }

fun <T> CoroutineScope.resolved(value: T) = this.coroutineContext[Job].toBroadcast().resolved(value)
fun CoroutineScope.resolved() = resolved(Unit)
fun <T> CoroutineScope.rejected(reason: Throwable?) = this.coroutineContext[Job].toBroadcast().rejected<T>(reason)
fun CoroutineScope.failed(reason: Throwable?) = rejected<Unit>(reason)
fun <T> CoroutineScope.cancelled() = this.coroutineContext[Job].toBroadcast().cancelled<T>()
fun CoroutineScope.terminated() = cancelled<Unit>()

fun <S, R, O> CoroutineScope.thenAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Any?
) = this.coroutineContext[Job].toBroadcast().thenAll<S, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = optionalPromiseList,
    onFulfilled = onFulfilled
)

fun <R, O> CoroutineScope.thenAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Unit
) = thenAll<Unit, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, R> CoroutineScope.thenAll(
    requiredPromiseList: List<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, Unit>.() -> Any?
) = thenAll<S, R, Unit>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = emptyList(),
    onFulfilled = onFulfilled
)

fun <R> CoroutineScope.thenAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, Unit>.() -> Unit
) = thenAll<Unit, R>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, O> CoroutineScope.thenAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseList: List<Promise<O>>,
    onFulfilled: KPromiseCompoundOnFulfilled<Unit, O>.() -> Any?
) = thenAll<S, Unit, O>(
    semaphore = semaphore,
    requiredPromiseList = emptyList(),
    optionalPromiseList = optionalPromiseList,
    onFulfilled = onFulfilled
)

fun <O> CoroutineScope.thenAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onFulfilled: KPromiseCompoundOnFulfilled<Unit, O>.() -> Unit
) = thenAll<Unit, O>(
    semaphore = semaphore,
    optionalPromiseList = optionalPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, R, O> CoroutineScope.catchAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Any?
) = this.coroutineContext[Job].toBroadcast().catchAll<S, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = optionalPromiseList,
    onRejected = onRejected
)

fun <R, O> CoroutineScope.catchAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Unit
) = catchAll<Unit, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    onRejected = onRejected
)

fun <S, R> CoroutineScope.catchAll(
    requiredPromiseList: List<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Any?
) = catchAll<S, R, Unit>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = emptyList(),
    onRejected = onRejected
)

fun <R> CoroutineScope.catchAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Unit
) = catchAll<Unit, R>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    onRejected = onRejected
)

fun <S, O> CoroutineScope.catchAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseList: List<Promise<O>>,
    onRejected: KPromiseOnRejected.() -> Any?
) = catchAll<S, Unit, O>(
    semaphore = semaphore,
    requiredPromiseList = emptyList(),
    optionalPromiseList = optionalPromiseList,
    onRejected = onRejected
)

fun <O> CoroutineScope.catchAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onRejected: KPromiseOnRejected.() -> Unit
) = catchAll<Unit, O>(
    semaphore = semaphore,
    optionalPromiseList = optionalPromiseArray.asList(),
    onRejected = onRejected
)

fun <R, O> CoroutineScope.cancelAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onCancelled: () -> Unit
) = this.coroutineContext[Job].toBroadcast().cancelAll<Unit, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    onCancelled = onCancelled
)

fun <R> CoroutineScope.cancelAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onCancelled: () -> Unit
) = cancelAll<R, Unit>(
    semaphore = semaphore,
    requiredPromiseArray = requiredPromiseArray,
    optionalPromiseArray = emptyArray(),
    onCancelled = onCancelled
)

fun <O> CoroutineScope.cancelAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onCancelled: () -> Unit
) = cancelAll<Unit, O>(
    semaphore = semaphore,
    requiredPromiseArray = emptyArray(),
    optionalPromiseArray = optionalPromiseArray,
    onCancelled = onCancelled
)

private fun wrapUnitOnSettled(onSettled: KPromiseOnSettled.() -> Unit): KPromiseOnSettled.() -> Promise<*>? = {
    onSettled()
    null
}

fun <R, O> CoroutineScope.finallyAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Promise<*>?
) = this.coroutineContext[Job].toBroadcast().finallyAll<Unit, R, O>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = optionalPromiseList,
    onSettled = onSettled
)

fun <R, O> CoroutineScope.finallyAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Unit
) = finallyAll(
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    semaphore = semaphore,
    onSettled = wrapUnitOnSettled(onSettled)
)

fun <R> CoroutineScope.finallyAll(
    requiredPromiseList: List<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Promise<*>?
) = finallyAll<R, Unit>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = emptyList(),
    onSettled = onSettled
)

fun <R> CoroutineScope.finallyAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Unit
) = finallyAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    onSettled = wrapUnitOnSettled(onSettled)
)

fun <O> CoroutineScope.finallyAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseList: List<Promise<O>>,
    onSettled: KPromiseOnSettled.() -> Promise<*>?
) = finallyAll<Unit, O>(
    semaphore = semaphore,
    requiredPromiseList = emptyList(),
    optionalPromiseList = optionalPromiseList,
    onSettled = onSettled
)

fun <O> CoroutineScope.finallyAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onSettled: KPromiseOnSettled.() -> Unit
) = finallyAll(
    semaphore = semaphore,
    optionalPromiseList = optionalPromiseArray.asList(),
    onSettled = wrapUnitOnSettled(onSettled)
)

fun CoroutineScope.delay(d: Duration) = this.coroutineContext[Job].toBroadcast().delay(d)

open class KPromiseJob<S>(
    private val resolver: PromiseResolver<S>,
    private val rejector: PromiseRejector,
    private val cancelledBroadcast: PromiseCancelledBroadcast,
) : KPromiseScope(cancelledBroadcast) {
    class KPromiseProcedure(
        job: KPromiseJob<Unit>,
    ) : KPromiseJob<Unit>(job.resolver, job.rejector, job.cancelledBroadcast) {
        fun resolve() = resolve(Unit)
    }

    fun resolve(value: S) = resolver.ResolveValue(value)
    fun resolve(promise: Promise<S>) = resolver.ResolvePromise(promise)
    fun reject(reason: Throwable?) = rejector.Reject(reason)
}

fun <S, T> Promise<T>.then(
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseOnFulfilled<T>.() -> Any?
): Promise<S> {
    return Then(semaphore) { value, cancelledBroadcast ->
        KPromiseOnFulfilled(value, cancelledBroadcast).onFulfilled()
    }
}

fun <T> Promise<T>.next(semaphore: PromiseSemaphore? = null, onFulfilled: KPromiseOnFulfilled<T>.() -> Unit) =
    then<Unit, T>(semaphore = semaphore, onFulfilled = onFulfilled)

fun <S, T> Promise<T>.catch(
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Any?
): Promise<S> {
    return Catch(semaphore) { reason, cancelledBroadcast ->
        KPromiseOnRejected(reason, cancelledBroadcast).onRejected()
    }
}

fun <T> Promise<T>.error(semaphore: PromiseSemaphore? = null, onRejected: KPromiseOnRejected.() -> Unit) =
    catch<Unit, T>(semaphore = semaphore, onRejected = onRejected)


fun <T> Promise<T>.cancel(semaphore: PromiseSemaphore? = null, onCancelled: () -> Unit): Promise<Unit> {
    return ForCancel(semaphore) {
        onCancelled()
    }
}

fun <T> Promise<T>.finally(
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Any?
): Promise<Unit> {
    return Finally(semaphore) { cancelledBroadcast ->
        KPromiseOnSettled(cancelledBroadcast).onSettled() as? Promise<*>
    }
}

fun <T> Promise<T>.timeout(timeout: Duration, onTimeOut: ((Duration) -> Unit)? = null): Promise<T> {
    SetTimeout(timeout.toJavaDuration(), onTimeOut?.let { l ->
        { d ->
            l(d.toKotlinDuration())
        }
    })
    return this
}