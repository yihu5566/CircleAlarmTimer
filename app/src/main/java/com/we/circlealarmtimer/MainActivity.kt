package com.we.circlealarmtimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    lateinit var startTime: String
    lateinit var endTime: String
    val openTime = "09:00"
    val closeTime = "21:00"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val alarmTimerView = findViewById<CircleAlarmTimerView>(R.id.circletimerview)
        val tvSelectTime = findViewById<TextView>(R.id.tv_select_time)
        val btnReset = findViewById<Button>(R.id.btn_reset)

        resetAlarmView(alarmTimerView, tvSelectTime)



        alarmTimerView?.setOnTimeChangedListener(object :
            CircleAlarmTimerView.OnTimeChangedListener {
            override fun start(starting: String?) {
                starting?.let {
                    startTime = starting
                    tvSelectTime?.text = "$startTime~$endTime"
                }
            }

            override fun end(ending: String?) {
                ending?.let {
                    endTime = ending
                    tvSelectTime?.text = "$startTime~$endTime"
                }
            }
        })


        btnReset.setOnClickListener {
            resetAlarmView(alarmTimerView, tvSelectTime)
        }
    }

    private fun resetAlarmView(
        alarmTimerView: CircleAlarmTimerView?,
        tvSelectTime: TextView
    ) {
        startTime = openTime
        endTime = closeTime
        alarmTimerView?.setStartTime(openTime)
        alarmTimerView?.setEndTime(closeTime)
        tvSelectTime?.text = "$startTime~$endTime"
    }


}