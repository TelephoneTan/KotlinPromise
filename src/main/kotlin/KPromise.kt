import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import pub.telephone.javapromise.async.Async
import pub.telephone.javapromise.async.promise.*
import pub.telephone.javapromise.async.task.once.OnceTask
import pub.telephone.javapromise.async.task.shared.SharedTask
import pub.telephone.javapromise.async.task.timed.TimedTask
import pub.telephone.javapromise.async.task.versioned.VersionedPromise
import pub.telephone.javapromise.async.task.versioned.VersionedTask
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

open class KPromiseScope(
    private val cancelledBroadcast: PromiseCancelledBroadcast,
) {
    val isActive get() = cancelledBroadcast.IsActive.get()

    fun <T> onceTask(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob<T>.() -> Unit)? = null
    ) = cancelledBroadcast.onceTask(
        semaphore = semaphore,
        job = job,
    )

    fun onceProcess(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
    ) = onceTask<Unit>(semaphore, job?.let {
        {
            KPromiseJob.KPromiseProcedure(this).job()
        }
    })

    fun <T> sharedTask(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob<T>.() -> Unit)? = null
    ) = cancelledBroadcast.sharedTask(
        semaphore = semaphore,
        job = job,
    )

    fun sharedProcess(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
    ) = sharedTask<Unit>(semaphore, job?.let {
        {
            KPromiseJob.KPromiseProcedure(this).job()
        }
    })

    fun timedTask(
        interval: Duration,
        semaphore: PromiseSemaphore? = null,
        lifeTimes: Int? = null,
        job: KPromiseJob<Boolean>.() -> Unit
    ) = cancelledBroadcast.timedTask(
        interval = interval,
        semaphore = semaphore,
        lifeTimes = lifeTimes,
        job = job,
    )

    fun <T> versionedTask(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob<T>.() -> Unit)? = null
    ) = cancelledBroadcast.versionedTask(
        semaphore = semaphore,
        job = job,
    )

    fun versionedProcess(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
    ) = versionedTask<Unit>(semaphore, job?.let {
        {
            KPromiseJob.KPromiseProcedure(this).job()
        }
    })

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

private fun <T> (KPromiseJob<T>.() -> Unit).toPromiseJob() =
    PromiseCancellableJob { resolver, rejector, cancelledBroadcast ->
        KPromiseJob(
            resolver = resolver,
            rejector = rejector,
            cancelledBroadcast = cancelledBroadcast,
        ).this()
    }

private fun <T> PromiseCancelledBroadcast?.onceTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
): OnceTask<T> {
    return OnceTask(
        this,
        job?.toPromiseJob(),
        semaphore,
    )
}

private fun <T> PromiseCancelledBroadcast?.sharedTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
): SharedTask<T> {
    return SharedTask(
        this,
        job?.toPromiseJob(),
        semaphore,
    )
}

private fun PromiseCancelledBroadcast?.timedTask(
    interval: Duration,
    semaphore: PromiseSemaphore? = null,
    lifeTimes: Int? = null,
    job: KPromiseJob<Boolean>.() -> Unit
): TimedTask {
    return TimedTask(
        this,
        interval.toJavaDuration(),
        job.toPromiseJob(),
        lifeTimes,
        semaphore,
    )
}

private fun <T> PromiseCancelledBroadcast?.versionedTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
): VersionedTask<T> {
    return VersionedTask(
        this,
        job?.toPromiseJob(),
        semaphore,
    )
}

fun <T> OnceTask<T>.perform(job: (KPromiseJob<T>.() -> Unit)? = null): Promise<T> {
    return job?.let {
        Do(job.toPromiseJob())
    } ?: Do()
}

fun OnceTask<Unit>.process(job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null) = perform(
    job?.let {
        {
            KPromiseJob.KPromiseProcedure(this).job()
        }
    }
)

fun <T> SharedTask<T>.perform(job: (KPromiseJob<T>.() -> Unit)? = null): Promise<T> {
    return job?.let {
        Do(job.toPromiseJob())
    } ?: Do()
}

fun SharedTask<Unit>.process(job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null) = perform(
    job?.let { { KPromiseJob.KPromiseProcedure(this).job() } }
)

fun TimedTask.start(delay: Duration?): Promise<Int> {
    return delay?.let {
        Start(delay.toJavaDuration())
    } ?: Start()
}

fun TimedTask.pause(): Promise<Unit> {
    return Pause() as Promise<Unit>
}

fun TimedTask.resume(delay: Duration?): Promise<Unit> {
    return (delay?.let {
        Resume(delay.toJavaDuration())
    } ?: Resume()) as Promise<Unit>
}

fun TimedTask.addTimesBy(delta: Int): Promise<Int> {
    return AddTimesBy(delta)
}

fun TimedTask.reduceTimesBy(delta: Int): Promise<Int> {
    return ReduceTimesBy(delta)
}

fun TimedTask.cancel(): Promise<Unit> {
    return Cancel() as Promise<Unit>
}

fun TimedTask.interval(interval: Duration): Promise<Unit> {
    return SetInterval(interval.toJavaDuration()) as Promise<Unit>
}

fun <T> VersionedTask<T>.perform(version: Int? = null, job: (KPromiseJob<T>.() -> Unit)? = null): VersionedPromise<T> {
    return if (job != null) {
        val j = job.toPromiseJob()
        version?.let {
            Perform(version, j)
        } ?: Perform(j)
    } else {
        version?.let {
            Perform(version)
        } ?: Perform()
    }
}

fun VersionedTask<Unit>.process(version: Int? = null, job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null) =
    perform(
        version,
        job?.let {
            {
                KPromiseJob.KPromiseProcedure(this).job()
            }
        }
    )

private fun <T> PromiseCancelledBroadcast?.promise(
    semaphore: PromiseSemaphore? = null,
    job: KPromiseJob<T>.() -> Unit
): Promise<T> {
    return Promise(
        this,
        job.toPromiseJob(),
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

private fun <R, O, S> (KPromiseCompoundOnFulfilled<R, O>.() -> Any?).toCompoundFulfilledListener() =
    PromiseCancellableCompoundFulfilledListener<R, O, S> { value, cancelledBroadcast ->
        KPromiseCompoundOnFulfilled(value, cancelledBroadcast).this()
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
        onFulfilled.toCompoundFulfilledListener(),
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun <S> (KPromiseOnRejected.() -> Any?).toRejectedListener() =
    PromiseCancellableRejectedListener<S> { reason, cancelledBroadcast ->
        KPromiseOnRejected(reason, cancelledBroadcast).this()
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
        onRejected.toRejectedListener(),
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun (() -> Unit).toCancelledListener() = PromiseCancelledListener {
    this()
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
        onCancelled.toCancelledListener(),
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun (KPromiseOnSettled.() -> Promise<*>?).toSettledListener() =
    PromiseCancellableSettledListener { cancelledBroadcast ->
        KPromiseOnSettled(cancelledBroadcast).this()
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
        onSettled.toSettledListener(),
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun PromiseCancelledBroadcast?.delay(d: Duration) = Async.Delay(this, d.toJavaDuration()) as Promise<Unit>

fun <T> CoroutineScope.onceTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
) = coroutineContext[Job].toBroadcast().onceTask(
    semaphore = semaphore,
    job = job,
)

fun CoroutineScope.onceProcess(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
) = onceTask<Unit>(semaphore, job?.let {
    {
        KPromiseJob.KPromiseProcedure(this).job()
    }
})

fun <T> CoroutineScope.sharedTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
) = coroutineContext[Job].toBroadcast().sharedTask(
    semaphore = semaphore,
    job = job,
)

fun CoroutineScope.sharedProcess(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
) = sharedTask<Unit>(semaphore, job?.let {
    {
        KPromiseJob.KPromiseProcedure(this).job()
    }
})

fun CoroutineScope.timedTask(
    interval: Duration,
    semaphore: PromiseSemaphore? = null,
    lifeTimes: Int? = null,
    job: KPromiseJob<Boolean>.() -> Unit
) = coroutineContext[Job].toBroadcast().timedTask(
    interval = interval,
    semaphore = semaphore,
    lifeTimes = lifeTimes,
    job = job,
)

fun <T> CoroutineScope.versionedTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
) = coroutineContext[Job].toBroadcast().versionedTask(
    semaphore = semaphore,
    job = job,
)

fun CoroutineScope.versionedProcess(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob.KPromiseProcedure.() -> Unit)? = null
) = versionedTask<Unit>(semaphore, job?.let {
    {
        KPromiseJob.KPromiseProcedure(this).job()
    }
})

fun <T> CoroutineScope.promise(semaphore: PromiseSemaphore? = null, job: KPromiseJob<T>.() -> Unit) =
    coroutineContext[Job].toBroadcast().promise(semaphore = semaphore, job = job)

fun CoroutineScope.process(semaphore: PromiseSemaphore? = null, job: KPromiseJob.KPromiseProcedure.() -> Unit) =
    promise(semaphore = semaphore) {
        KPromiseJob.KPromiseProcedure(this).job()
    }

fun <T> CoroutineScope.resolved(value: T) = coroutineContext[Job].toBroadcast().resolved(value)
fun CoroutineScope.resolved() = resolved(Unit)
fun <T> CoroutineScope.rejected(reason: Throwable?) = coroutineContext[Job].toBroadcast().rejected<T>(reason)
fun CoroutineScope.failed(reason: Throwable?) = rejected<Unit>(reason)
fun <T> CoroutineScope.cancelled() = coroutineContext[Job].toBroadcast().cancelled<T>()
fun CoroutineScope.terminated() = cancelled<Unit>()

fun <S, R, O> CoroutineScope.thenAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<R, O>.() -> Any?
) = coroutineContext[Job].toBroadcast().thenAll<S, R, O>(
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
) = coroutineContext[Job].toBroadcast().catchAll<S, R, O>(
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
) = coroutineContext[Job].toBroadcast().cancelAll<Unit, R, O>(
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
) = coroutineContext[Job].toBroadcast().finallyAll<Unit, R, O>(
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

fun CoroutineScope.delay(d: Duration) = coroutineContext[Job].toBroadcast().delay(d)

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

private fun <T, S> (KPromiseOnFulfilled<T>.() -> Any?).toFulfilledListener() =
    PromiseCancellableFulfilledListener<T, S> { value, cancelledBroadcast ->
        KPromiseOnFulfilled(value, cancelledBroadcast).this()
    }

fun <S, T> Promise<T>.then(
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseOnFulfilled<T>.() -> Any?
): Promise<S> {
    return Then(semaphore, onFulfilled.toFulfilledListener())
}

fun <T> Promise<T>.next(semaphore: PromiseSemaphore? = null, onFulfilled: KPromiseOnFulfilled<T>.() -> Unit) =
    then<Unit, T>(semaphore = semaphore, onFulfilled = onFulfilled)

fun <S, T> Promise<T>.catch(
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected.() -> Any?
): Promise<S> {
    return Catch(semaphore, onRejected.toRejectedListener())
}

fun <T> Promise<T>.error(semaphore: PromiseSemaphore? = null, onRejected: KPromiseOnRejected.() -> Unit) =
    catch<Unit, T>(semaphore = semaphore, onRejected = onRejected)


fun <T> Promise<T>.cancel(semaphore: PromiseSemaphore? = null, onCancelled: () -> Unit): Promise<Unit> {
    return ForCancel(semaphore, onCancelled.toCancelledListener())
}

private fun (KPromiseOnSettled.() -> Any?).toSettledListenerUnit() =
    PromiseCancellableSettledListener { cancelledBroadcast ->
        KPromiseOnSettled(cancelledBroadcast).this() as? Promise<*>
    }

fun <T> Promise<T>.finally(
    semaphore: PromiseSemaphore? = null,
    onSettled: KPromiseOnSettled.() -> Any?
): Promise<Unit> {
    return Finally(semaphore, onSettled.toSettledListenerUnit())
}

fun <T> Promise<T>.timeout(timeout: Duration, onTimeOut: ((Duration) -> Unit)? = null): Promise<T> {
    SetTimeout(timeout.toJavaDuration(), onTimeOut?.let { l ->
        { d ->
            l(d.toKotlinDuration())
        }
    })
    return this
}