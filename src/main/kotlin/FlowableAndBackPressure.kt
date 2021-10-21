import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

class FlowableAndBackPressure {

    /**
        Observable의 경우 생산자 역할을 하고, Observer는 소비자 역할을 합니다.
        Observerable에서는 생산과, 소비가 각각 따로 놀기 때문에 만약 Observer의 데이터 처리속도가 느린경우 생산은 대량으로 진행되고, 소비는 천천히 일어나는 상황을 마딱드릴 수 있습니다.
        따라서 소비자의 상황에 따라 생산되는 데이터를 조절하기 위해 사용하는 채널을 backpressure라고 하며, Flowable은 backpressure를 지원합니다.
     */

    fun run() {
        runBlocking { window() }
    }

    /**
        Flowable은 Observable에서 backpressure를 지원하는 버전이라고 볼수 있습니다.
        Flowable은 내부적으로 buffer(최대 128개)지원하여, 생산 속도를 제어할 수 있습니다.
        Flowable은 기본적으로 Observable의 생성과 동일한 API를 사용합니다.
     */
    fun create100MSUse200MS() = runBlocking<Unit> {
        val observable = Observable.range(1, 10)
        observable.map {
            val str = "Mapping item $it"
            runBlocking { delay(100) }
            println("$str - ${Thread.currentThread().name}")
            str
        }.observeOn(Schedulers.computation()) // observeOn을 이용하여 subscriber가 실행되는 thread를 worker thread로 변경했습니다.
            .subscribe({
                println("Received $it - ${Thread.currentThread().name}")
                runBlocking { delay(200) }
            })
        delay(1000)
    }

    /**
        생산이 128개가 되면 생산을 멈추고, 소비를 시작 -> 96개가 버퍼에서 소비되는 순간 다시 생산이 시작되면서 생산 속도를 조절함
        Flowable은 생산속도를 조절함으로써 memory 에러를 방지할 수 있지만 속도가 그만큼 느려짐
            - Flowable은 생산의 갯수가 수천, 수만개 정도로 많을때 사용하고 그렇지 않은 경우는 observable을 사용하면 된다
     */
    fun flowable() = runBlocking{
        val flowable = Flowable.range(1, 150)
        flowable.map {
            println("Mapping item $it")
            it
        }.observeOn(Schedulers.computation())
            .subscribe({
                println("Received item $it")
                runBlocking { delay(10) }
            })

        delay(10000)
    }

    class MySubscriber(var job: Job?): Subscriber<Int> {
        override fun onSubscribe(s: Subscription?) {
            //s?.request(Long.MAX_VALUE)
            s?.request(200)
        }

        override fun onNext(t: Int?) {
            runBlocking { delay(50) }
            println("onNext(): $t - ${Thread.currentThread().name}")
        }

        override fun onError(t: Throwable?) {
            println("Error !!!!")
            t?.printStackTrace()
            job?.cancel()
        }

        override fun onComplete() {
            println("onComplete()")
            job?.cancel()
        }
    }

    /**
        Subscriber
        Flowable은 observer 대신에 backpressure을 지원하는 Subscriber를 사용합니다
        Override 해야하는 Method은 Observer와 동일합니다만 request() 함수를 이용하여 수신 할 데이터의 개수를 요청해야합니다.
        request() 를 사용하지않으면 소비량이 없기 때문에, 더이상 생산을 하지 않고 대기하다 종료됨
     */

    fun subscriber() = runBlocking {
        Flowable.range(1, 150)
            .map {
                println("Mapping item $it - ${Thread.currentThread().name}")
                it
            }
            .observeOn(Schedulers.computation())
            .subscribe(MySubscriber(null))

        delay(5000)
    }

    /**
        BackpressureStrategy
            - BUFFER
                - 무제한 버퍼를 제공합니다 ( 기본 값인 128개가 아님 ) { 내부적으로는 128개에서 32개가 남을떄까지 계속 생성하고 초기화하여 다시 사용하는듯, 200개가 생성하면서 소비됨 }
                - 따라서 생산하는 데이터 개수가 많고, 수신하여 처리하는쪽이 너무 느릴경우 OutOfMemory가 발생할 수 있다
                - Flowable은 자신의 속도대로 모두 방출하고 수신자 역시 자신의 처리 속도에 따라 모든 데이터를 처리함
            - ERROR
                - 소비쪽에서 생산을 따라잡을 수 없는 경우 ERROR를 발생시킵니다.
            - DROP
                - 수신자가 처리중에 생산자로 부터 전달을 받으면 해당 데이터는 버립니다.
                - 개념상 10개를 생성하더라도 1개를 수신중이라면 나머지 9개는 버려지는 개념이나, 여기도 기본 버퍼(128개)가 존재합니다.
                - 따라서 기본 버퍼개수 128개까지는 버퍼에 쌓여있어 순차적으로 수신자가 처리하나, 버퍼에 담기지 못하는 129개째 부터 수신중에 생산된 데이터 이므로 버려집니다.
                - 수신자의 동작 기준으로 데이터를 처리하고, 처리중에 생산된 데이터는 버려지며, 수신 처리가 끝나면 그 다음으로 수신하는 데이터를 처리합니다.
                - 데이터 생산은 200개 모두 되었으나, 200개를 다 받을때 까지도 수신단에서 아직 처리를 못했으므로, 버퍼 크기인 128개까지만 저장되고 그 이후부터는 버려집니다.
                - 따라서 수신측에서는 버퍼에 저장된 128개를 처리한 이후에 들어오는 데이터가 없기 때문에 (이미 방출이 끝난상태) 추가 처리 없이 종료 됩니다.
            - LATEST
                - Drop과 유사하게 수신 처리중에 받은 데이터는 무시됩니다. 다만 무시할때 마지막 배출된 값을 저장하여 최종값을 유지하고 있습니다.
                - 수신쪽의 처리가 완료되면 무시된 값들중 최종값 하나를 전달한 후 이후에 배출되는 값을 수신 받습니다.
            - MISSING
                - Missing값은 기본적으로 Flowable에서 제공하는 backpressure를 사용하지 않겠다는 의미 입니다.
                - 다만 위에서 언급한 strategy와 동일하게 동작시키기 위해 추가적인 operator를 제공합니다.
                    - onBackpressureBuffer()
                        - MISSING 과 함께 사용한다면 BackPressureStrategy.BUFFER를 사용한 것과 동일하게 적용됨, 인자 값으로 버퍼 크기를 한정할 수 있음
                    - onBackpressureDrop()
                    - onBackpressureLatest()
     */
    fun backpressureStrategyBuffer() = runBlocking {
        val flowable = Flowable.create<Int>({
            for(i in 1..200) {
                println("send item $i")
                it.onNext(i)
                runBlocking { delay(10) }
            }
            it.onComplete()
        }, BackpressureStrategy.MISSING) // BUFFER, ERROR, DROP, LATEST

        val waitingJob = launch {
            delay(Long.MAX_VALUE)
        }

        flowable
            .onBackpressureBuffer(50)
            .onBackpressureDrop{println("Drop item $it")}
            .observeOn(Schedulers.computation())
            .subscribe(MySubscriber(waitingJob)) // waitingJob
//        delay(1000)
    }

    /**
        ConnectableFLowable
        - 이전에 사용했던 flowable은 cold observable로 등록되는 순간 배출됩니다.
        - Observable에서 hot observable을 ConnectableObservable로 제공하듯이 동일한 방법으로 hot flowable을 만들 수 있습니다.
     */

    fun hotFlowable() = runBlocking {
        val connectableFlowable = Flowable.interval(100, TimeUnit.MILLISECONDS).publish()
        connectableFlowable.subscribeOn(Schedulers.computation())
            .subscribe { println("Subscription 1: $it") }

        connectableFlowable.connect()
        delay(500)

        connectableFlowable.subscribeOn(Schedulers.io())
            .subscribe{ println("Subscription 2 $it") }

        delay(500)
    }

    /**
        Processor
        - Processor는 backpressure를 지원하는 Subject입니다.
        - 기본적으로 Subject의 모든 타입에 대해 Processor가 존재합니다.
        - Subject는 하기 링크에서 확인 가능합니다.
     */
    fun processor() = runBlocking{
        val connectableFlowable = Flowable.interval(100, TimeUnit.MILLISECONDS)
        val publishProcess = PublishProcessor.create<Long>()

        publishProcess.subscribe{ println("processor 1- $it") }
        connectableFlowable.subscribe(publishProcess)

        delay(300)

        publishProcess.subscribe{ println("prosessor 2- $it") }

        delay(300)
    }

    /**
        Throttling
        - 연속적인 emission을 수신측에서 다 처리할 수 없는 경우 특정 배출만 획득하고 나머지는 버리도록 하는 작업입니다.

        sample -> 특정한 시간 동안 가장 최근에 발행된 데이터만 걸러줌, 해당 시간에는 아무리 많은 데이터가 들어와도 해당 구간의 마지막 데이터만 발행 가능하고 나머진 무시함
               -> 정해진 단위 시간내에 배출된 마지막 값을 단위시간이 끝날때 마다 주기적으로 획득
                ex) observable.sample(300, TimeUnit.MILLISECONDS) -> 아래의 코드에서 100ms마다 나오는 데이터를 300ms마다 마지막 값을 출력
                ex) observable.throttleFirst(300, TimeUnit.MILLISECONDS) -> 100ms마다 나오는 데이터를 300ms마다 첫번째 값을 출력
        debounce -> 정해진 단위시간보다 더 긴 간격으로 배출되는 아이템만 획득
                 -> debounce간격은 300ms이나, 데이터는 100ms로 배출되기 때문에, 배출 간격이 300ms를 초과하는 데이터가 없기 때문에 아무것도 나오지 않습니다
                 -> debounce간격을 90ms로 한다면 데이터가 90ms보다 큰 100ms로 배출되므로 모든 데이터가 출력됩니다.
     */

    fun throttling() = runBlocking{
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS) // observable 이 100ms 마다 연속된 데이터를 생성
        val time = System.currentTimeMillis()
        observable.debounce(90, TimeUnit.MILLISECONDS)
            .subscribe{
                println("Time: ${System.currentTimeMillis()-time} : $it")
            }
        delay(1000)
    }

    /**
        Buffers and Windows
        - throttling은 연속적인 데이터의 일부를 버리고 규칙에 따라 특정 데이터만 취득하는 반면, buffer나 window는 단위시간동안 해당 데이터를 모아서 수신받는 형태로
          데이터의 손실 없이 배출 간격을 좀 더 늦출 수 있습니다.
        - 느리게 동작하는 소비자는 이렇게 모아진 데이터중에서 하나를 골라서 처리하거나, 모아서 병합하거나등의 작업을 따로 구현할 수 있습니다.

        buffers
        - 조건에 따라서 배출되는 데이터를 묶어서 collection 형태로 반환합니다.
        - 인자로 묶을 개수를 넣어줍니다.

        Time interval
        - 또는 특정 시간 간격으로 묶는것도 가능합니다. (시간때문에 그런가 잘못나올 가능성? 이 있는거같습니다.) 밑의 예제에서 배출되는 타입은 MutableList<Long> 입니다
        - 맞는거같습니다. range로 생성한 Flowable을 3개로 window시켰더니 제대로 나옵니다!
     */

    fun buffers() = runBlocking {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val intervalTime = Observable.interval(300, TimeUnit.MILLISECONDS)

        val time = System.currentTimeMillis()
        observable.buffer(intervalTime)
            .subscribe{
                println("Time: ${System.currentTimeMillis()-time} - $it")
            }
        delay(1000)
    }

    /**
        window
        - buffers와 매우 유사하나, 출력 값이 Observable로 반환됩니다.
        - 즉, 특정 단위로 묶어 sub-Observable를 배출합니다
        - 만약 Flowable 이었다면, 출력은 Flowable이 됩니다.
     */

    fun window() = runBlocking {
        val flowable = Flowable.interval(100, TimeUnit.MILLISECONDS)
        val intervalTime = Flowable.interval(300, TimeUnit.MILLISECONDS)
//        val flowable = Flowable.range(1,10)

        flowable.window(intervalTime).subscribe{
            it.subscribe {
                print("$it ")
            }
            println("")
        }
        delay(1000)
    }
}