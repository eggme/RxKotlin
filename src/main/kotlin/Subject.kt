import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class Subject {

    fun run() {
        publishSubject()
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

}