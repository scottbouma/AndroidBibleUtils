package com.example.utils.network

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.brianherbert.biblenavwatch.data.BibleRef
import com.example.utils.data.yv.YVPassageResponse
import com.example.utils.data.yv.YVPassageResponseWrapper
import com.google.gson.Gson
import java.nio.charset.Charset

class YVFetcher(val context: Context, val listener: YVFetcherListener) {
    companion object {
        internal val TAG = YVFetcher::class.java.simpleName
    }

    interface YVFetcherListener {
        fun onFetched(response: YVPassageResponse? = null)
    }

    fun getPassage(ref: BibleRef) {
        var refStr = ref.book.abbr + "." + ref.chap
        var isChapter = ref.verse == null

        if (!isChapter) {
            refStr += ".${ref.verse}"
        }

        getVerse(refStr, isChapter)
    }

    private fun getVerse(ref: String, isChapter: Boolean = false) {
        Log.v(TAG, "Requesting $ref")
        val queue = Volley.newRequestQueue(context)
        var endpoint = if (isChapter) "chapter" else "verse"
        val url = "https://bible.youversionapi.com/3.1/$endpoint.json?id=111&reference=$ref&format=text"
        val getRequest = object : YVStringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                // response
                var responseWrapper: YVPassageResponseWrapper =
                    Gson().fromJson(response, YVPassageResponseWrapper::class.java)
                listener.onFetched(responseWrapper.response)
            },
            Response.ErrorListener { error ->
                // TODO Auto-generated method stub
                if (error.networkResponse == null) {
                    Log.e(TAG, "error ", error)
                } else {
                    var msg = String(error.networkResponse.data, Charset.forName("UTF-8"))
                    Log.e(TAG, "error bytes: $msg")
                }

                listener.onFetched()
            }
        ) {

        }
        getRequest.setShouldCache(false)
        queue.add(getRequest)
    }

    abstract class YVStringRequest(
        method: Int,
        url: String?,
        listener: Response.Listener<String>?,
        errorListener: Response.ErrorListener?
    ) : StringRequest(method, url, listener, errorListener) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["X-YouVersion-Client"] = "youversion"
            headers["X-YouVersion-App-Platform"] = "web"
            headers["X-YouVersion-App-Version"] = "1"
            headers["User-Agent"] = "Request-Promise"
            headers["Content-Type"] = "application/json"
            headers["Referer"] = "http://yvapi.youversionapi.com"

            return headers
        }
    }
}