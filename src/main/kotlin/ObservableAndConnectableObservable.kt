import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ObservableAndConnectableObservable {

    fun run() {
        /**
        Observer은 Consumer가 소비하는 값을 생성하는 역할을 함
        Consumer들은 Observable에 구독을 신청하고, Observable은 값을 생성한 후에 Consumer들에게 push 방식으로 값을 전달합니다.
         */
        val observer: Observer<Any> = object : Observer<Any> {
            /**
            구독을 신청하면 호출해줌
            - 이 때 넘어오는 Disposable 객체는 Observer가 구독을 해제할 때 사용함
             */
            override fun onSubscribe(d: Disposable) {
                println("onSubscribe() - $d")
            }

            override fun onNext(t: Any) {
                println("onNext() - $t")
            }

            override fun onError(e: Throwable) {
                println("onError() - ${e.message}")
            }

            override fun onComplete() {
                println("onComplete()")
            }
        }

        val observable1 : Observable<Any> = Observable.create{
            it.onNext(1)
            it.onNext(2)
            it.onComplete()
        }

        val observable2 : Observable<Any> = Observable.create{
            it.onNext(1)
            it.onNext(2)
            //it.onComplete()
            it.onError(Exception("Wow!! Exception"))
        }


        observable1.subscribe(observer)
        observable2.subscribe(observer)

        println("Iterator ----------------------------")
        val list = listOf("One", 2 , "Three", "Four", 4.5, "Five", 6.0f)
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            println(iterator.next())
        }
        println("RxKotlin ----------------------------")
        val observerList: List<Any> = listOf("One", 2 , "Three", "Four", 4.5, "Five", 6.0f)
        val observable : Observable<Any> = observerList.toObservable()

        observable.subscribeBy(
            onNext = { println(it) },
            onError = { it.printStackTrace() },
            onComplete = { println("Done!!!") }
        )
        nativeIterator()
        println("\\----ㅇ0ㅇ----/ nativeIterator")
        reactiveCode()
        println("\\----ㅇ0ㅇ----/ reactiveCode")
        fromXXX()
        println("\\----ㅇ0ㅇ----/ fromXXX")
        just()
        println("\\----ㅇ0ㅇ----/ just")
        range()
        println("\\----ㅇ0ㅇ----/ range")
        empty()
        println("\\----ㅇ0ㅇ----/ empty")
        connectableObservable()
        println("\\----ㅇ0ㅇ----/ connectableObservable")
        connectableObservableInterval()
        println("\\----ㅇ0ㅇ----/ connectableObservableInterval")
        /**
        Observable에서 방출하는 값을 받기 위해서는 subscribe() 함수를 이용해 등록해야한다.
        위 예제에서는 Observer instance를 등록했으나, 각각의 메서드를 필요한것만 따로 등록할 수 있습니다.
         */
    }


    fun nativeIterator() {
        var number = 4
        var isEven = isEven(number)
        println("The number is " + if(isEven) "Even" else "Odd")
        number = 9
        isEven = isEven(number)
        println("The number is " + if(isEven) "Even" else "Odd")
    }

    fun isEven(n: Int): Boolean = (n % 2 == 0)


    fun reactiveCode() {
        val subject: Subject<Int> = PublishSubject.create()

        subject.map { isEven(it) }.subscribe {
            println("The number is ${(if (it) "Even" else "Odd")}")
        }
        subject.onNext(4)
        subject.onNext(9)
    }

    fun fromXXX() {
        val list = listOf(1,2,3)
        val listOb = Observable.fromIterable(list)
        val call = Callable<Int>{ 4 }
        val callOb = Observable.fromCallable(call)
        val future = object : Future<Int> {
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
            override fun isCancelled(): Boolean = false
            override fun isDone(): Boolean = true
            override fun get(): Int = 5
            override fun get(timeout: Long, unit: TimeUnit): Int = 6
        }

        val futureOb = Observable.fromFuture(future)


        listOb.subscribe(makeObserver(Int))
        callOb.subscribe(makeObserver(Int))
        futureOb.subscribe(makeObserver(Int))
    }
    /**
    Observer은 Consumer가 소비하는 값을 생성하는 역할을 함
    Consumer들은 Observable에 구독을 신청하고, Observable은 값을 생성한 후에 Consumer들에게 push 방식으로 값을 전달합니다.
     */

    fun <T> makeObserver(type: T) : Observer<T> {
        return object : Observer<T> {
            /**
            구독을 신청하면 호출해줌
            - 이 때 넘어오는 Disposable 객체는 Observer가 구독을 해제할 때 사용함
             */
            lateinit var disposable: Disposable
            override fun onSubscribe(d: Disposable) {
                disposable = d
                println("onSubscribe() - ${d::class}")
            }
            override fun onNext(a: T) {
                if(a is Int){
                    if(a >= 5 && !disposable.isDisposed) disposable.dispose()
                }
                println("onNext() - $a")
            }
            override fun onError(e: Throwable) = println("onError() - $")
            override fun onComplete() = println("OnComplete()")
        }
    }

    /**
    받은 인자를 그대로 전달하는 just
    list를 받든 map을 받는 객체 자체를 전달하며, 여러개를 전달하려면 각각의 인자로 넣어서 호출해야 합니다.
     */
    fun just() {
        val list = listOf(1, 2, 3)
        val num = 3
        val str = "wow!"
        val map = mapOf(1 to "one", 2 to "two")
        val justOb = Observable.just(list ,num, str, map)
        justOb.subscribe(makeObserver(Any()))
    }

    /**
    특정 범위만큼 수를 생성하여 전달합니다
     */
    fun range() {
        Observable.range(1, 3).subscribe(makeObserver(Int))
    }

    /**
    아무값을 전달하지는 않지만 onComplete를 호출한다
     */
    fun empty() {
        Observable.empty<Any>().subscribe(makeObserver(Any()))

    }

    /**
    Hot & Cold Observable
    앞서 언급된 observable들은 subscribe를 신청하면 가지고 있는 데이터를 순서에 맞춰 전부 내보내 줍니다
    여러번 subscribe를 하더라도 순서에 맞춰서 동일한 데이터를 내어줍니다
    즉, Observable의 데이터는 subscribe해서 소모되는게 아니라, 계속 저장되어 있다가 구족자가 추가될때마다 데이터 전부를 내어주도록 되어 있습니다
    이를 Cold Observable이라고 합니다

    Cold Observable
    - subscribe가 호출되면 데이터를 배출하기 시작한다. (Observable이 배출하는 동작을 시작한다)
    - 처음부터 모든 데이터가 순서대로 배출된다
    - 구독할때마다 동일한 데이터가 동일한 순서로 배출된다
    Hot Observable
    - subscribe와 상관없이 데이터를 배출한다
    - 구독시점부터 데이터를 전달 받으며, 구독신청전의 데이터는 받을 수 없다
    - Event를 전달받는 형태로 사용함
     */
    fun connectableObservable() {
        /**
        publish() 를 통하여 hot observable로 변경합니다
        subscribe()를 호출하였으나, onNext()가 호출되지 않았습니다.
        1번과 2번 구독자를 등록 후 Observable의 connect()를 호출하면 그때서야 데이터가 배출됩니다.
        또한 배출이 완료된 이후에 등록된 3번은 데이터를 하나도 전달받지 못합니다
         */
        val connectableObservable = (1..10).toObservable().publish()
        // 1번 구독자 등록
        connectableObservable.subscribe { println("first subscriber : $it") }
        println("Add first subscriber")

        // 2번 구독자 등록
        connectableObservable.map { "second subscriber : $it" }.subscribe { println(it) }
        println("Add second subscriber")

        // observable connect()
        connectableObservable.connect()

        // 3번 구독자 등록
        connectableObservable.subscribe{ println("Subscription 3 : $it") }
    }

    fun connectableObservableInterval() {
        val connectableObservable = Observable.interval(100, TimeUnit.MILLISECONDS).publish()
        // 1번 구독자 등록
        connectableObservable.subscribe { println("1st subscriber : $it") }
        println("Add first subscriber")

        // 2번 구독자 등록
        connectableObservable.map { "2nd subscriber : $it" }.subscribe { println(it) }
        println("Add second subscriber")

        // observable connect()
        connectableObservable.connect()
        runBlocking { delay(300) }

        // 3번 구독자 등록
        connectableObservable.map{ "3rd subscriber : $it" }.subscribe{ println(it) }
        println("Add third subscriber")
        runBlocking { delay(300) }
    }

}