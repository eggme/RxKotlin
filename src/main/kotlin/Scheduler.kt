import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class Scheduler {

    fun run(){
        subscribeOn()
    }

    /**
        Observable은 기본적으로 subscribe 하는 Thread에서 동작합니다
        interval이나, timer로 Observable을 생성하거나, delay, operator를 사용하는 경우가 아니고서는 subscribe가 완료될때까지 block됩니다.
        즉 subscribe{..} 블럭에서 모든 데이터를 수신받고 처리해야하만 해당 블럭을 벗어날 수 있는거죠.

        아래 함수의 결과를 보고 판달할 수 있는 것들은 아래와 같습니다.
            - bservable과 Subscribe는 subscribe를 수행한 실행한 thread에서 수행된다.
            - 모든 데이터의 수신이 완료 되어야만 (subscribe {..} 블럭이 끝나야만) 다음 코드 라인이 수행된다.
            - 즉 subscribe {..} 블럭이 끝날때까지 thread는 blocking 된다.

        Schedulers
        - Scheduler.io()
            - 파일 / network IO 작업을 할때 사용하는 용도입니다.
            - 내부적으로 cachedPool을 사용하기 때문에 thread가 동시에 계속 늘어나면서 생성될수 있으며, 유휴 thread가 있을 경우 재활용됩니다.
        - Scheduler.computation()
            - cpu 의존적인 계산을 수행을 위한 thread pool을 사용합니다.
            - 코어개수만큼 thread pool을 만들어 사용합니다. (내부적으로 ForkJoinPool을 쓰는게 아닐지 예상해 봅니다..)
        - Scheduler.newThread()
            - new Thread() 처럼 새로운 Thread를 하나 만들어 사용합니다.
        - Scheduler.single()
            - singleThreadPool을 사용하므로, 해당 Scheduler로 여러 작업 수행시 Queuing 되어 순서가 보장됩니다.
        - Scheduler.trampoline()
            - 호출을 수행한 thread를 이용하여 수행합니다.
            - 호출한 스레드 역시 단일 thread 이므로 여러 작업 요청시 Queuing 되어 순서가 보장됩니다.
            - 단, 호출한 스레드를 사용하기 때문에 queuing된 모든 작업이 끝나야만 다음 코드라인이 수행될 수 있습니다.
        - Scheduler.from()
            - Executor를 전달하여 새로운 Scheduler를 생성할 수 있습니다.
            - AndroidSchedulers.mainThread()
            - RxAndroid 사용시 mainThread()에서 수행하기 위한 Scheduler 입니다.
     */

    fun schedulerTest() = runBlocking{
        val ob = Observable.just(1,2,3)
            .map {
                println("map: $it - ${Thread.currentThread().name}")
                it
            }

        ob.subscribe{ println("First: $it - ${Thread.currentThread().name}") }
        println("----------------------")

        Thread { ob.subscribe() { println("Second: $it - ${Thread.currentThread().name}")} }.start()

        delay(1000)
    }

    fun ioAndComputation() = runBlocking{
        val obj1 = Observable.range(1,3)
        println("start Schedules.io()")

        obj1.subscribeOn(Schedulers.io()).subscribe{
            runBlocking { delay(100) }
            println("$it: Schedulers.io - ${Thread.currentThread().name}")
        }
        println("start Schedules.computation()")
        obj1.subscribeOn(Schedulers.computation()).subscribe{
            runBlocking { delay(100) }
            println("$it: Schedulers.computation - ${Thread.currentThread().name}")
        }

        println("done")
        delay(500)
    }

    /**
        Schedulers.single() vsSchedulers.trampoline()
        - 둘다 single 스레드를 사용하여 순서를 보장하지만, single()은 worker thread를 하나 만들어 해당 thread queue에 작업을 넘겨주는 방식이고,
          trampoline()은 호출한 thread의 queue에 넘겨주는 방식입니다.
        - 둘다 순서를 보장하지만, single은 single을 사용하는 Observable간의 순서만 보장하고, trampoline은 trampoline을 사용한 observable을 포함한 다른 코드에 까지 영향을 줍니다.
        - single()은 worker thread를 하나 만들어 해당 thread queue에 작업을 넘겨주는 방식이고, trampoline()은 호출한 thread의 queue에 넘겨주는 방식 ex) main thread로 넘겨줌
     */

    fun singleVsTrampoline() = runBlocking{
        val ob = Observable.just(1,2,3)
        println("start - Observable#1")
        ob.subscribeOn(Schedulers.single())
            .subscribe{
                runBlocking { delay(100) }
                println("$it: Observable#1 - ${Thread.currentThread().name}")
            }

        println("start - Observable#2")
        ob.subscribeOn(Schedulers.single())
            .subscribe{
                runBlocking { delay(100) }
                println("$it: Observable#2 - ${Thread.currentThread().name}")
            }

        println("done")

        delay(1000)
    }

    fun intervalNotEffect() = runBlocking{
        println("start")
        val ob = Observable.interval(100, TimeUnit.MILLISECONDS)

        ob.subscribeOn(Schedulers.trampoline())
            .subscribe{
                println("$it")
            }
        println("end")
    }

    /**
        subscribeOn vs observeOn
        - subscribeOn은 어느 위치에서 선언되든지 Observabe과 Observer 모두 특정 scheduler에서 동작하도록 지정합니다.
          데이터의 생산과 소비를 동일한 스케줄러를 사용하도록 지정해 줍니다.
     */

    fun subscribeOnVsObserveOn() = runBlocking{
        Observable.range(1,3)
            .map {
                println("mapping - ${Thread.currentThread().name}")
                it
            }
            .subscribeOn(Schedulers.io())
            .subscribe{
                println("subscribe $it - ${Thread.currentThread().name}")
            }

        delay(100)
    }

    /**
        observeOn은 선언부분 이하의 downstream이 사용할 scheduler를 지정합니다.
        observeOn을 이용하여 각각의 작업을 지정한 Scheduler에서 수행하도록 할 수 있습니다.
        연산자 하나로 손쉽게 context를 switching 할 수 있습니다.

        subscribeOn과 observeOn가 겹쳤을 때
        - subscribeOn은 어디에 선언되든 Observable과 subscribe가 동작되는 전체 Scheduler를 지정한다.
        - subscribeOn이 여러개 선언되면, 가장 먼저 선언된 Scheduler로 동작된다.
        - subscribeOn과 observeOn이 혼용될 경우 subscribeOn은 observeOn 선언 직전 부분의 코드를 실행하고, observeOn 선언 이후부터는 observeOn에서 선언된 Scheduler로 동작된다.

     */
    fun subscribeOn() = runBlocking{
        Observable.just(1)
            .observeOn(Schedulers.io())
            .map {
                println("mapping#1 - ${Thread.currentThread().name}")
                it
            }
            .observeOn(Schedulers.computation())
            .map {
                println("mapping#2 - ${Thread.currentThread().name}")
                it
            }
            .observeOn(Schedulers.single())
            .subscribe{
                println("subscribe $it - ${Thread.currentThread().name}")
            }

        delay(100)
    }
}