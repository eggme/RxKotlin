import io.reactivex.Observable
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class Subject {

    /**
        Hot Observable 핫 옵저버블을 구현하는 좋은 방법 중 하나인 Subjects 주제가 있다
        Subjects는 기본적으로 옵저버블과 옵저버의 조합입니다.
        핫 옵저버블과 마찬가지로, 내부 Observer를 유지하고, 배출시에 구독한 모든 옵저버에 단일 푸시를 준다
        Subjects의 특징
            - 옵저버블이 갖고있어야 하는 모든 연산자를 갖고있다.
            - 옵저버와 마찬가지로 모든 값에 접근이 가능하다.
            - onComplete완료 시, onError에러 시, onDispose구독 해지 이후에는 재 사용이 불가능합니다.
            - Subject는 동시에 옵저버이기 때문에, 값을 onNext(T)로 전달하면, 그 값을 온전히 Observable로 전달 받을 수 있게 된다.

        Subjects의 종류
            AsyncSubject
                - AsyncSubject는 수신 대기중인 옵저버블의 마지막 값과, 그에대한 배출만 전달하게 됩니다.
                  쉽게 말하면, 마지막 값만을 전달한다는 것입니다.
            PublishSubject
            BehaviorSubject
            ReplaySubject
    */

    fun run() {
        //publishSubject()
        //behaviorSubject()
//        subjectsTest()
//        asyncObservable()
        asyncObservableOnNextCall()
    }

    /**
        구독을 언제하든 항상 마지막 결과만 받는것 같습니다
     */
    fun asyncObservableOnNextCall(){
        val subject = AsyncSubject.create<Int>() //1
        subject.onNext(1) //2
        subject.onNext(2)
        subject.onNext(3)
        subject.onNext(4)
        subject.subscribe({ //3
            //onNext
            println("S1 Received $it")
        },{
            //onError
            it.printStackTrace()
        },{
            //onComplete
            println("S1 Complete")
        })
        subject.onNext(5) //4
        subject.subscribe({ //5
            //onNext
            println("S2 Received $it")
        },{
            //onError
            it.printStackTrace()
        },{
            //onComplete
            println("S2 Complete")
        })
        subject.onComplete()//6
    }

    fun asyncObservable() {
        val observable = Observable.just(1,2,3,4)
        val subject = AsyncSubject.create<Int>()

        observable.subscribe(subject)
        subject.subscribe({
            // onNext
            println("Received $it")
        }, {
            // onError
            it.printStackTrace()
        }, {
            // onComplete
            println("Complete")
        })

        subject.onComplete()
    }

    fun subjectsTest() {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = PublishSubject.create<Long>()

        observable.subscribe(subject)
        subject.subscribe{
            println("Received $it")
        }
        runBlocking { delay(1100) }
    }

    /**
        PublishSubject를 등록할 경우 등록 시점부터 이후 데이터를 전달 받습니다
     */
    fun publishSubject() {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = PublishSubject.create<Long>()
        observable.subscribe(subject)
        runBlocking { delay(300) }
        subject.subscribe{ println("1st: $it") }
        runBlocking { delay(300) }
        subject.subscribe{ println("2nd: $it") }
        runBlocking { delay(300) }
    }

    /**
        Timer - 지정된 시간이 지나고 난 후 항목을 하나 배출
        Interavl - 특정 시간별로 연속된 정수형을 배출
        Take - Observable이 배출한 처음 n개의 항목들만 배출

        BehaviorSubject의 경우 등록 시점에 이전에 배출된 직전값 하나를 전달받고 시작합니다
     */
    fun behaviorSubject() {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val subject = BehaviorSubject.create<Long>()
        observable.subscribe(subject)
        runBlocking { delay(300) }
        subject.subscribe{ println("1st : $it") }
        runBlocking { delay(300) }
        subject.subscribe { println("2ed : $it") }
        runBlocking { delay(300) }
    }

    /**
        AsyncSubject는
     */
    fun asyncSubject() {
        val observable = Observable.just(1,2,3,4,5,6,7,8,9,10)
        val subject = AsyncSubject.create<Int>()
        observable.subscribe(subject)
        subject.subscribe{ println("1st : $it") }
        subject.subscribe{ println("2nd: $it") }

    }
}