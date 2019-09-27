package edu.lhu.easypainter

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.io.OutputStream

class PaintBoard(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var paint: Paint
    private var bitmap: Bitmap
    private var mCanvas: Canvas

    private var startX: Float = 0f
    private var startY: Float = 0f

    init {
        // bitmap 點陣圖 >> Canvas的屬性
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels - 400
        // ARGB 分別佔用 8位，合起來就是 32位，也就是 4 字節
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Canvas 畫布
        mCanvas = Canvas(bitmap)
        mCanvas.drawColor(Color.WHITE)

        // Paint
        paint = Paint()
        paint.setColor(Color.BLACK) //顏色
        paint.setStrokeWidth(5f) //線條寬度
        paint.style = Paint.Style.STROKE //繪製模式
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true //抗鋸齒開關
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas!!.drawBitmap(bitmap, 0f, 0f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val stopX = event.x
                val stopY = event.y

                mCanvas.drawLine(startX, startY, stopX, stopY, paint)
                startX = event.x
                startY = event.y

                // call onDraw 更新畫面
                invalidate()
            }
        }

        return true
    }

    fun colorChange(view: View) {
        when (view.id) {
            R.id.whiteButton -> paint.setColor(Color.WHITE)
            R.id.blackButton -> paint.setColor(Color.BLACK)
            R.id.redButton -> paint.setColor(Color.RED)
            R.id.orangeButton -> paint.color = ContextCompat.getColor(context, R.color.orange)
            R.id.yelloButton -> paint.setColor(Color.YELLOW)
            R.id.greenButton -> paint.color = ContextCompat.getColor(context, R.color.green)
            R.id.blueButton -> paint.setColor(Color.BLUE)
        }
    }

    fun saveBitmap(stream: OutputStream) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    }

    fun clearBitmap() {
//        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mCanvas = Canvas(bitmap)
        mCanvas.drawColor(Color.WHITE)
        invalidate()
    }
}
