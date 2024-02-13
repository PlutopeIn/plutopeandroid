package com.app.plutope.ui.fragment.card.personal_details.personal_info


import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentPersonalDetailBinding
import com.app.plutope.dialogs.DialogCountryList
import com.app.plutope.model.CountryListModel
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.constant.typeCountryList
import com.app.plutope.utils.date_formate.showDatePicker
import com.app.plutope.utils.date_formate.toAny
import com.app.plutope.utils.date_formate.toCal
import com.app.plutope.utils.date_formate.ymdHMS
import com.app.plutope.utils.loadBannerImage
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Calendar

@AndroidEntryPoint
class PersonalDetail : BaseFragment<FragmentPersonalDetailBinding, PersonalDetailViewModel>() {
    private val personalDetailViewModel: PersonalDetailViewModel by viewModels()
    override fun getViewModel(): PersonalDetailViewModel {
        return personalDetailViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.personalDetailViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal_detail
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setCardProgress(viewDataBinding!!.root, 1, (requireActivity() as BaseActivity))

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.btnContinue?.setOnClickListener {
            findNavController().navigate(PersonalDetailDirections.actionPersonalDetailToAddAddressFragment())
        }

        viewDataBinding!!.imgCalender.setOnClickListener {
            requireContext().showDatePicker(
                { date ->
                    viewDataBinding!!.txtDateOfBirth.tag = date.toAny(ymdHMS)
                    viewDataBinding!!.txtDateOfBirth.text = date.toAny("dd-MM-yyyy")

                },
                maxDate = Calendar.getInstance(),
                selectedDate = if (viewDataBinding!!.txtDateOfBirth.tag != null) viewDataBinding!!.txtDateOfBirth.tag.toString()
                    .toCal() else Calendar.getInstance()
            )

        }


        viewDataBinding!!.layoutCountry.setOnClickListener {
            DialogCountryList.getInstance()
                ?.show(requireContext(), arrayListOf(), typeCountryList) {
                    viewDataBinding!!.txtCountryName.text = it.countryName
                    loadBannerImage(viewDataBinding?.imgCountryFlag!!, it.image)
                }
        }


    }

    private fun getCountryList(): MutableList<CountryListModel> {
        try {
            val `is`: InputStream = resources.openRawResource(R.raw.country_list)
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
                val currencyName = countryObject.getJSONArray("currencies").getString(0)
                val currencyCode = countryObject.getJSONArray("currencies").getString(1)
                val currencySymbol = countryObject.getJSONArray("currencies").getString(2)

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

    override fun setupObserver() {

    }


}