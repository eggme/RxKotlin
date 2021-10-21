import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CustomOperator{

    fun run() {
        customOperatorTest()
    }

    class SquareNumber<T> : ObservableOperator<String, T> {
        override fun apply(observer: Observer<in String>): Observer<in T> {
            return object: Observer<T> {
                override fun onSubscribe(d: Disposable) = observer.onSubscribe(d)

                override fun onNext(t: T) {
                    if(t is Number) {
                        observer.onNext("Input value: $t square:${t.toInt() * t.toInt()}")
                    }else{
                        observer.onNext("Input value $t is not Number")
                    }
                }

                override fun onError(e: Throwable) = observer.onError(e)

                override fun onComplete() = observer.onComplete()
            }
        }
    }

    class SquareNumber2: ObservableTransformer<Int, Int> {
        override fun apply(upstream: Observable<Int>): ObservableSource<Int> {
            return upstream
                .map { it * it }
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
        }
    }

    /**
        Observable에 operator를 사용하기 위해 lift() 연산자를 이용해 설정
     */

    fun customOperatorTest() {
        Observable.range(1, 5)
            .lift(SquareNumber<Number>())
            .map { "($it)" }
            .subscribe{ println("result: $it") }
        /**
            compose는 ObservableTransformer를 구현한 함수를 Observable chain에 추가합니다.
            compose의 반환값 역시 Observable 이빈다.

            observeOn 이랑 subscribeOn 헷갈림...
         */
        Observable.range(6, 5)
            .compose(SquareNumber2())
            .map { "square $it" }
            .blockingSubscribe{ println(it) }
    }
}