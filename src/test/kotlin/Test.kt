import kotlinx.coroutines.GlobalScope
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
}