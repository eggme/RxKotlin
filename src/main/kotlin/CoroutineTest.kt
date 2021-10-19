import kotlinx.coroutines.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class CoroutineTest {
    /**
        592p 코루틴 컨텍스트와 디스패처에서 종료
     */
    fun now() = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.MILLIS)

    fun log(msg: String) = println("${now()} : ${Thread.currentThread()} : $msg")

    /**
        runBlocking -> Blocking 시키고 다음 작업을 처리한다, 작업을 끝나면 동작 시킨 부분의 Blocking을 해제하고 돌아간다, MainThread 를 멈추고 해당 동작을 실행하는것 같습니다.
        GlobalScope -> 앱 동작 간 단 하나의 Scope을 활용한다
            확실한건 MainThread는 아닙니다. DefaultDispatcher-worker-1라고 쓰레드가 따로 있는 것 같습니다
        CoroutineScope -> LifeCycle을 처리하기 쉽고, 생성과 동시에 사용이 가능하다
     */

    fun launchInGlobalScope() {
//        runBlocking{
//            launch {
//                log("coroutine started!! Coroutine Scope!!")
//            }
//        }
        GlobalScope.launch {
            log("coroutine started")
        }
    }

    fun fibonacciFun() {
        val fibonacciSeries = sequence<Int> {
            var a = 0
            var b = 1
            yield(a)
            yield(b)

            while (true) {
                val c = a + b
                yield(c)
                a = b
                b = c
            }
        }
        println(fibonacciSeries.take(10) join ", ")
    }

    infix fun Sequence<Int>.join(str: String): String {
        return this.joinToString(str)
    }

    /**
        async는 사실상 launch와 같은 일을 한다. 유일한 차이는 launch가 Job를 반환하는 반면 async는 Deffered를 반환한다는 점이다.
        심지어 Deffered는 Job을 상속한 클래스이기 때문에 launch 대신 async를 사용해도 항상 아무 문제가 없다.
        Deffered안에는 await() 함수가 정의돼 있다는 점이고, Deffered 코루틴이 계산을 하고 돌려주는 바로 값의 타입이다
        async는 코드 블록을 비동기로 실행할 수 있고, async가 반환하는 Deffered의 await을 사용해서 코루틴이 결과 값을 내놓을 때까지 기다렸다가 결과 값을 얻어낼 수 있다.
     */
    /**
        launch, async -> 즉시 처리, launch는 반환 값이 없음, async는 반환 값이 있어서 await() 호출 시 반환 값 제어 가능
        runBlocking -> 즉시 처리 x
     */
    fun sumAll() {
        runBlocking {
            val d1 = async {
                delay(1000L)
                log("d1 finished")
                1 }
            log("after async(d1)")
            val d2 = async {
                delay(2000L)
                log("d2 finished")
                2 }
            log("after async(d2)")
            val d3 = async {
                delay(3000L)
                log("d3 finished")
                3 }
            log("after async(d3)")

            log("1+2+3 = ${d1.await() + d2.await() + d3.await()}")
            log("after await all & add")
        }
    }

    fun sumAllRunBlocking() {
        runBlocking {
            runBlocking { delay(1000L); log("d1 finished") }
            log("after async(d1)")
            runBlocking { delay(2000L); log("d2 finished") }
            log("after async(d2)")
            runBlocking { delay(3000L); log("d3 finished") }
            log("after async(d3)")

            log("after await all & add")

        }
    }

    /**
        delay()를 사용한 코루틴은 그 시간이 지날 때까지 다른 코투린에게 실행을 양보한다.
        밑의 코드에서 delay(1000L) 대신 yield()를 쓰면 차례대로 1,2,3,4,5,6이 표시될 것이다.
     */
    fun yieldExample() {
        runBlocking {
            launch {
                log("1")
                yield()
                log("3")
                yield()
                log("5")
            }
            log("after first launch")
            launch {
                log("2")
                delay(1000L)
                log("4")
                delay(1000L)
                log("6")
            }
            log("after second launch")
        }
    }
}