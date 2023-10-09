package com.neb.linko.ui.search

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivitySearchBinding
import com.neb.linko.models.ItemModel
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.search.adapters.HistoryAdapter
import com.neb.linko.ui.search.adapters.SearchStoreAdapter
import com.neb.linko.ui.store.StoreActivity
import javax.inject.Inject

class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var searchViewModel: SearchViewModel

    lateinit var searchBinding: ActivitySearchBinding
    lateinit var searchStoreAdapter: SearchStoreAdapter
    lateinit var stores: ArrayList<StoreModel>
    lateinit var items: ArrayList<ItemModel>
    lateinit var filterStoresList: ArrayList<StoreModel>
    lateinit var filterItemsList: ArrayList<ItemModel>
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var historys: ArrayList<String>
    lateinit var historyAdapter: HistoryAdapter
    lateinit var languageCache: LanguageCache
    var country = ""

    var storeOrItem = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)
        App.appComponent.searchActivity(this)

        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE)!!)


        stores = ArrayList()
        items = ArrayList()
        filterStoresList = ArrayList()
        filterItemsList = ArrayList()
        historys = ArrayList()

        sharedPreferences = getSharedPreferences("Base", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        getHistory()

        country = intent.getStringExtra("country") ?: "KWT"

        searchBinding.storesBtn.setOnClickListener {
            storeOrItem = true
            search()
            searchBinding.itemsBtn.setBackgroundResource(R.drawable.report_corner_bg)
            searchBinding.storesBtn.setBackgroundResource(R.drawable.report_corner_bg_selected)
            transfer()
        }
        searchBinding.itemsBtn.setOnClickListener {
            storeOrItem = false
            search()
            searchBinding.itemsBtn.setBackgroundResource(R.drawable.report_corner_bg_selected)
            searchBinding.storesBtn.setBackgroundResource(R.drawable.report_corner_bg)
            transfer()
        }

        searchBinding.trashBtn.setOnClickListener {
            clearHistory()
        }

        searchBinding.searchBox.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search()
                closedKeyboard()
                if (searchBinding.searchBox.text.toString() != "") {
                    addHistory(searchBinding.searchBox.text.toString())
                }
                return@OnEditorActionListener true
            }
            false
        })

        searchBinding.searchRv.setOnTouchListener { v, event ->
            closedKeyboard()
            false
        }

        searchBinding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                search()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        transfer()

        searchBinding.cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun getHistory() {
        val a = sharedPreferences.getString("history", "")

        if (a != "") {
            val type = object : TypeToken<List<String>>() {}.type
            val fromJson = Gson().fromJson<List<String>>(a, type)
            historys.clear()
            historys.addAll(fromJson)
        }

        historyAdapter = HistoryAdapter(historys, object : HistoryAdapter.HistoryClick {
            override fun click(str: String) {
                searchBinding.searchBox.setText(str)
                searchBinding.searchBox.setSelection(searchBinding.searchBox.text.toString().length)
            }
        })

        if (historys.isNotEmpty()) {
            searchBinding.historyView.visibility = View.VISIBLE
        } else {
            searchBinding.historyView.visibility = View.GONE
        }

        searchBinding.historyListView.adapter = historyAdapter
    }

    fun addHistory(str: String) {
        var s = true
        for (h in historys) {
            if (h == str) {
                s = false
                break
            }
        }
        if (s) {
            searchBinding.historyView.visibility = View.VISIBLE
            historys.add(str)
            editor.putString("history", Gson().toJson(historys))
            editor.commit()
            historyAdapter.notifyDataSetChanged()
        }
    }

    fun clearHistory() {
        historyAdapter.notifyDataSetChanged()
        historys.clear()
        editor.putString("history", "")
        editor.commit()
        searchBinding.historyView.visibility = View.GONE
    }

    private fun search() {
        if (storeOrItem) {
            if (stores.isEmpty()) {
                getStores {
                    if (it == null) {
                        searchingStores(
                            stores,
                            searchBinding.searchBox.text?.trimStart().toString()
                        )
                    }
                }
            } else {
                searchingStores(
                    if (searchBinding.searchBox.text?.trimStart()
                            .toString() != ""
                    ) stores else ArrayList(),
                    searchBinding.searchBox.text?.trimStart().toString()
                )
            }
        } else {
            if (items.isEmpty()) {
                getItems {
                    if (it == null) {
                        searchingItems(items, searchBinding.searchBox.text?.trimStart().toString())
                    }
                }
            } else {
                searchingItems(
                    if (searchBinding.searchBox.text?.trimStart()
                            .toString() != ""
                    ) items else ArrayList(),
                    searchBinding.searchBox.text?.trimStart().toString()
                )
            }
        }
    }

    private fun searchingItems(items: ArrayList<ItemModel>, str: String) {
        filterItemsList.clear()
        for (item in items) {
            if (item.country == country || country == "") {
                if ((item.name ?: "").toLowerCase().contains(str.toLowerCase()) || (item.nameArabic
                        ?: "").toLowerCase().contains(str.toLowerCase()) || (item.details
                        ?: "").toLowerCase().contains(str.toLowerCase()) || (item.detailsArabic
                        ?: "").toLowerCase().contains(str.toLowerCase())
                ) {
                    filterItemsList.add(item)
                }
//                if ((if (languageCache.getLanguage()) item.name ?: "" else item.nameArabic ?: "").toLowerCase().contains(str.toLowerCase()) || (if (languageCache.getLanguage()) item.details ?: "" else item.detailsArabic ?: "").toLowerCase().contains(str.toLowerCase())
//                ) {
//                    filterItemsList.add(item)
//                }
            }
        }
        searchStoreAdapter.notifyDataSetChanged()
    }

    private fun searchingStores(stores: ArrayList<StoreModel>, str: String) {
        filterStoresList.clear()
        for (store in stores) {
            if (store.country == country || country == "") {
                if ((store.name ?: "").toLowerCase().contains(str.toLowerCase()) || (store.nameArabic
                        ?: "").toLowerCase().contains(str.toLowerCase()) || (store.details
                        ?: "").toLowerCase().contains(str.toLowerCase()) || (store.detailsArabic
                        ?: "").toLowerCase().contains(str.toLowerCase())
                ) {
                    filterStoresList.add(store)
                }
//                if ((if (languageCache.getLanguage()) store.name ?: "" else store.nameArabic ?: "").toLowerCase().contains(str.toLowerCase()) || (if (languageCache.getLanguage()) store.details ?: "" else store.detailsArabic ?: "").toLowerCase().contains(str.toLowerCase())
//                ) {
//                    filterStoresList.add(store)
//                }
            }
        }
        searchStoreAdapter.notifyDataSetChanged()
    }

    private fun getStores(callback: (Exception?) -> Unit) {
        searchViewModel.getStores(this).observe(this, Observer {
            if (it != null) {
                stores.clear()
                stores.addAll(it)
                filterStoresList.clear()
                filterStoresList.addAll(it)
                searchStoreAdapter.notifyDataSetChanged()
                callback(null)
            }
        })
    }

    private fun transfer() {
        searchStoreAdapter =
            SearchStoreAdapter(
                languageCache.getLanguage(),
                filterStoresList,
                filterItemsList,
                storeOrItem,
                this,
                object : SearchStoreAdapter.SearchClickListener {
                    override fun clickItem(itemModel: ItemModel) {
                        var storeModel: StoreModel? = null
                        for (store in stores) {
                            if (itemModel.storeKey == store.key) {
                                storeModel = store
                                break
                            }
                        }
                        if (storeModel != null) {
                            openStore(storeModel)
                        } else {
                            Toast.makeText(
                                this@SearchActivity,
                                "store information is incomplete",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        if (searchBinding.searchBox.text.toString() != "") {
                            addHistory(searchBinding.searchBox.text.toString())
                        }
                    }

                    override fun clickStore(storeModel: StoreModel) {
                        openStore(storeModel)
                        if (searchBinding.searchBox.text.toString() != "") {
                            addHistory(searchBinding.searchBox.text.toString())
                        }
                    }
                })
        searchBinding.searchRv.adapter = searchStoreAdapter
    }

    private fun openStore(storeModel: StoreModel) {
        val intent = Intent(this@SearchActivity, StoreActivity::class.java)
        val b =
            ActivityOptions.makeSceneTransitionAnimation(this@SearchActivity)
                .toBundle()
        try {
            intent.putExtra("store", Gson().toJson(storeModel))
            startActivity(intent, b)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@SearchActivity,
                "store information is incomplete",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onResume() {
        getStores {
            search()
        }

        getItems {
            search()
        }
        searchBinding.searchBox.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        if (languageCache.getLanguage()) {
            searchBinding.optionView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            searchBinding.searchBox.textDirection = View.TEXT_DIRECTION_LTR
            searchBinding.searchBox.hint = "Search..."
            searchBinding.cancelBtn.text = "Cancel"
            searchBinding.storesBtn.text = "Look up stores"
            searchBinding.itemsBtn.text = "Look up items"
            searchBinding.historyTv.text = "Search History"
        } else {
            searchBinding.optionView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            searchBinding.searchBox.textDirection = View.TEXT_DIRECTION_RTL
            searchBinding.searchBox.hint = "ابحث…"
            searchBinding.cancelBtn.text = "إلغاء"
            searchBinding.storesBtn.text = "ابحث عن متجر"
            searchBinding.itemsBtn.text = "ابحث عن منتج"
            searchBinding.historyTv.text = "تاريخ البحث"
            setTextFonts()
        }

        super.onResume()
    }

    private fun setTextFonts() {
        val typeface = ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
        searchBinding.searchBox.typeface = typeface
        searchBinding.cancelBtn.typeface = typeface
        searchBinding.storesBtn.typeface = typeface
        searchBinding.itemsBtn.typeface = typeface
        searchBinding.historyTv.typeface = typeface
    }

    private fun getItems(callback: (Exception?) -> Unit) {
        searchViewModel.getItems().observe(this, Observer {
            if (it != null) {
                items.clear()
                items.addAll(it)
                filterItemsList.clear()
                filterItemsList.addAll(it)
                searchStoreAdapter.notifyDataSetChanged()
                callback(null)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        closedKeyboard()
    }

    private fun closedKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}