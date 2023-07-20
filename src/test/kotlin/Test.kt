import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class Test {
    @Test
    fun test() {
        GlobalScope.process {
            println("please wait...")
            delay(4.seconds).next {
                println("hello, world")
                resolve()
            }
        }.Await()
        GlobalScope.delay(1.seconds).then<Unit, Unit> {
            return@then delay(1.seconds)
        }.Await()
        println("finished")
        GlobalScope.delay(1.seconds).then<Unit, Unit> {
            return@then delay(1.seconds)
        }.Await()
        println("end")
        GlobalScope.delay(1.seconds).then<Unit, Unit> {
            return@then delay(1.seconds)
        }.Await()
    }

    @Test
    fun testOnceTask() {
        GlobalScope.onceProcess {
            println("origin job")
            resolve()
        }.process {
            println("new job")
            resolve()
        }.finally {
            println("end")
        }.Await()
    }

    @Test
    fun testJob() {
        println("runBlocking 开始")
        runBlocking {
            println("延时之前")
            delay(5.seconds).next {
                println("hello, world")
            }.cancel {
                println("外部检测到取消")
                if (!isActive) {
                    println("取消时取消")
                }
            }.cancel {
                println("外部检测到取消2")
                if (!isActive) {
                    println("取消时取消2")
                }
            }
        }
        println("runBlocking 结束")
        Thread.sleep(3500)
    }
}