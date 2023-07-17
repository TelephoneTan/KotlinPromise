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

open class KPromiseScope<S>(
    val ps: PromiseState<S>,
) {
    val isActive get() = ps.CancelledBroadcast.IsActive.get()

    fun <T> onceTask(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob<T>.() -> Unit)? = null
    ) = ps.ScopeCancelledBroadcast.onceTask(
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
    ) = ps.ScopeCancelledBroadcast.sharedTask(
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
    ) = ps.ScopeCancelledBroadcast.timedTask(
        interval = interval,
        semaphore = semaphore,
        lifeTimes = lifeTimes,
        job = job,
    )

    fun <T> versionedTask(
        semaphore: PromiseSemaphore? = null,
        job: (KPromiseJob<T>.() -> Unit)? = null
    ) = ps.ScopeCancelledBroadcast.versionedTask(
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
        ps.ScopeCancelledBroadcast.promise(semaphore = semaphore, job = job)

    fun process(semaphore: PromiseSemaphore? = null, job: KPromiseJob.KPromiseProcedure.() -> Unit) =
        promise(semaphore = semaphore) {
            KPromiseJob.KPromiseProcedure(this).job()
        }

    fun <T> resolved(value: T) = ps.ScopeCancelledBroadcast.resolved(value)
    fun resolved() = resolved(Unit)
    fun <T> rejected(reason: Throwable?) = ps.ScopeCancelledBroadcast.rejected<T>(reason)
    fun failed(reason: Throwable?) = rejected<Unit>(reason)
    fun <T> cancelled() = ps.ScopeCancelledBroadcast.cancelled<T>()
    fun terminated() = cancelled<Unit>()
    fun delay(d: Duration) = ps.ScopeCancelledBroadcast.delay(d)
    fun <S, R, O> thenAll(
        requiredPromiseList: List<Promise<R>>,
        optionalPromiseList: List<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<S, R, O>.() -> Any?
    ) = ps.ScopeCancelledBroadcast.thenAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = optionalPromiseList,
        onFulfilled = onFulfilled
    )

    fun <R, O> thenAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<Unit, R, O>.() -> Unit
    ) = thenAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, R> thenAll(
        requiredPromiseList: List<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<S, R, Unit>.() -> Any?
    ) = thenAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = emptyList(),
        onFulfilled = onFulfilled
    )

    fun <R> thenAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onFulfilled: KPromiseCompoundOnFulfilled<Unit, R, Unit>.() -> Unit
    ) = thenAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, O> thenAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseList: List<Promise<O>>,
        onFulfilled: KPromiseCompoundOnFulfilled<S, Unit, O>.() -> Any?
    ) = thenAll(
        semaphore = semaphore,
        requiredPromiseList = emptyList(),
        optionalPromiseList = optionalPromiseList,
        onFulfilled = onFulfilled
    )

    fun <O> thenAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onFulfilled: KPromiseCompoundOnFulfilled<Unit, Unit, O>.() -> Unit
    ) = thenAll(
        semaphore = semaphore,
        optionalPromiseList = optionalPromiseArray.asList(),
        onFulfilled = onFulfilled
    )

    fun <S, R, O> catchAll(
        requiredPromiseList: List<Promise<R>>,
        optionalPromiseList: List<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected<S>.() -> Any?
    ) = ps.ScopeCancelledBroadcast.catchAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = optionalPromiseList,
        onRejected = onRejected
    )

    fun <R, O> catchAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected<Unit>.() -> Unit
    ) = catchAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        optionalPromiseList = optionalPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <S, R> catchAll(
        requiredPromiseList: List<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected<S>.() -> Any?
    ) = catchAll<S, R, Unit>(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseList,
        optionalPromiseList = emptyList(),
        onRejected = onRejected
    )

    fun <R> catchAll(
        requiredPromiseArray: Array<Promise<R>>,
        semaphore: PromiseSemaphore? = null,
        onRejected: KPromiseOnRejected<Unit>.() -> Unit
    ) = catchAll(
        semaphore = semaphore,
        requiredPromiseList = requiredPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <S, O> catchAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseList: List<Promise<O>>,
        onRejected: KPromiseOnRejected<S>.() -> Any?
    ) = catchAll<S, Unit, O>(
        semaphore = semaphore,
        requiredPromiseList = emptyList(),
        optionalPromiseList = optionalPromiseList,
        onRejected = onRejected
    )

    fun <O> catchAll(
        semaphore: PromiseSemaphore? = null,
        optionalPromiseArray: Array<Promise<O>>,
        onRejected: KPromiseOnRejected<Unit>.() -> Unit
    ) = catchAll(
        semaphore = semaphore,
        optionalPromiseList = optionalPromiseArray.asList(),
        onRejected = onRejected
    )

    fun <R, O> cancelAll(
        requiredPromiseArray: Array<Promise<R>>,
        optionalPromiseArray: Array<Promise<O>>,
        semaphore: PromiseSemaphore? = null,
        onCancelled: () -> Unit
    ) = ps.ScopeCancelledBroadcast.cancelAll<Unit, R, O>(
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
    ) = ps.ScopeCancelledBroadcast.finallyAll<Unit, R, O>(
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

class KPromiseOnFulfilled<S, T>(
    val value: T,
    state: PromiseState<S>,
) : KPromiseScope<S>(state)

class KPromiseCompoundOnFulfilled<S, R, O>(
    val value: PromiseCompoundResult<R, O>,
    state: PromiseState<S>,
) : KPromiseScope<S>(state)

class KPromiseOnRejected<S>(
    val reason: Throwable,
    state: PromiseState<S>,
) : KPromiseScope<S>(state)

class KPromiseOnSettled(
    state: PromiseState<Any?>,
) : KPromiseScope<Any?>(state)

fun Job?.ToBroadcast(): PromiseCancelledBroadcast? = this?.let { parentJob ->
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
    PromiseStatefulJob { resolver, rejector, state ->
        KPromiseJob(
            resolver = resolver,
            rejector = rejector,
            state = state
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

private fun <R, O, S> (KPromiseCompoundOnFulfilled<S, R, O>.() -> Any?).toCompoundFulfilledListener() =
    PromiseStatefulCompoundFulfilledListener { value, cancelledBroadcast ->
        KPromiseCompoundOnFulfilled(value, cancelledBroadcast).this()
    }

private fun <S, R, O> PromiseCancelledBroadcast?.thenAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<S, R, O>.() -> Any?
): Promise<S> {
    return Promise.ThenAll(
        this,
        semaphore,
        onFulfilled.toCompoundFulfilledListener(),
        requiredPromiseList,
        optionalPromiseList
    )
}

private fun <S> (KPromiseOnRejected<S>.() -> Any?).toRejectedListener() =
    PromiseStatefulRejectedListener { reason, cancelledBroadcast ->
        KPromiseOnRejected(reason, cancelledBroadcast).this()
    }

private fun <S, R, O> PromiseCancelledBroadcast?.catchAll(
    semaphore: PromiseSemaphore? = null,
    requiredPromiseList: List<Promise<R>>? = null,
    optionalPromiseList: List<Promise<O>>? = null,
    onRejected: KPromiseOnRejected<S>.() -> Any?
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
    PromiseStatefulSettledListener { cancelledBroadcast ->
        KPromiseOnSettled(cancelledBroadcast as PromiseState<Any?>).this()
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
) = coroutineContext[Job].ToBroadcast().onceTask(
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
) = coroutineContext[Job].ToBroadcast().sharedTask(
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
) = coroutineContext[Job].ToBroadcast().timedTask(
    interval = interval,
    semaphore = semaphore,
    lifeTimes = lifeTimes,
    job = job,
)

fun <T> CoroutineScope.versionedTask(
    semaphore: PromiseSemaphore? = null,
    job: (KPromiseJob<T>.() -> Unit)? = null
) = coroutineContext[Job].ToBroadcast().versionedTask(
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
    coroutineContext[Job].ToBroadcast().promise(semaphore = semaphore, job = job)

fun CoroutineScope.process(semaphore: PromiseSemaphore? = null, job: KPromiseJob.KPromiseProcedure.() -> Unit) =
    promise(semaphore = semaphore) {
        KPromiseJob.KPromiseProcedure(this).job()
    }

fun <T> CoroutineScope.resolved(value: T) = coroutineContext[Job].ToBroadcast().resolved(value)
fun CoroutineScope.resolved() = resolved(Unit)
fun <T> CoroutineScope.rejected(reason: Throwable?) = coroutineContext[Job].ToBroadcast().rejected<T>(reason)
fun CoroutineScope.failed(reason: Throwable?) = rejected<Unit>(reason)
fun <T> CoroutineScope.cancelled() = coroutineContext[Job].ToBroadcast().cancelled<T>()
fun CoroutineScope.terminated() = cancelled<Unit>()

fun <S, R, O> CoroutineScope.thenAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<S, R, O>.() -> Any?
) = coroutineContext[Job].ToBroadcast().thenAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = optionalPromiseList,
    onFulfilled = onFulfilled
)

fun <R, O> CoroutineScope.thenAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<Unit, R, O>.() -> Unit
) = thenAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, R> CoroutineScope.thenAll(
    requiredPromiseList: List<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<S, R, Unit>.() -> Any?
) = thenAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = emptyList(),
    onFulfilled = onFulfilled
)

fun <R> CoroutineScope.thenAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseCompoundOnFulfilled<Unit, R, Unit>.() -> Unit
) = thenAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, O> CoroutineScope.thenAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseList: List<Promise<O>>,
    onFulfilled: KPromiseCompoundOnFulfilled<S, Unit, O>.() -> Any?
) = thenAll(
    semaphore = semaphore,
    requiredPromiseList = emptyList(),
    optionalPromiseList = optionalPromiseList,
    onFulfilled = onFulfilled
)

fun <O> CoroutineScope.thenAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onFulfilled: KPromiseCompoundOnFulfilled<Unit, Unit, O>.() -> Unit
) = thenAll(
    semaphore = semaphore,
    optionalPromiseList = optionalPromiseArray.asList(),
    onFulfilled = onFulfilled
)

fun <S, R, O> CoroutineScope.catchAll(
    requiredPromiseList: List<Promise<R>>,
    optionalPromiseList: List<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected<S>.() -> Any?
) = coroutineContext[Job].ToBroadcast().catchAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = optionalPromiseList,
    onRejected = onRejected
)

fun <R, O> CoroutineScope.catchAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected<Unit>.() -> Unit
) = catchAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    optionalPromiseList = optionalPromiseArray.asList(),
    onRejected = onRejected
)

fun <S, R> CoroutineScope.catchAll(
    requiredPromiseList: List<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected<S>.() -> Any?
) = catchAll<S, R, Unit>(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseList,
    optionalPromiseList = emptyList(),
    onRejected = onRejected
)

fun <R> CoroutineScope.catchAll(
    requiredPromiseArray: Array<Promise<R>>,
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected<Unit>.() -> Unit
) = catchAll(
    semaphore = semaphore,
    requiredPromiseList = requiredPromiseArray.asList(),
    onRejected = onRejected
)

fun <S, O> CoroutineScope.catchAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseList: List<Promise<O>>,
    onRejected: KPromiseOnRejected<S>.() -> Any?
) = catchAll<S, Unit, O>(
    semaphore = semaphore,
    requiredPromiseList = emptyList(),
    optionalPromiseList = optionalPromiseList,
    onRejected = onRejected
)

fun <O> CoroutineScope.catchAll(
    semaphore: PromiseSemaphore? = null,
    optionalPromiseArray: Array<Promise<O>>,
    onRejected: KPromiseOnRejected<Unit>.() -> Unit
) = catchAll(
    semaphore = semaphore,
    optionalPromiseList = optionalPromiseArray.asList(),
    onRejected = onRejected
)

fun <R, O> CoroutineScope.cancelAll(
    requiredPromiseArray: Array<Promise<R>>,
    optionalPromiseArray: Array<Promise<O>>,
    semaphore: PromiseSemaphore? = null,
    onCancelled: () -> Unit
) = coroutineContext[Job].ToBroadcast().cancelAll<Unit, R, O>(
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
) = coroutineContext[Job].ToBroadcast().finallyAll<Unit, R, O>(
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

fun CoroutineScope.delay(d: Duration) = coroutineContext[Job].ToBroadcast().delay(d)

open class KPromiseJob<S>(
    private val resolver: PromiseResolver<S>,
    private val rejector: PromiseRejector,
    private val state: PromiseState<S>,
) : KPromiseScope<S>(state) {
    class KPromiseProcedure(
        job: KPromiseJob<Unit>,
    ) : KPromiseJob<Unit>(job.resolver, job.rejector, job.state) {
        fun resolve() = resolve(Unit)
    }

    fun resolve(value: S) = resolver.ResolveValue(value)
    fun resolve(promise: Promise<S>) = resolver.ResolvePromise(promise)
    fun reject(reason: Throwable?) = rejector.Reject(reason)
}

private fun <T, S> (KPromiseOnFulfilled<S, T>.() -> Any?).toFulfilledListener() =
    PromiseStatefulFulfilledListener<T, S> { value, cancelledBroadcast ->
        KPromiseOnFulfilled(value, cancelledBroadcast).this()
    }

fun <S, T> Promise<T>.then(
    semaphore: PromiseSemaphore? = null,
    onFulfilled: KPromiseOnFulfilled<S, T>.() -> Any?
): Promise<S> {
    return Then(semaphore, onFulfilled.toFulfilledListener())
}

fun <T> Promise<T>.next(semaphore: PromiseSemaphore? = null, onFulfilled: KPromiseOnFulfilled<Unit, T>.() -> Unit) =
    then(semaphore = semaphore, onFulfilled = onFulfilled)

fun <S, T> Promise<T>.catch(
    semaphore: PromiseSemaphore? = null,
    onRejected: KPromiseOnRejected<S>.() -> Any?
): Promise<S> {
    return Catch(semaphore, onRejected.toRejectedListener())
}

fun <T> Promise<T>.error(semaphore: PromiseSemaphore? = null, onRejected: KPromiseOnRejected<Unit>.() -> Unit) =
    catch(semaphore = semaphore, onRejected = onRejected)


fun <T> Promise<T>.cancel(semaphore: PromiseSemaphore? = null, onCancelled: () -> Unit): Promise<Unit> {
    return ForCancel(semaphore, onCancelled.toCancelledListener())
}

private fun (KPromiseOnSettled.() -> Any?).toSettledListenerUnit() =
    PromiseStatefulSettledListener { cancelledBroadcast ->
        KPromiseOnSettled(cancelledBroadcast as PromiseState<Any?>).this() as? Promise<*>
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