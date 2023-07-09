import kotlinx.coroutines.GlobalScope
import org.junit.jupiter.api.Test

class Test {
    @Test
    fun test() {
        GlobalScope.process {
            println("please wait...")
            Thread.sleep(5000)
            println("hello, world")
            resolve()
        }.Await()
        println("finished")
        Thread.sleep(5000)
        println("end")
    }
}