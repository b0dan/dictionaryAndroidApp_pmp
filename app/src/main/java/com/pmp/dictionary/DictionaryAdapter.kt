package com.pmp.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DictionaryAdapter (
    private var dictionary: MutableMap<String, String>
) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {

    class DictionaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
        return DictionaryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dictionary, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
        val key = dictionary.keys.elementAt(position)
        val value = dictionary[key]

        holder.itemView.apply {
            findViewById<TextView>(R.id.tvDictionaryEnglishWord).text = key
            findViewById<TextView>(R.id.tvDictionaryMacedonianWord).text = value
        }
    }

    override fun getItemCount(): Int {
        return dictionary.size
    }

    fun updateDictionary(newDictionary: MutableMap<String, String>) {
        dictionary = newDictionary
        notifyDataSetChanged()
    }
}