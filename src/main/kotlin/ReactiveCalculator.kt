import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.regex.Pattern

class ReactiveCalculator (a: Int, b: Int) {

    private val subjectCalc : Subject<ReactiveCalculator> = PublishSubject.create()
    private var nums: Pair<Int, Int> = Pair(0, 0)

    init {
        nums = Pair(a, b)

        subjectCalc.subscribe {
            with(it) {
                calculateAddition()
                calculateSubstraction()
                calculateMultiplication()
                calculateDivision()
            }
        }
        subjectCalc.onNext(this)
    }

    private inline fun calculateDivision(): Double {
        val result = nums.first / (nums.second*1.0)
        println("Add = $result")
        return result
    }

    private inline fun calculateMultiplication(): Int {
        val result = nums.first * nums.second
        println("Add = $result")
        return result
    }

    private inline fun calculateSubstraction(): Int {
        val result = nums.first - nums.second
        println("Add = $result")
        return result
    }

    private inline fun calculateAddition(): Int {
        val result = nums.first + nums.second
        println("Add = $result")
        return result
    }

    private inline fun modifyNumbers (a:Int = nums.first, b: Int = nums.second) {
        nums = Pair(a,b)
        subjectCalc.onNext(this)
    }

    suspend fun handleInput(inputLine:String?) {
        if(!inputLine.equals("exit")) {
            val pattern: Pattern = Pattern.compile("([a|b])(?:\\s)?=(?:\\s)?(\\d*)");

            var a: Int? = null
            var b: Int? = null

            val matcher: java.util.regex.Matcher = pattern.matcher(inputLine)

            if (matcher.matches() && matcher.group(1) != null && matcher.group(2) != null) {
                if(matcher.group(1).toLowerCase() == "a"){
                    a = matcher.group(2).toInt()
                } else if(matcher.group(1).toLowerCase() == "b"){
                    b = matcher.group(2).toInt()
                }
            }

            when {
                a != null && b != null -> modifyNumbers(a, b)
                a != null -> modifyNumbers(a = a)
                b != null -> modifyNumbers(b = b)
                else -> println("Invalid Input")

            }
        }
    }
}