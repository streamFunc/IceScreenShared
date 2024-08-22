package com.sc.lesa.mediashar.jlib.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import hole.Hole
import java.net.Inet4Address
import java.net.NetworkInterface
import java.nio.ByteBuffer
//import rtmpSdk.RtmpSdk

/**
 * Created by Lesa on 2018/12/03.
 */
open class Encoder(private val videoW: Int, private val videoH: Int, private val videoBitrate: Int,
                   private val videoFrameRate: Int, private var encoderListener: EncoderListener?,private var iceRoom: String)
    : Thread(TAG) {

    companion object {
        private const val TAG = "Encoder"
        private const val MIME = "Video/AVC"
    }

    private lateinit var codec: MediaCodec
    private lateinit var mSurface: Surface
    private val TIMEOUT_USEC = -1
    private lateinit var configbyte: ByteArray
    private var exit = false
    private val mBufferInfo = MediaCodec.BufferInfo()
   // private var iceMyRoom = GlobalVariables.currentIceRoom

   // private val videoMuxer = H264To3gpConverter("/sdcard/output.3gp")
  //  private var frameIndex: Long = 0
   // private var mp4Muxer = H264ToMp4Converter("/sdcard/output.mp4")

  //  private val clientRtp = Client.newClient()
    //private var httpRequestOk = false


    open fun init(){
        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.presentationTimeUs = 0

        initMediaCodec()
    }

    fun getmSurface(): Surface {
        return mSurface
    }

    private fun initMediaCodec() {
        Log.d("myTest", "00000 initMediaCodec videoBitrate : $videoBitrate")
        val format = MediaFormat.createVideoFormat(MIME, videoW, videoH)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) //颜色格式
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate) //码流
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate) //帧数
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
        codec = MediaCodec.createEncoderByType(MIME)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = codec.createInputSurface()
        codec.start()

        Hole.setFecEnable(true)
      //  Hole.setFecParm(10,8)


        iceRoom = "caller$iceRoom"
        Log.d("myTest", "room:$iceRoom")
        iceRoom = "whatever"

      //RtmpSdk.startConnect("rtmp://47.92.86.188/live/test")
       Hole.startConnect("","47.92.86.188:9090","turn://admin:123456@47.92.86.188","ctrl",iceRoom)
       // Hole.startConnect("","45.63.59.254:9090","turn://appcrash:testonly@45.63.59.254","ctrl","whatever")
    }

    /**
     * 获取h264数据
     */
    override fun run() {
        Log.d("myTest", "00000 start...")
      //  val filePath = "/sdcard/output.h264"
      //  val file = File(filePath)
       // val fos = FileOutputStream(file)



        try {
            while (!exit) {
                var outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC.toLong())
                while (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    val outData = ByteArray(mBufferInfo.size)
                    outputBuffer[outData]
                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) { // 含有编解码器初始化/特定的数据
                        configbyte = outData
                        Log.d(this.javaClass.name,"生成配置")
                      //  Log.d("myTest", "receive data...")
                      //  fos.write(outData);
                        Hole.startSendH264ByteQueue(iceRoom,outData)
                       // RtmpSdk.startPush(outData)
                       /* if(httpRequestOk){
                            clientRtp.sendH264Packet(outData)
                        }*/

                    } else if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) { // 关键帧
                        Log.d(this.javaClass.name,"生成关键帧")
                        val keyframe = ByteArray(mBufferInfo.size + configbyte.size)
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.size)
                        System.arraycopy(outData, 0, keyframe, configbyte.size, outData.size)
                       // Log.d("myTest", "receive key frame data...")
                       // fos.write(keyframe)
                        Hole.startSendH264ByteQueue(iceRoom,keyframe)
                      //  RtmpSdk.startPush(keyframe)
                        encoderListener!!.onH264(keyframe, 1, mBufferInfo.presentationTimeUs)
                    } else {
                        //其他帧末
                      //  Log.d("myTest", "receive p frame data...")
                      //  fos.write(outData)
                        Hole.startSendH264ByteQueue(iceRoom,outData)
                      /*  if(httpRequestOk){
                            clientRtp.sendH264Packet(outData)
                        }*/
                        //RtmpSdk.startPush(outData)
                        encoderListener!!.onH264(outData, 2, mBufferInfo.presentationTimeUs)
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC.toLong())
                }
            }
        } catch (e: Exception) {
            Log.d("myTest", "00000 releaseOutputBuffer...")
            encoderListener?.onError(e)
            e.printStackTrace()
        }
       // videoMuxer.stop()
       // mp4Muxer.stop()
       // fos.close()
        //fos1.close()
       // mp4Muxer.stop()
        onClose()
    }

    open fun onClose() {
        codec.stop()
        codec.release()
        encoderListener?.onCloseH264()
        encoderListener = null
        //RtmpSdk.stopConnect()
       // clientRtp.close()
    }


    fun exit() {
        Log.d("myTest", "lby stopConnect...")
        Hole.stopConnect(iceRoom)
        exit = true
    }

    interface EncoderListener {
        fun onH264(buffer: ByteArray, type: Int, ts: Long)
        fun onError(t:Throwable)
        fun onCloseH264()
    }

}


object Utils {

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        Log.d("LocalIpAddress", "Local IP Address: ${addr.hostAddress}")
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}





