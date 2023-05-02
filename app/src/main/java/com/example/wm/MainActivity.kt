package com.example.wm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        var counter: Int = 0
    }

    private val constraints = Constraints.Builder()
        .setRequiresCharging(false)
        .setRequiredNetworkType(NetworkType.CONNECTED)
        //.setRequiresCharging(false)
        .setRequiresBatteryNotLow(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val click=findViewById<Button>(R.id.click_me)
        //val stop=findViewById<Button>(R.id.stop_me)

        click.setOnClickListener { //your implementation goes here
            myWorkManager()
            counter++
        }

//        stop.setOnClickListener { //your implementation goes here
//            WorkManager.getInstance(this@MainActivity).cancelAllWork()
//        }

    }

    private fun myWorkManager(){

        val myRequest = PeriodicWorkRequest.Builder(
            download::class.java, 1, TimeUnit.DAYS)
            .setConstraints(this.constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "id $counter",ExistingPeriodicWorkPolicy.REPLACE, myRequest
        )
    }
}

