import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import java.util.concurrent.Callable

class Using {

    fun run() {
        using()
    }

    /**
        RxKotlin에서는 코틀린의 use 함수처럼 사용완료후 자원을 반환을 지원하는 연산자로 using을 지원합니다.
        자바로 따지자면 try catch with resources와 같겠네요.
        use나 try catch with resources는 Closable interface를 구현한 클래스에 대해서 자동으로 close()를 호출해 주는 역할을 하지만
        rxKotlin에서 제공하는 using은 그보다 범용적으로 resource를 해제할수 있는 위치를 제공합니다.
     */

    class DataLoad : Callable<DataLoad>{
        val list = mutableListOf<Any>()

        init {
            (1..10).forEach{ list.add(it) }
        }

        fun release() {
            list.clear()
            println("release source")
        }

        override fun call(): DataLoad {
            return this
        }
    }

    /**
        using operator
        using은 세개의 람다를 param으로 전달받습니다.
            1. resource의 선언
            2. resource의 사용 및 Obserbable을 return
            3. reousrce의 해제
        throw 가 발생하더라도 resource의 종료가 우선시되고 error가 Observer로 전달됩니다.
     */

    fun using() {
        Observable.using(DataLoad(), { data ->
            data.list.toObservable()
        }, { data ->
            data.release()
        }).subscribeBy(onNext = { println(it)}, onComplete = { println("Completed!!")}, onError = { println(it)})
    }

}
