import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.util.concurrent.TimeUnit

class Reactive {

    val observer : Observer<Any> = object : Observer<Any> {
        override fun onSubscribe(d: Disposable) = println("onSubscribe $d")

        override fun onNext(t: Any) = println("onNext $t")

        override fun onError(e: Throwable) = println("onError $e")

        override fun onComplete() = println("onComplete")
    }

    fun listObservable(){
        runBlocking {
            async {
                val observable: Observable<Any> = listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f).toObservable()
                yield()
                observable.subscribe(observer)
                println("observable finished")
            }
            async {
                val observableList: Observable<List<Any>> = Observable.just(
                    listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f),
                    listOf("List with Single Item"),
                    listOf(1,2,3,4,5,6)
                )
                observableList.subscribe(observer)
            }
        }
    }

    /**
        Connected Observable 예제
     */
    fun connectableObservable() {
        val connectableObservable = Observable.interval(100, TimeUnit.MILLISECONDS).publish()

        connectableObservable.subscribe { println("Subscription 1: $it") }
        connectableObservable.subscribe { println("Subscription 2: $it") }

        connectableObservable.connect()

        runBlocking { delay(500) }

        connectableObservable.subscribe { println("Subscription 3: $it") }
        runBlocking { delay(500) }
    }

    fun observableDisposableTest(){
        runBlocking {
            val observable: Observable<Long> = Observable.interval(100, TimeUnit.MILLISECONDS)
            val observer: Observer<Long> = object: Observer<Long>{
                private lateinit var disposable: Disposable
                override fun onSubscribe(d: Disposable) {
                    d.also { this.disposable = it }
                }
                override fun onNext(t: Long) {
                    println("Received $t")
                    if(t >= 20 && !disposable.isDisposed){
                        disposable.dispose()
                        println("Dispose")
                    }
                }
                override fun onError(e: Throwable) = println("Error ${e.message}")
                override fun onComplete() = println("Complete")
            }

            observable.subscribe(observer)
            delay(1500)
        }
    }

    /**
        이 방식은 사용자가 지정한 데이터 구조를 사용하거나, 내보내는 값을 제어하려고 할 때 유용한 방식이다
     */
    fun createObservable() {
        val observable: Observable<String> = Observable.create {
            it.onNext("Emit 1")
            it.onNext("Emit 2")
            it.onNext("Emit 3")
            it.onNext("Emit 4")
            it.onComplete()
        }

        observable.subscribe(observer)

        val observable2: Observable<Int> = Observable.create {
            it.onNext(1)
            it.onNext(2)
            it.onNext(3)
            it.onNext(4)
            it.onNext(99)
            it.onComplete()
        }

        observable2.subscribe(observer)
    }

    fun reactive(){
        //listObservable()
        //observableDisposableTest()
        connectableObservable()
    }
}