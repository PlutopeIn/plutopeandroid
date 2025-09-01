package com.app.plutope.ui.fragment.transactions.buy.graph


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentGraphDetailBinding
import com.app.plutope.model.CoinGeckoMarketsResponse
import com.app.plutope.model.CustomMarkerView
import com.app.plutope.model.TimeSelection
import com.app.plutope.ui.adapter.GraphPriceTimeAdapter
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.constant.COIN_GEKO_COIN_DETAIL
import com.app.plutope.utils.constant.COIN_GEKO_MARKETPRICE
import com.app.plutope.utils.constant.COIN_GEKO_MARKET_API
import com.app.plutope.utils.convertToBillions
import com.app.plutope.utils.convertToMillions
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.setBalanceDoubleText
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showSnackBar
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ViewPortHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GraphDetail : BaseFragment<FragmentGraphDetailBinding, GraphDetailViewModel>() {

    private var graphObj: CoinGeckoMarketsResponse? = null
    private val graphDetailViewModel: GraphDetailViewModel by viewModels()
    val args: GraphDetailArgs by navArgs()
    private var customMarker: CustomMarkerView? = null

    private var high = Double.MIN_VALUE
    private var low = Double.MAX_VALUE

    override fun getViewModel(): GraphDetailViewModel {
        return graphDetailViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.graphDetailViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_graph_detail
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {

        viewDataBinding!!.model = args.tokenModel

        setDetail()

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.imgNotification.setOnClickListener {
            findNavController().safeNavigate(GraphDetailDirections.actionGraphDetailToNotification())
        }

        setTimeSelectionData()
        if (!args.tokenModel.isCustomTokens) {
            graphDetailViewModel.executeGetGraphMarketResponse("${COIN_GEKO_MARKETPRICE}${args.tokenModel.tokenId.lowercase()}/market_chart?id=${args.tokenModel.tokenId}&&vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&days=1&interval=")
            graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_MARKET_API?vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&ids=${args.tokenModel.tokenId}")
            //  graphDetailViewModel.executeGetMarketResponse("$COIN_GEKO_PLUTO_PE_SERVER_URL${preferenceHelper.getSelectedCurrency()?.code}&ids=${args.tokenModel.tokenId}")
        } else {
            viewDataBinding?.constRoot?.showSnackBar(getString(R.string.graph_details_not_available))
        }
    }

    private fun setDetail() {
        //viewDataBinding?.txtCoinName?.text = args.tokenModel.t_name
        viewDataBinding?.txtNetworkName?.text = args.tokenModel.t_type
        viewDataBinding?.txtToolbarTitle?.text = args.tokenModel.t_name


        Glide.with(requireContext()).load(args.tokenModel.t_logouri)
            .into(viewDataBinding?.imgCoin!!)
        graphDetailViewModel.executeGetCoinDetailResponse(COIN_GEKO_COIN_DETAIL + args.tokenModel.tokenId)

        val priceDouble = args.tokenModel.t_price.toDoubleOrNull() ?: 0.0
        val priceText = String.format("%.4f", priceDouble)
        val percentChange = args.tokenModel.t_last_price_change_impact.toDoubleOrNull() ?: 0.0
        val color = if (percentChange < 0.0) context?.resources!!.getColor(
            R.color.red,
            null
        ) else context?.resources!!.getColor(R.color.green_00A323, null)

        val pricePercent = if (percentChange < 0.0) String.format(
            "%.2f",
            percentChange
        ) else "+" + String.format("%.2f", percentChange)
        viewDataBinding?.txtCoinPrice?.text =
            preferenceHelper.getSelectedCurrency()?.symbol + "" + priceText
        viewDataBinding?.txtCoinMargin?.text = "$pricePercent%"
        viewDataBinding?.txtCoinMargin?.setTextColor(color)


        //viewDataBinding?.txtCoinBalance?.text = preferenceHelper.getSelectedCurrency()?.symbol + "" + priceText

    }

    private fun setTimeSelectionData() {
        val adapter = GraphPriceTimeAdapter {

            graphDetailViewModel.executeGetGraphMarketResponse("${COIN_GEKO_MARKETPRICE}${args.tokenModel.tokenId.lowercase()}/market_chart?id=${args.tokenModel.tokenId.lowercase()}&&vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&days=${it.interval}&interval=")

        }
        val list = mutableListOf<TimeSelection>()
        list.add(TimeSelection("1H", true, "1hour"))
        list.add(TimeSelection("1D", false, "1"))
        list.add(TimeSelection("1W", false, "7"))
        list.add(TimeSelection("1M", false, "30"))
        list.add(TimeSelection("1Y", false, "365"))
        list.add(TimeSelection("All", false, "max"))
        adapter.submitList(list)
        viewDataBinding?.recyclerTimeSelect?.adapter = adapter
    }


    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                graphDetailViewModel.getGetGraphMarketResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {

                            // if (it.data.prices.isNotEmpty())

                            val result = it.data?.prices
                            loge("DataChart", "$result")

                            val hrSelected =
                                (viewDataBinding?.recyclerTimeSelect?.adapter as GraphPriceTimeAdapter).currentList.filter { it.isSelected && it.interval == "1hour" }
                            val dataList = if (hrSelected.isNotEmpty()) {
                                if (result?.size!! >= 12) {
                                    result.subList(result.size - 12, result.size)
                                } else {
                                    result
                                }
                            } else {
                                result
                            }

                            viewDataBinding?.chartView?.apply {
                                data = null
                                marker = null
                                clear()
                                setOnChartValueSelectedListener(null)
                                notifyDataSetChanged()
                            }



                            setChartDetailGraph(dataList)

                            // setCandleChartData(dataList)


                        }

                        is NetworkState.Loading -> {

                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                graphDetailViewModel.getGetMarketResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            val result = it.data
                            loge(
                                "HelLoResult",
                                "${args.tokenModel.t_symbol.lowercase()} :: ${args.tokenModel.chain?.symbol?.lowercase()} ::$result"
                            )

                            if (result!!.isNotEmpty()) {
                                graphObj =
                                    result.single { it.symbol.lowercase() == args.tokenModel.t_symbol.lowercase() || it.symbol.lowercase() == args.tokenModel.chain?.symbol?.lowercase() }

                                loge("HelLoResult2", "graphObj :: $graphObj")
                                setMarketCapDetail(graphObj)
                            }

                            // graphDetailViewModel.executeGetGraphMarketResponse("${COIN_GEKO_MARKETPRICE}${graphObj?.id?.lowercase()}/market_chart?id=${graphObj?.id}&&vs_currency=${preferenceHelper.getSelectedCurrency()?.code}&days=1")

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                graphDetailViewModel.getGetCoinDetailResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            val result = it.data
                            if (result != null) {

                                viewDataBinding?.txtCoinAbout?.apply {
                                    text = Html.fromHtml(
                                        result.description.en,
                                        Html.FROM_HTML_MODE_LEGACY
                                    )
                                    movementMethod = LinkMovementMethod.getInstance()
                                }


                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            )
                                .show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setMarketCapDetail(graphObj: CoinGeckoMarketsResponse?) {
        /*viewDataBinding?.txtMarketCapValue?.text =
            preferenceHelper.getSelectedCurrency()?.symbol + graphObj?.market_cap.toString()*/
        val marketCap = graphObj?.market_cap?.toDouble() ?: 0.0
        val marketCapInBillions = convertToBillions(marketCap)
        val currency = preferenceHelper.getSelectedCurrency()?.symbol ?: ""
        viewDataBinding?.txtMarketCapValue?.text = "$currency$marketCapInBillions"

        /*viewDataBinding?.txtVolumeValue?.text =
            preferenceHelper.getSelectedCurrency()?.symbol + graphObj?.total_volume.toString()*/
        val totalVolumeInMillions = convertToMillions(graphObj?.total_volume?.toDouble() ?: 0.0)
        val currencySymbol = preferenceHelper.getSelectedCurrency()?.symbol ?: ""
        viewDataBinding?.txtVolumeValue?.text = "$currencySymbol$totalVolumeInMillions"

        viewDataBinding?.txtCirculatingSupplyValue?.text =
            graphObj?.circulating_supply.toString() + " " + args.tokenModel.t_symbol
        viewDataBinding?.txtTotalSupplyValue?.text =
            if (graphObj?.total_supply.toString() != "null") graphObj?.total_supply.toString() else "0.0" + " " + args.tokenModel.t_symbol


        viewDataBinding?.txtTotalSupplyValue?.text =
            if (graphObj?.total_supply.toString() != "null") graphObj?.total_supply.toString() else "0.0" + " " + args.tokenModel.t_symbol

        viewDataBinding?.txtCoinBalance?.text = setBalanceDoubleText(
            args.tokenModel.t_balance.toDouble(),
            args.tokenModel.t_symbol.toString(),
            7
        )
    }

    private fun setCandleChartData(result: List<List<Double>>?) {

        val priceList = ArrayList<Double>().apply {
            result?.forEachIndexed { index, entryData ->
                val x = index.toFloat()
                val price = entryData[1]
                add(price)
            }
        }

        val entries = ArrayList<CandleEntry>()
        priceList.forEachIndexed { index, price ->

            val shadowH =
                if (index == 0) priceList[0] else if (index == priceList.lastIndex) priceList[index] else priceList[index + 1]
            val shadowL = priceList[index]
            val open =
                if (index == 0) priceList[0] else if (index == priceList.lastIndex) priceList[index] else priceList[index + 1]
            val close = price
            updateHighLow(price)
            entries.add(
                CandleEntry(
                    index.toFloat(),
                    high.toFloat(),
                    low.toFloat(),
                    open.toFloat(),
                    close.toFloat()
                )
            )
            high = price
            low = price
        }


        /* entries.add(CandleEntry(0f, 390f, 400f, 390f, 400f))
         entries.add(CandleEntry(1f, 400f, 430f, 400f, 430f))
         entries.add(CandleEntry(2f, 430f, 450f, 430f, 450f))
         entries.add(CandleEntry(3f, 450f, 480f, 450f, 420f))
         entries.add(CandleEntry(4f, 420f, 500f, 420f, 500f))*/

        val dataSet = CandleDataSet(entries, "${args.tokenModel.t_name}")
        dataSet.color = Color.rgb(80, 80, 80)
        dataSet.shadowColor = Color.DKGRAY
        dataSet.shadowWidth = 0.7f
        dataSet.decreasingColor = Color.rgb(122, 242, 84)
        dataSet.decreasingPaintStyle = Paint.Style.FILL
        dataSet.increasingColor = Color.RED
        dataSet.increasingPaintStyle = Paint.Style.FILL
        dataSet.neutralColor = Color.BLUE

        val data = CandleData(dataSet)
        viewDataBinding?.candleChartView?.data = data
        viewDataBinding?.candleChartView?.invalidate()

        val description = Description()
        description.text = "${args.tokenModel.t_name}"
        viewDataBinding?.candleChartView?.description = description
        viewDataBinding?.candleChartView?.setMaxVisibleValueCount(50)
        viewDataBinding?.candleChartView?.setPinchZoom(true)
        viewDataBinding?.candleChartView?.isDoubleTapToZoomEnabled = false


        val yAxis: YAxis = viewDataBinding?.candleChartView?.axisLeft!!
        val rightAxis: YAxis = viewDataBinding?.candleChartView?.axisRight!!
        yAxis.setDrawGridLines(false)
        rightAxis.setDrawGridLines(false)
        viewDataBinding?.candleChartView?.requestDisallowInterceptTouchEvent(true)

        val xAxis: XAxis = viewDataBinding?.candleChartView?.xAxis!!

        xAxis.setDrawGridLines(false) //disable x axis grid lines
        xAxis.setDrawLabels(false)
        rightAxis.textColor = ResourcesCompat.getColor(resources, R.color.black, null)
        yAxis.setDrawLabels(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setAvoidFirstLastClipping(true)
        val l: Legend = viewDataBinding?.candleChartView?.legend!!
        l.isEnabled = false

    }

    private fun updateHighLow(newPrice: Double) {
        if (newPrice > high) {
            high = newPrice
        }
        if (newPrice < low) {
            low = newPrice
        }
    }

    private fun setChartDetailGraph(result: List<List<Double>>?) {
        viewDataBinding?.chartView?.refreshDrawableState()
        val prices: MutableList<Pair<Long, Float>> = mutableListOf()
        result?.forEach { prices.add(Pair(it[0].toLong(), it[1].toFloat())) }
        val entryArrayList = ArrayList<Entry>().apply {
            result?.forEachIndexed { index, entryData ->
                val x = index.toFloat()
                val y = entryData[1]
                add(Entry(x, y.toFloat()))
            }
        }


        val minPrice: Pair<Long, Float> = Pair(
            prices.minByOrNull { it.second }?.first ?: 0L,
            prices.minByOrNull { it.second }?.second ?: 0f
        )
        val maxPrice = Pair(
            prices.maxByOrNull { it.second }?.first ?: 0L,
            prices.maxByOrNull { it.second }?.second ?: 0f
        )
        val valueFormatter1 = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                axis!!.mEntries[0] = minPrice.second
                axis.mEntries[1] = maxPrice.second
                return getFormattedValue(value)
            }

            override fun getFormattedValue(value: Float): String {
                if (value == minPrice.second || value == maxPrice.second) {

                    return "${preferenceHelper.getSelectedCurrency()?.symbol}${
                        String.format(
                            "%.2f",
                            value
                        )
                    }"
                }
                return ""
            }

            override fun getFormattedValue(
                value: Float,
                entry: Entry?,
                dataSetIndex: Int,
                viewPortHandler: ViewPortHandler?
            ): String {
                if (value == minPrice.second || value == maxPrice.second) {
                    return "${preferenceHelper.getSelectedCurrency()?.symbol}${
                        String.format(
                            "%.2f",
                            value
                        )
                    }"
                }
                return ""
            }


        }

        customMarker = CustomMarkerView(requireContext(), R.layout.layout_textview_label)
        var dataset: LineDataSet? = null


        dataset = LineDataSet(entryArrayList, null).apply {
            setDrawCircles(false)
            lineWidth = 2f
            color = Color.GREEN
            //  fillColor = resources.getColor(R.color.light_green_22d1ee, null)
            setCircleColor(Color.YELLOW)
            circleRadius = 2f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(false)
            // Set text size directly on the LineDataSet
            valueTextSize = 8f // Set your desired text size here
            valueFormatter = valueFormatter1

            setDrawValues(true)
        }


        val lineData = LineData(dataset).apply {
            setValueTextSize(10f)
            setValueTextColor(Color.WHITE)

        }




        viewDataBinding?.chartView?.apply {
            marker = customMarker
            axisLeft.setDrawLabels(false)
            axisRight.setDrawLabels(false)
            xAxis.setDrawLabels(false)
            setScaleEnabled(false)
            xAxis?.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            axisLeft.valueFormatter = valueFormatter1
            axisRight.valueFormatter = valueFormatter1

        }

        val description = Description()
        description.text = "${args.tokenModel.t_name}"
        viewDataBinding?.chartView?.description = description

        viewDataBinding?.chartView?.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val price = "${preferenceHelper.getSelectedCurrency()?.symbol}${
                        String.format(
                            "%.2f",
                            e.y
                        )
                    }"

                    viewDataBinding?.chartView?.marker = customMarker
                    viewDataBinding?.chartView?.refreshDrawableState()

                    // You can also update the marker's content based on the selected value
                    customMarker?.refreshContent(e, h)
                    //viewDataBinding?.txtCoinBalance?.text = price

                }
            }

            override fun onNothingSelected() {
                // Remove the default label when nothing is selected
                viewDataBinding?.chartView?.highlightValue(null)
                valueFormatter1.getFormattedValue(minPrice.second)
            }
        })


        viewDataBinding?.chartView?.data = lineData
        viewDataBinding?.chartView?.axisLeft?.spaceBottom = 20f
        viewDataBinding?.chartView?.setExtraOffsets(10f, 10f, 10f, 10f)
        viewDataBinding?.chartView?.invalidate()
    }


}