package com.nassimo.coroutinesjobs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.nassimo.coroutinesjobs.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var PROGRESS_MAX = 100
    private var PROGRESS_START = 0
    private var JOB_TIME = 4000 //mills
    private lateinit var  job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartCancelJob.setOnClickListener{
            if(!::job.isInitialized){
                initJob()
            }
            binding.progressBar.startJobOrCancel(job)
        }
    }
    private fun ProgressBar.startJobOrCancel(job: Job){
        if(this.progress > 0){
            println("$job is already active.")
            resetJob()
        }else{
            binding.btnStartCancelJob.setText(R.string.btn_text_cancel_job)

            CoroutineScope(Dispatchers.IO + job).launch {
                 //if you want cancel this job you must be add it with context + jobName
                //Example CoroutineScope(Dispatches.IO + jobName).coroutineBuilder like launch
                println("Coroutine $this is activated for jobName");
                  //println("Coroutine $this is activated for all job")
                for(i in PROGRESS_START.. PROGRESS_MAX){
                    delay((JOB_TIME/PROGRESS_MAX).toLong())
                    //Begin progress from start = O to 100 by delay(time)
                    this@startJobOrCancel.progress = i
                    updateTextView(i)
                }
                //switching to main thread to display result


            }

        }
    }
    private fun updateTextView(value: Int){
        GlobalScope.launch(Dispatchers.Main) {
            if(value == PROGRESS_MAX){
                binding.tvJobCompleted.setText(R.string.tv_text_Job_is_completed)
            }
            binding.tvCounter.text = "$value%"
        }
    }

    private fun initJob(){
        binding.tvCounter.text = "$PROGRESS_START%"
        //set default text to button
        binding.btnStartCancelJob.setText(R.string.btn_text_start_Job)
        //set empty text before "job is completed"
        binding.tvJobCompleted.setText(R.string.tv_text_empty)

        //initialize job first
        job = Job()
        //Handling exception message
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if(msg.isNullOrBlank()){
                    msg = "UnKnown Cancellation error..."
                }
                print("$job was cancelled. Reason : $msg")
                showToastMessage(msg)
            }
        }
        //init Progress widget
        binding.progressBar.max = PROGRESS_MAX
        binding.progressBar.progress = PROGRESS_START
    }

    private fun resetJob(){
          if(job.isActive || job.isCompleted){
              job.cancel(CancellationException(resources.getString(R.string.text_resitting_job)))
              initJob()
          }

    }
    private fun showToastMessage(msg: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }


}