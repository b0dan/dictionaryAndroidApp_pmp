package com.pmp.dictionary

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var dictionaryAdapter: DictionaryAdapter
    private var dictionary: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        copyFileFromAssetsToInternalStorage(this, "en_mk_recnik.txt")

        dictionary = populateDictionaryFromTxtFile(this, "en_mk_recnik.txt")

        dictionaryAdapter = DictionaryAdapter(dictionary)

        val rvDictionaryWords = findViewById<RecyclerView>(R.id.rvDictionaryWords)
        rvDictionaryWords.adapter = dictionaryAdapter
        rvDictionaryWords.layoutManager = LinearLayoutManager(this)

        val searchView = findViewById<SearchView>(R.id.svSearchDictionary)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterDictionary(newText)
                return true
            }
        })

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showAddTranslationDialog()
        }
    }

    private fun populateDictionaryFromTxtFile(context: Context, filename: String): MutableMap<String, String> {
        val dictionary = mutableMapOf<String, String>()

        try {
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split(",") ?: continue
                val englishWord = parts[0].trim()
                val macedonianTranslation = parts[1].trim()
                dictionary[englishWord] = macedonianTranslation
            }
            reader.close()

            val file = File(context.filesDir, filename)
            if (file.exists()) {
                val internalReader = BufferedReader(FileReader(file))
                var internalLine: String?
                while (internalReader.readLine().also { internalLine = it } != null) {
                    val parts = internalLine?.split(",") ?: continue
                    val englishWord = parts[0].trim()
                    val macedonianTranslation = parts[1].trim()
                    dictionary[englishWord] = macedonianTranslation
                }
                internalReader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dictionary
    }


    private fun filterDictionary(query: String?) {
        val filteredDictionary = if (query.isNullOrEmpty()) {
            dictionary
        } else {
            dictionary.filter { it.key.contains(query, ignoreCase = true) || it.value.contains(query, ignoreCase = true) }
                .toMutableMap()
        }
        dictionaryAdapter.updateDictionary(filteredDictionary)
    }

    private fun showAddTranslationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view, null)
        val englishEditText = dialogView.findViewById<EditText>(R.id.etEnglish)
        val macedonianEditText = dialogView.findViewById<EditText>(R.id.etMacedonian)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Translation")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val englishText = englishEditText.text.toString()
                val macedonianText = macedonianEditText.text.toString()

                addTranslationToFile(englishText, macedonianText)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addTranslationToFile(englishText: String, macedonianText: String) {
        val file = File(filesDir, "en_mk_recnik.txt")
        val fileContent = "$englishText, $macedonianText\n"

        try {
            FileOutputStream(file, true).use {
                it.write(fileContent.toByteArray())
            }

            dictionary = populateDictionaryFromTxtFile(this, "en_mk_recnik.txt")

            dictionaryAdapter.updateDictionary(dictionary)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun copyFileFromAssetsToInternalStorage(context: Context, filename: String) {
        val assetManager = context.assets
        val inputFile = File(context.filesDir, filename)
        if (!inputFile.exists()) {
            try {
                val inputStream = assetManager.open(filename)
                val outputStream = FileOutputStream(inputFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
