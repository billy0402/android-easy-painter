package edu.lhu.easypainter

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var paintBoard: PaintBoard
    //    private lateinit var vibrator: Vibrator
    private lateinit var sensorManager: SensorManager
    private var isClear = false

    //Activity準備產生
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupSensor()
    }

    //Activity結束之前
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }

    private fun setupView() {
        paintBoard = findViewById(R.id.layout_paint_board)

        saveButton.setOnClickListener(saveClickHandler)
        albumButton.setOnClickListener(albemClickHandler)
    }

    fun colorChange(view: View) {
        paintBoard.colorChange(view)
    }

    //判斷相片存取權限
    private fun checkWritable(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            return false
        } else {
            return true
        }
    }

    //將圖片存到相簿
    private val saveClickHandler = View.OnClickListener {
        if (checkWritable()) {
            try {
                //根據當下時間產生檔名
                val fileName = (System.currentTimeMillis() / 1000).toString() + ".jpg"
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)
                val stream = FileOutputStream(file)
                paintBoard.saveBitmap(stream)
                stream.close()

                //更新資料夾
                val intent = Intent()
                intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.setData(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))
                sendBroadcast(intent)

                Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show()

                showClearDialog()
            } catch (e: Exception) {
                println(e)
                Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //打開相簿檢視圖片
    private var albemClickHandler = View.OnClickListener {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.INTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    //建立感應器
    private fun setupSensor() {
        //和system要到SensorManager，再要到對應的sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Accelerometer sensor
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //registerListener用來接收sensor拿到的數據
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        printAllSensors()

        // Vibrator
//        vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
    }

    //當使用者搖動手機的時候，執行shakeHandler()
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                val xValue = Math.abs(event.values[0]) // 加速度 - X 軸方向
                val yValue = Math.abs(event.values[1]) // 加速度 - Y 軸方向
                val zValue = Math.abs(event.values[2]) // 加速度 - Z 軸方向

                if (xValue > 20 || yValue > 20 || zValue > 20) {
                    shakeHandler()
                }
            }
        }
    }

    private fun shakeHandler() {
        if (isClear) {
            return
        }

        isClear = true

        showClearDialog()

        //防止在搖動手機過程連續清空畫面，執行一個Handler相隔一秒以後在將isClear改回false
        Handler().postDelayed({
            isClear = false
        }, 1000)
    }

    //震動
//    private fun doVibrate() {
//        if (Build.VERSION.SDK_INT >= 26) {
//            vibrator.vibrate(VibrationEffect.createOneShot(100, 10))
//        } else {
//            vibrator.vibrate(100)
//        }
//    }

    //Log.v – v 代表 verbose – 任何訊息都會輸出
    //Log.d – d 代表 debug – 輸出調試訊息
    //Log.i – i 代表 information - 任何提示性訊息(i.w.e)
    //Log.w – w 代表 warning – 輸出警告訊息
    //Log.e – e 代表 error – 輸出錯誤訊息

    //getSensorList來取得這個設備支援的Sensor
    private fun printAllSensors() {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in allSensors) {
            //印出sensor類型、廠商名、版本
            Log.i("sensors", "${sensor.name} - ${sensor.vendor} - ${sensor.version}")
        }
    }

    //顯示訊息視窗
    private fun showClearDialog() {
        // setup dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Whether to clear the screen or not?")
//        builder.setMessage("Hello world!")
        builder.setPositiveButton("Clear") { _, _ ->
            paintBoard.clearBitmap()

            Toast.makeText(this, "Clear Success", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            println("cancel")
        }

        // create dialog and show it
        val dialog = builder.create()
        dialog.show()
    }
}
