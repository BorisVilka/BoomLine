package com.boom.line


import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.math.abs

class GameView(ctx: Context, att: AttributeSet): SurfaceView(ctx,att) {


    private var paintB: Paint = Paint(Paint.DITHER_FLAG)
    private var ball = BitmapFactory.decodeResource(ctx.resources,R.drawable.ball)
    private var basket = BitmapFactory.decodeResource(ctx.resources,R.drawable.basket)
    private var diamond = BitmapFactory.decodeResource(ctx.resources,R.drawable.diamond)

    private var paintT = Paint().apply {
        color = ctx.getColor(R.color.orange)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = DashPathEffect(floatArrayOf(10f,20f),0f)
    }
    private var listener: EndListener? = null
    private val random = Random()
    private var millis = 0
    var bg = MediaPlayer.create(ctx,R.raw.bg)
    var music = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",false)
    var balance = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("balance",0)

    var level = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("tmp",0)

    var bx =0f
    var by = 0f

    var basY = 0f

    var list = mutableListOf<Model>()

    init {
        bg.setOnCompletionListener { it.start() }
        if(music) bg.start()
        diamond = Bitmap.createScaledBitmap(diamond,diamond.width/3,diamond.height/3,true)
        ball = Bitmap.createScaledBitmap(ball,ball.width/3,ball.height/3,true)
        basket = Bitmap.createScaledBitmap(basket,basket.width/3,basket.height/3,true)
        holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val canvas = holder.lockCanvas()
                if(canvas!=null) {
                    while(list.size<level) {
                        list.add(Model(random.nextInt(canvas.width/2)+canvas.width/2f,random.nextInt(canvas.height/3)+canvas.height/2f))
                    }
                    bx = canvas.width- ball.width*2f
                    by = canvas.height/4f
                    basY = random.nextInt(canvas.height/2)+canvas.height/3f
                    draw(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                paused = true
                bg.stop()
                bg.release()

            }

        })
        val updateThread = Thread {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (!paused) {
                        update.run()
                        millis += delta
                    }
                }
            }, 500, if(find==2) 10 else 16)
        }

        updateThread.start()
    }
    var started = false
    private var mX = 0f
    private  var mY = 0f
    private var sx = 0f
    private var sy = 0f
    private var ex = 0f
    private var ey = 0f
    var mPath = Path()
    private val TOUCH_TOLERANCE = 4f

    private fun touch_start(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
        sy = y
        sx = x
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy: Float = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        ex = mX
        ey = mY
        mPath.lineTo(mX, mY)
       started = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
       if(!started) {
           val x = event.x
           val y = event.y
           when (event.action) {
               MotionEvent.ACTION_DOWN -> {
                   touch_start(x, y)
                   invalidate()
               }

               MotionEvent.ACTION_MOVE -> {
                   touch_move(x, y)
                   invalidate()
               }

               MotionEvent.ACTION_UP -> {
                   touch_up()
                   invalidate()
               }
           }
       }
        return true
    }

    var paused = false
    var delta = -1
    var find = 0
    var pos = floatArrayOf(0f,0f)
    var tan = floatArrayOf(0f,0f)

    var len = 0
    var segm = 0f
    var ind = 0
    var win = false

    val update = Runnable{
        var isEnd = false
        var sc = false
        if(paused) return@Runnable
        try {
            val canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)

            if(started) {
                val m = PathMeasure(mPath,false)
                if(find==0) {
                    var distance = 0f
                    while(distance<m.length) {
                        m.getPosTan(distance,pos,tan)
                        if(abs(pos[0]-(bx+ball.width/2f)) <3f) {
                            find = 1
                            break
                        }
                        distance++
                    }
                    if(find==0) find = -1
                    //canvas.drawCircle(pos[0],pos[1],100f,paintT)
                } else if(find==1) {
                    if(by+ball.height+delta<pos[1]) {
                        by += delta
                        delta +=2
                    } else find = 2
                } else if(find==-1) {
                    by += delta
                    delta +=2
                    if(by>=canvas.height) find = -2
                } else if(find==-2) isEnd = true
                else if(find==2) {
                    val m = PathMeasure(mPath,false)
                    len = m.length.toInt()/3
                    segm = 3f
                    if(ind<len) {
                        val matr = Matrix()
                        m.getMatrix(segm*ind,matr,PathMeasure.POSITION_MATRIX_FLAG)
                        matr.postTranslate(-ball.width/2f,-ball.height.toFloat())
                        matr.preRotate(-ind.toFloat(),ball.width/2f,ball.height/2f)
                        canvas.drawBitmap(ball, matr, paintB);
                        val values = floatArrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f)
                        matr.getValues(values)
                        val x1 = values[Matrix.MTRANS_X]
                        val y1 = values[Matrix.MTRANS_Y]
                        var i = 0
                       if(x1<=50f+basket.width && x1>=50+basket.width/3 && y1<basY+basket.height && y1>basY+basket.height/2) {
                            win = true
                            isEnd = true
                        }
                        while(i<list.size) {
                            val j = list[i]
                            if((j.x<x1+ball.width && j.x>x1 || j.x<x1 && j.x+diamond.width>x1)
                                && (j.y<y1 && j.y+diamond.height>y1 || j.y>y1 && j.y<y1+ball.height)) {
                                balance++
                                sc = true
                                list.removeAt(i)
                            } else i+=1
                        }
                        ind+=2
                    } else find = 3

                } else if(find==3) {
                    val m = PathMeasure(mPath, false)
                    len = m.length.toInt() / 3
                    segm = 3f
                    ind = len-1
                    val matr = Matrix()
                    m.getMatrix(segm * ind, matr, PathMeasure.POSITION_MATRIX_FLAG)
                    matr.postTranslate(-ball.width / 2f, -ball.height.toFloat())
                    matr.preRotate(-ind.toFloat(), ball.width / 2f, ball.height / 2f)
                    canvas.drawBitmap(ball, matr, paintB);
                    val values = floatArrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f)
                    matr.getValues(values)
                    val x1 = values[Matrix.MTRANS_X]
                    val y1 = values[Matrix.MTRANS_Y]
                    if(x1<=50f+basket.width && x1>=50+basket.width/3 && y1<basY+basket.height && y1>basY) {
                        win = true
                        isEnd = true
                    }
                }
            }
            for(i in list) canvas.drawBitmap(diamond,i.x,i.y,paintB)
            canvas.drawPath(mPath,paintT)
            if(find<2) canvas.drawBitmap(ball,bx,by,paintB)
            canvas.drawBitmap(basket,50f,basY,paintB)
            holder.unlockCanvasAndPost(canvas)
            if(isEnd) {
                togglePause()
                if(listener!=null) listener!!.end()
            }
            if(sc) {
                listener?.score(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEndListener(list: EndListener) {
        this.listener = list
    }
    fun togglePause() {
        paused = !paused
    }
    companion object {
        interface EndListener {
            fun end();
            fun score(score: Int);
        }
        data class Model(var x: Float, var y: Float)
    }
}