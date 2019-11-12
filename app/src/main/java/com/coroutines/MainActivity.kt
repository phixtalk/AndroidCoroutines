package com.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val RESULT_1 = "Result #1"
    private val RESULT_2 = "Result #2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {

            //IO, Main, Default
            //call suspending function inside a coroutine scope
            CoroutineScope(IO).launch {
                //IO coroutine scope will run on a background thread
                fakeApiRequest()
            }
        }
    }

    private fun setNewText(input: String){
        val newText = textDisplay.text.toString() + "\n$input"
        textDisplay.text = newText
    }

    private suspend fun setTextOnMainThread(input: String){
        //IO runs on a different thread on the background, so we cannot update UI from the background thread
        //in other to update the UI, we need to switch to the main thread by using withcontext
        withContext(Main){
            setNewText(input)
        }
    }

    private suspend fun fakeApiRequest(){
        var result1 = getResult1FromApi()
        println("debug: $result1")
        setTextOnMainThread(result1)

        //here we can see that, we are making a non-blocking network calls to run like a blocking call
        //i.e they run sequentially, second job waits for the first job to finish before continuing
        var result2 = getResult2FromApi()
        setTextOnMainThread(result2)
    }

    private suspend fun getResult1FromApi(): String {
        logThread("getResult1FromApi")
        delay(1000)
        return RESULT_1
    }

    private suspend fun getResult2FromApi(): String {
        logThread("getResult2FromApi")
        delay(1000)
        return RESULT_2
    }

    private fun logThread(methodName: String) {
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
