
package com.sc.lesa.mediashar.config

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.util.Log
import com.sc.lesa.mediashar.BR


class Config(): BaseObservable() {
    val filename = "MyConfigfile"

    companion object{
        private var value: Config?=null
        fun getConfig(context: Context):Config{
            if (value==null){
                synchronized(Config::class.java) {
                    if (value==null){
                        value=Config()
                        value!!.init(context)
                    }
                }
            }
            return value!!
        }
    }

    fun init(context:Context){
        val sp = context.getSharedPreferences(filename,Context.MODE_PRIVATE)
        width=sp.getString("width",width)
        height=sp.getString("height",height)
        videoBitrate=sp.getString("videoBitrate",videoBitrate)
        videoFrameRate=sp.getString("videoFrameRate",videoFrameRate)
        channelCount=sp.getString("channelCount",channelCount)
        voiceByteRate=sp.getString("voiceByteRate",voiceByteRate)
        voiceSampleRate=sp.getString("voiceSampleRate",voiceSampleRate)
        channelMode=sp.getInt("channelMode",channelMode)
        encodeFormat=sp.getInt("encodeFormat",encodeFormat)
     //   iceRoom=sp.getString("iceRoom",iceRoom)
      //  GlobalVariables.currentIceRoom = iceRoom
        Log.d("lby", "bit: $videoBitrate")

    }
    
    fun save(context:Context){
       // Log.d("lby", "000 bit: $videoBitrate")
        context.getSharedPreferences(filename,Context.MODE_PRIVATE).also {sp->
            sp.edit().also {
                it.putString("width",width)
                it.putString("height",height)
                it.putString("videoBitrate",videoBitrate)
                it.putString("videoFrameRate",videoFrameRate)
                it.putString("channelCount",channelCount)
                it.putString("voiceByteRate",voiceByteRate)
                it.putString("voiceSampleRate",voiceSampleRate)
                it.putInt("channelMode",channelMode)
                it.putInt("encodeFormat",encodeFormat)
               // it.putString("iceRoom",iceRoom)
               // Log.d("lby", "save currentIceRoom: $iceRoom")
                it.apply()
            }
        }
    }
    
    
    @Bindable
    var width:String="720"
    set(value) {
        field=value
        notifyPropertyChanged(BR.width)
    }
            
    @Bindable
    var height:String="1280"
    set(value) {
        field=value
        notifyPropertyChanged(BR.height)
    }
            
    @Bindable
    var videoBitrate:String="187500"
    set(value) {
        field=value
        notifyPropertyChanged(BR.videoBitrate)
    }
            
    @Bindable
    var videoFrameRate:String="30"
    set(value) {
        field=value
        notifyPropertyChanged(BR.videoFrameRate)
    }
            
    @Bindable
    var channelCount:String="1"
    set(value) {
        field=value
        notifyPropertyChanged(BR.channelCount)
    }
            
    @Bindable
    var voiceByteRate:String="384000"
    set(value) {
        field=value
        notifyPropertyChanged(BR.voiceByteRate)
    }
            
    @Bindable
    var voiceSampleRate:String="44100"
    set(value) {
        field=value
        notifyPropertyChanged(BR.voiceSampleRate)
    }
            
    @Bindable
    var channelMode:Int=16
    set(value) {
        field=value
        notifyPropertyChanged(BR.channelMode)
    }
            
    @Bindable
    var encodeFormat:Int=2
    set(value) {
        field=value
        notifyPropertyChanged(BR.encodeFormat)
    }

    /*var iceRoom:String="19802021668"
        set(value) {
            field=value
            notifyPropertyChanged(BR.iceRoom)
        }*/

    /*private var iceRoom: String? ="19802021668"

    // 其他属性和方法...
    fun getIceRoom(): String? {
        return iceRoom
    }

    fun setIceRoom(iceRoom: String) {
        this.iceRoom = iceRoom
    }

    private var iceRoomShow: String? ="房间号(主叫号码)"
    //private var iceRoomText: String? ="房间号"
    // 其他属性和方法...

    // 其他属性和方法...
    fun getIceRoomShow(): String? {
        return iceRoomShow
    }*/




}

