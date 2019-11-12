package com.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class SecondActivity : AppCompatActivity() {

    /**
     * This example illustrates that we can add multiple jobs to the same
     * coroutine scope and cancel them individually without affecting the other running jobs
     * how to do ==>  CoroutineScope(IO + job).launch {)
    * */

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000//ms
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        job_button.setOnClickListener {
            if(!::job.isInitialized){//first check if the lateinit variable has been initialized
                initJob()
            }
            job_progress_bar.startJobOrCancel(job)//toggle button
        }
    }

    fun ProgressBar.startJobOrCancel(job: Job){
        if(this.progress > 0){
            println("${job} is already active. Cancelling...")
            resetJob()
        }
        else{
            job_button.setText("Cancel job #1")
            CoroutineScope(IO + job).launch {
                //we are adding this job to the coroutine IO scope, we can add multiple jobs to a coroutine scope
                //so that if we have multiple jobs running on the IO scope
                //we can cancel then individually without affecting other scopes running on the same IO scope
                println("coroutine $this is activated with job $job")

                for (i in PROGRESS_START.. PROGRESS_MAX){
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String){
        //to update the ui, we need to swithc to the main thread
        GlobalScope.launch(Main) {
            job_complete_text.setText(text)
        }
    }


    private fun resetJob() {
        if(job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting Job"))
            //when a job gets canceled, you cant reuse that same job, you have to create a new one
            initJob()
        }
    }

    fun initJob(){
        job_button.setText("Start Job #1")
        updateJobCompleteTextView("")
        job = Job()

        //the below code take action when the job is completed or cancelled
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if(msg.isNullOrBlank()){
                    msg = "Unknown cancellation error"
                }
                showToast("$job was cancelled. Reason $msg")
            }
        }

        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    fun showToast(text: String){
        GlobalScope.launch(Main) {
            Toast.makeText(this@SecondActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

}
