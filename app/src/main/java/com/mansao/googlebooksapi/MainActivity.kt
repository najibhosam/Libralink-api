package com.mansao.googlebooksapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.mansao.googlebooksapi.databinding.ActivityMainBinding
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bookViewModel: BookViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        binding.btnSearch.setOnClickListener {
            searchBook()
        }
    }

    private fun searchBook() {
        binding.progressBar.visibility = View.VISIBLE
        val query = binding.edtInputBook.text.toString()
        val client = AsyncHttpClient()
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query}"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>,
                responseBody: ByteArray
            ) {
                val result = String(responseBody)
                Log.d(TAG, result)
                binding.progressBar.visibility = View.INVISIBLE

                try {
                    val jsonObject = JSONObject(result)
                    val itemsArray = jsonObject.getJSONArray("items")

                    var i = 0
                    var bookTitle = ""
                    var bookAuthor = ""
                    var pages = ""
                    var coverImageUrl = ""

                    while (i < itemsArray.length()) {
                        val book = itemsArray.getJSONObject(i)
                        val volumeInfo = book.getJSONObject("volumeInfo")
                        try {
                            bookTitle = volumeInfo.getString("title")
                            bookAuthor = volumeInfo.getString("authors")
                            pages = volumeInfo.optString("pageCount", "")
                            val imageLinks = volumeInfo.getJSONObject("imageLinks")
                            coverImageUrl = imageLinks.getString("thumbnail")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }

                    bookViewModel.apply {
                        this.bookTitle = bookTitle
                        this.bookAuthor = bookAuthor
                        this.pages = pages
                        this.coverImageUrl = coverImageUrl
                    }

                    updateUI()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                val errorMessage = when (statusCode) {
                    401 -> "StatusCode: Bad Request"
                    403 -> "StatusCode: Forbidden"
                    404 -> "StatusCode: Not Found"
                    else -> " StatusCode: ${error?.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        binding.apply {
            tvTitleResult.text = bookViewModel.bookTitle
            tvAuthorResult.text = bookViewModel.bookAuthor
            tvPages.text = bookViewModel.pages

            // Laden und Anzeigen des Buchcover-Fotos
            Glide.with(this@MainActivity)
                .load(bookViewModel.coverImageUrl)
                .into(tvBookImg)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}