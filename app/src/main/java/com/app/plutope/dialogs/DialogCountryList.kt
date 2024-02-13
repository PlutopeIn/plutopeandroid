package com.app.plutope.dialogs


import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.app.plutope.R
import com.app.plutope.model.CountryListModel
import com.app.plutope.ui.adapter.CountryListAdapter
import com.app.plutope.utils.constant.typeCountryCodeList
import com.app.plutope.utils.constant.typeCountryList
import com.app.plutope.utils.constant.typeCurrencyList
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class DialogCountryList private constructor() {
    companion object {
        var singleInstence: DialogCountryList? = null
        fun getInstance(): DialogCountryList? {
            if (singleInstence == null) {
                singleInstence = DialogCountryList()
            }
            return singleInstence
        }
    }

    private var adapter: CountryListAdapter? = null
    private var alertDialogLocation: BottomSheetDialog? = null
    fun show(
        context: Context?,
        list: MutableList<CountryListModel>,
        listType: Int = typeCountryList,
        unit: (CountryListModel) -> Any
    ) {

        if (alertDialogLocation == null) {
            alertDialogLocation = BottomSheetDialog(
                context!!,
                R.style.Theme_QuinableFacilityApp_BottomSheet_Dialog_Navigation
            )
        }
        alertDialogLocation?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(alertDialogLocation?.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        alertDialogLocation?.window?.attributes = lp
        alertDialogLocation?.behavior?.peekHeight = 2000
        alertDialogLocation?.setCancelable(true)
        alertDialogLocation?.setContentView(R.layout.dialog_country_list)

        val countries = getCountryList(context)
        val rvDialogButtonList =
            alertDialogLocation?.findViewById<RecyclerView>(R.id.rv_country_list)
        adapter = CountryListAdapter(listType) {
            unit.invoke(it)
            alertDialogLocation?.dismiss()

        }
        rvDialogButtonList?.adapter = adapter
        adapter?.submitList(countries)


        var noData = alertDialogLocation?.findViewById<LottieAnimationView>(R.id.no_data_available)
        alertDialogLocation?.findViewById<EditText>(R.id.edt_search)?.doAfterTextChanged {
            filters(it.toString(), countries, listType)
        }

        if (alertDialogLocation != null) {
            if (alertDialogLocation!!.isShowing) {
                alertDialogLocation?.dismiss()
            } else {
                try {
                    alertDialogLocation?.show()
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun filters(text: String, list: MutableList<CountryListModel>, listType: Int) {
        val filterList = ArrayList<CountryListModel>()
        for (i in list)
            when (listType) {
                typeCountryList -> {
                    if (i.countryName?.lowercase()!!.contains(text.lowercase())) {
                        filterList.add(i)
                    }
                }

                typeCountryCodeList -> {
                    if (i.code?.lowercase()!!
                            .contains(text.lowercase()) || i.countryName?.lowercase()!!
                            .contains(text.lowercase())
                    ) {
                        filterList.add(i)
                    }

                }

                typeCurrencyList -> {
                    if (i.currencyCode?.lowercase()!!.contains(text.lowercase())) {
                        filterList.add(i)
                    }

                }
            }

        /*   if (i.countryName?.lowercase()!!.contains(text.lowercase()) || i.currencyName?.lowercase()!!.contains(text.lowercase())) {
               filterList.add(i)
           }*/

        if (filterList.isEmpty()) {
            alertDialogLocation?.findViewById<LottieAnimationView>(R.id.no_data_available)?.visibility =
                View.VISIBLE
        } else {
            alertDialogLocation?.findViewById<LottieAnimationView>(R.id.no_data_available)?.visibility =
                View.GONE
        }

        adapter?.submitList(filterList)

    }


    fun dismiss() {
        if (alertDialogLocation != null) {
            alertDialogLocation?.dismiss()
        }
    }

    private fun getCountryList(context: Context?): MutableList<CountryListModel> {
        try {
            val `is`: InputStream = context?.resources!!.openRawResource(R.raw.country_list)
            val reader = BufferedReader(InputStreamReader(`is`))
            val json = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                json.append(line)
            }
            reader.close()
            val countryArray = JSONArray(json.toString())
            val countryList = arrayListOf<CountryListModel>()
            for (i in 0 until countryArray.length()) {
                val countryObject = countryArray.getJSONObject(i)

                val countryName = countryObject.getString("name")
                val images = countryObject.getJSONObject("flags").getString("png")
                val countryCode = countryObject.getJSONArray("callingCodes").getString(0)

                var currencyName = ""
                var currencyCode = ""
                var currencySymbol = ""
                if (countryObject.has("currencies")) {

                    val currency = countryObject.getJSONArray("currencies")
                    for (i in 0 until currency.length()) {
                        currencyCode = currency.getJSONObject(i).getString("code")
                        currencyName = currency.getJSONObject(i).getString("name")
                        currencySymbol = currency.getJSONObject(i).getString("symbol")
                    }

                }
                countryList.add(
                    CountryListModel(
                        countryName = countryName,
                        image = images,
                        code = countryCode,
                        currencyName = currencyName,
                        currencyCode = currencyCode,
                        currencySymbol = currencySymbol
                    )
                )
            }
            return countryList

        } catch (e: IOException) {
            e.printStackTrace()
            return arrayListOf()


        } catch (e: JSONException) {
            e.printStackTrace()
            return arrayListOf()
        }

    }


}