package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.app.plutope.utils.loge
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonToken
import kotlinx.parcelize.Parcelize
import org.json.JSONArray

@Keep
data class TransactionHistoryDetail(
    @SerializedName("data")
    val result: List<TransactionDetail>
)

@Keep
@Parcelize
data class TransactionDetail(
    val amount: String,
    val chainFullName: String,
    val chainShortName: String,
    val confirm: String,
    //val contractDetails: List<Any>,
    val errorLog: String,
    val gasLimit: String,
    val gasPrice: String,
    val gasUsed: String,
    val height: String,
    val index: String,
    val inputData: String,
    val inputDetails: List<InputDetail>,
    val methodId: String,
    val nonce: String,
    val outputDetails: List<OutputDetail>,
    val state: String,
    val tokenTransferDetails: List<TokenTransferDetail>,
    val totalTransactionSize: String,
    val transactionSymbol: String,
    val transactionTime: String,
    val transactionType: String,
    val txfee: String,
    val txid: String,
    val virtualSize: String,
    val weight: String
) : Parcelable

@Keep
@Parcelize
data class InputDetail(
    val amount: String,
    val inputHash: String,
    val isContract: Boolean,
    val tag: String
) : Parcelable

@Keep
@Parcelize
data class OutputDetail(
    val amount: String,
    val isContract: Boolean,
    val outputHash: String,
    val tag: String
) : Parcelable

@Keep
@Parcelize
data class TokenTransferDetail(
    val amount: String,
    val from: String,
    val index: String,
    val isFromContract: Boolean,
    val isToContract: Boolean,
    val symbol: String,
    val to: String,
    val token: String,
    val tokenContractAddress: String,
    val tokenId: String
) : Parcelable


@Keep
@Parcelize
data class TransactionModelDApp(
    val hash: String = "",
    val iconUrl: String? = "",
    val exchange: String = "",
    val timestamp: String = "",
    val transactionDetails: List<TransactionDetailDApp> = arrayListOf(),
    val chainId: String = "",
    val transactionType: String = ""
) : Parcelable

@Keep
@Parcelize
data class TransactionDetailDApp(
    var data: String = "",
    var from: String = "",
    var gas: String = "0",
    var to: String = "",
    var value: String = "0"
) : Parcelable


fun parseData(inputList: ArrayList<String?>): TransactionModelDApp {
    val hash = inputList[0] as String
    val iconUrl = inputList[1]
    val exchange = inputList[2] as String
    val timestamp = (inputList[3] as String)
    val transactionDetails = parseTransactionDetails(inputList[4]!!)
    val chainId = (inputList[5] as String)
    val transactionType = inputList[6] as String

    return TransactionModelDApp(
        hash,
        iconUrl,
        exchange,
        timestamp,
        transactionDetails,
        chainId,
        transactionType
    )
}

/*fun parseTransactionDetails(jsonString: String): List<TransactionDetailDApp> {
    return try {
        Gson().fromJson(jsonString, Array<TransactionDetailDApp>::class.java)?.toList()!!
    } catch (e: Exception) {
        e.printStackTrace()
        val model = TransactionDetailDApp()
        listOf(model)


    }
}*/

fun parseTransactionDetails(jsonString: String): List<TransactionDetailDApp> {
    return try {
        val jsonArray = JSONArray(jsonString)

        loge("jsonString=>", jsonString + "  :: $jsonArray")

        if (jsonArray.length() == 2) {
            // Parse the JSON array

            // Extract addresses directly from the JSON array
            val toAddress = jsonArray.getString(0)  // First element is the "to" address
            val fromAddress = jsonArray.getString(1) // Second element is the "from" address

            // Create a single TransactionDetailDApp object with the extracted addresses
            val model = TransactionDetailDApp(
                to = toAddress,
                from = fromAddress
            )

            listOf(model)
        } else {

            val gson = GsonBuilder()
                .registerTypeAdapter(TransactionDetailDApp::class.java, TransactionDetailAdapter())
                .create()


            gson.fromJson(jsonString, Array<TransactionDetailDApp>::class.java)?.toList()!!
        }


    } catch (e: Exception) {
        e.printStackTrace()
        val model = TransactionDetailDApp()
        listOf(model)
    }
}


class TransactionDetailAdapter : TypeAdapter<TransactionDetailDApp>() {


    override fun write(out: com.google.gson.stream.JsonWriter?, value: TransactionDetailDApp?) {
        //Not yet implemented
    }

    override fun read(reader: com.google.gson.stream.JsonReader?): TransactionDetailDApp {
        val model = TransactionDetailDApp()
        reader?.beginObject()
        while (reader!!.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "data" -> model.data = reader.nextString()
                "from" -> model.from = reader.nextString()
                "gas" -> model.gas =
                    if (reader.peek() == JsonToken.STRING) reader.nextString() else "0"

                "to" -> model.to = reader.nextString()
                "value" -> model.value =
                    if (reader.peek() == JsonToken.STRING) reader.nextString() else "0"

                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return model
    }
}

