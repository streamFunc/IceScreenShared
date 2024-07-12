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

/**
 * Created by Lesa on 2018/12/03.
 */
open class Encoder(private val videoW: Int, private val videoH: Int, private val videoBitrate: Int,
                   private val videoFrameRate: Int, private var encoderListener: EncoderListener?)
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

      //  mp4Muxer.start(format)

        //val localIpAddress = Utils.getLocalIpAddress()
       /* val error = clientRtp.init(localIpAddress)
        if(error != null){
            Log.d("ClientInitFail", "Client initialized fail")
        }else{
            httpRequestOk = true
            Log.d("ClientInitSuccess", "Client initialized successfully")
        }*/
        Hole.setFecEnable(true)
      //  Hole.setFecParm(10,8)


       // iceMyRoom = "caller$iceMyRoom"
      //  Log.d("myTest", "room:$iceMyRoom")


       Hole.startConnect("","47.92.86.188:9090","turn://admin:123456@47.92.86.188","ctrl","whatever")
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
                        Hole.startSendH264ByteQueue("whatever",outData)
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
                        Hole.startSendH264ByteQueue("whatever",keyframe)
                      /*  if(httpRequestOk){
                            clientRtp.sendH264Packet(keyframe)
                        }*/
                        encoderListener!!.onH264(keyframe, 1, mBufferInfo.presentationTimeUs)
                    } else {
                        //其他帧末
                      //  Log.d("myTest", "receive p frame data...")
                      //  fos.write(outData)
                        Hole.startSendH264ByteQueue("whatever",outData)
                      /*  if(httpRequestOk){
                            clientRtp.sendH264Packet(outData)
                        }*/
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
        Hole.stopConnect("whatever")
       // clientRtp.close()
    }


    fun exit() {
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


class H264To3gpConverter(private val outputFilePath: String) {
    private val mediaMuxer: MediaMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP)
    private var videoTrackIndex = -1
    private var frameIndex: Long = 0
    private var isRelease = false

    init {
        // 添加视频轨道
        val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_H263, VIDEO_WIDTH, VIDEO_HEIGHT)

        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) //颜色格式
         videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 187500) //码流
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE) //帧数
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
        videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel1);
        videoTrackIndex = mediaMuxer.addTrack(videoFormat)
        mediaMuxer.start()
    }

    fun writeH264Data(h264Data: ByteArray) {

        // 假设这里有一个变量用于存储上一帧的呈现时间
        // 可以根据需要进行初始化
        var lastPresentationTimeUs: Long = 0

        val presentationTimeUs = calculatePresentationTimeUs(frameIndex, lastPresentationTimeUs)

        Log.d("myTest","presentationTimeUs is:$presentationTimeUs")

        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.presentationTimeUs = presentationTimeUs
        bufferInfo.offset = 0
        bufferInfo.size = h264Data.size
        bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME

        val byteBuffer = ByteBuffer.wrap(h264Data)

        mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)

        // 更新帧索引和上一帧的呈现时间
        frameIndex++
        Log.d("myTest","frameIndex: $frameIndex")
        lastPresentationTimeUs = presentationTimeUs
    }

    private fun calculatePresentationTimeUs(frameIndex: Long, lastPresentationTimeUs: Long): Long {
        // 在这里根据 frameIndex 和 lastPresentationTimeUs 计算 presentationTimeUs
        // 这里给出的是一个示例，你可以根据需要自行修改
        val frameRate = 30
        return lastPresentationTimeUs + ((1000000L / frameRate) * (frameIndex + 1))
    }

    private fun signalEndOfStream() {
        val eos = MediaCodec.BufferInfo()
        val buffer = ByteBuffer.allocate(0)
        eos[0, 0, 0] = MediaCodec.BUFFER_FLAG_END_OF_STREAM
        if (videoTrackIndex != -1) {
            mediaMuxer.writeSampleData(videoTrackIndex, buffer,eos)
        }
        videoTrackIndex = -1
    }

    fun stop() {
       // Thread.sleep(1000)
        if(!isRelease){
            Log.d("myTest","3gp stop it...")
            isRelease = true
            Thread.sleep(2000)
          //  signalEndOfStream()
            mediaMuxer.stop()
            mediaMuxer.release()
        }

    }

    companion object {
        private const val VIDEO_WIDTH = 720
        private const val VIDEO_HEIGHT = 1080
        private const val FRAME_RATE = 30
        private const val I_FRAME_INTERVAL = 3
    }
}



class H264ToMp4Converter(private val outputPath: String) {
    private val mediaMuxer: MediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var videoTrackIndex = -1
    private var frameIndex: Long = 0
    private var isRelease = false

     fun start(format: MediaFormat) {
        videoTrackIndex = mediaMuxer.addTrack(format)
        mediaMuxer.start()
    }


    fun stop() {
       // Thread.sleep(1000)
        if(!isRelease){
            isRelease = true
            Log.d("myTest","mp4 stop it...")
           // signalEndOfStream()
            Thread.sleep(2000)
            mediaMuxer.stop()
            mediaMuxer.release()
        }

    }

    private fun signalEndOfStream() {
        val eos = MediaCodec.BufferInfo()
        val buffer = ByteBuffer.allocate(0)
        eos[0, 0, 0] = MediaCodec.BUFFER_FLAG_END_OF_STREAM
        if (videoTrackIndex != -1) {
            mediaMuxer.writeSampleData(videoTrackIndex, buffer,eos)
        }
        videoTrackIndex = -1
    }

    fun writeH264Data(h264Data: ByteArray) {
        // 假设这里有一个变量用于跟踪帧的索引
        // 可以根据需要进行初始化或者递增

        // 假设这里有一个变量用于存储上一帧的呈现时间
        // 可以根据需要进行初始化
        var lastPresentationTimeUs: Long = 0

        // 设置 BufferInfo 的参数
        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.offset = 0
        bufferInfo.size = h264Data.size
        bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME

        val byteBuffer = ByteBuffer.wrap(h264Data)

        // 计算呈现时间并设置给 BufferInfo
        val presentationTimeUs = calculatePresentationTimeUs(frameIndex, lastPresentationTimeUs)
        bufferInfo.presentationTimeUs = presentationTimeUs

        // 将数据写入到 MP4 文件
        mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)

        // 更新帧索引和上一帧的呈现时间
        frameIndex++
        lastPresentationTimeUs = presentationTimeUs
    }

    private fun calculatePresentationTimeUs(frameIndex: Long, lastPresentationTimeUs: Long): Long {
        // 在这里根据 frameIndex 和 lastPresentationTimeUs 计算 presentationTimeUs
        // 这里给出的是一个示例，你可以根据需要自行修改
        val frameRate = 30
        return lastPresentationTimeUs + ((1000000L / frameRate) * (frameIndex + 1))
    }


    companion object {
        const val VIDEO_WIDTH = 720
        const val VIDEO_HEIGHT = 1080
        const val FRAME_RATE = 30
        const val I_FRAME_INTERVAL = 3
    }
}

