package org.intelehealth.app.ui.billgeneration.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityBillCreationBinding
import org.intelehealth.app.ui.billgeneration.models.BillDetails
import org.intelehealth.app.ui.billgeneration.models.BillTestsChargeModel
import org.intelehealth.app.ui.billgeneration.repository.BillRepository
import org.intelehealth.app.ui.billgeneration.utils.BillRate
import org.intelehealth.app.ui.billgeneration.utils.VisitType
import org.intelehealth.app.utilities.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BillGenerationViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val sessionManager = SessionManager(appContext)
    private val repository = BillRepository(sessionManager, appContext)

    private val _patientDetails = MutableLiveData<String>()
    val patientDetails: LiveData<String> get() = _patientDetails

    private val mutablePaymentStatus = MutableLiveData<String>()
    val paymentStatusValue: LiveData<String> get() = mutablePaymentStatus
    var finalBillPath: String? = null
    var isBillGenerated: Boolean = false

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage
    lateinit var bitmap: Bitmap

    suspend fun confirmBill(billDetails: BillDetails): Boolean {
        val result = withContext(Dispatchers.IO) {
            repository.confirmBill(billDetails, paymentStatusValue.value.toString())
        }
        return result
    }

    fun printBill() {

    }

    fun updatePaymentStatusValue(paymentStatusValue: String) {
        mutablePaymentStatus.postValue(paymentStatusValue)
    }

    suspend fun syncOnServer() {
        try {
            repository.syncOnServer()
        } catch (_: Exception) {
        }
    }

    fun setPatientDetails(billDetails: BillDetails): String {
        val patientDetails = buildString {
            append(appContext.getString(R.string.receipt_no)).append(billDetails.receiptNum)
                .append("\n")
            append(appContext.getString(R.string.client_name)).append(billDetails.patientName)
                .append("\n")
            append(appContext.getString(R.string.client_id)).append(billDetails.patientOpenID)
                .append("\n")
            append(appContext.getString(R.string.visit_id)).append(billDetails.patientHideVisitID)
                .append("\n")
            append(appContext.getString(R.string.contact_no)).append(billDetails.patientPhoneNum)
                .append("\n")
            append(appContext.getString(R.string.client_village_name)).append(billDetails.patientVillage)
                .append("\n")
            append(appContext.getString(R.string.date_bill)).append(billDetails.billDateString)
        }
        return patientDetails

    }

    fun manageTestsData(
        binding: ActivityBillCreationBinding,
        selectedTests: ArrayList<String?>,
        billDetails: BillDetails
    ) {
        val billTestsChargeModel = BillTestsChargeModel()
        var totalAmount = 0
        if (billDetails.visitType.equals(VisitType.CONSULTATION.value, true)) {
            val price = BillRate.CONSULTATION.value
            billTestsChargeModel.consultationChargeAmount = "₹$price/-"
            billTestsChargeModel.consultationChargesVisible = true
            totalAmount += price
        }
        if (billDetails.visitType.equals(VisitType.FOLLOW_UP.value, true)) {
            val price = BillRate.FOLLOW_UP.value
            billTestsChargeModel.followUpChargeAmount = "₹$price/-"
            billTestsChargeModel.followUpChargesVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.blood_glucose_random))) {
            val price = BillRate.GLUCOSE_RANDOM.value
            billTestsChargeModel.glucoseRandomAmount = "₹$price/-"
            billTestsChargeModel.glucoseRandomVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.blood_glucose_fasting))) {
            val price = BillRate.GLUCOSE_FASTING.value
            billTestsChargeModel.glucoseFastingChargeAmount = "₹$price/-"
            billTestsChargeModel.glucoseFastingChargesVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.blood_glucose_post_prandial))) {
            val price = BillRate.GLUCOSE_POST_PRANDIAL.value
            billTestsChargeModel.glucosePostPrandialChargeAmount = "₹$price/-"
            billTestsChargeModel.glucosePostPrandialChargesVisible = true
            totalAmount += price
        }

        if (selectedTests.contains(appContext.getString(R.string.uric_acid))) {
            val price = BillRate.URIC_ACID.value
            billTestsChargeModel.uricAcidChargeAmount = "₹$price/-"
            billTestsChargeModel.uricAcidChargesVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.haemoglobin))) {
            val price = BillRate.HEMOGLOBIN.value
            billTestsChargeModel.hemoglobinChargeAmount = "₹$price/-"
            billTestsChargeModel.hemoglobinChargesVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.total_cholestrol))) {
            val price = BillRate.CHOLESTEROL.value
            billTestsChargeModel.cholestrolChargeAmount = "₹$price/-"
            billTestsChargeModel.cholestrolChargesVisible = true
            totalAmount += price
        }
        if (selectedTests.contains(appContext.getString(R.string.visit_summary_bp))) {
            val price = BillRate.BP.value
            billTestsChargeModel.bpChargeAmount = "₹$price/-"
            billTestsChargeModel.bpChargesVisible = true
            totalAmount += price
        }
        billTestsChargeModel.totalChargeAmount = "₹${totalAmount}/-"

        binding.contentGenerateBill.testsChargesModel = billTestsChargeModel

    }
    private fun loadBitmap(view: View, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun showToast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
    }

    fun createPdf(billDetails: BillDetails, binding: ActivityBillCreationBinding) {
       bitmap= loadBitmap(binding.contentGenerateBill.finalBillCV, binding.contentGenerateBill.finalBillCV.width, binding.contentGenerateBill.finalBillCV.height)

        try {
            val wm = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displaymetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displaymetrics)
            val convertHeight = displaymetrics.heightPixels
            val convertWidth = displaymetrics.widthPixels

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create()
            val page = document.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint()
            canvas.drawPaint(paint)

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHeight, true)
            paint.color = Color.BLUE
            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
            document.finishPage(page)

            val path = appContext.getExternalFilesDir("Bill")
            val fName = "${billDetails.patientName}_${billDetails.patientOpenID}_${billDetails.billDateString}.pdf"
            val filePath = File(path, fName)

            if (!filePath.exists()) filePath.mkdirs()

            val finalPath = path.toString() + fName
            finalBillPath = finalPath

            if (filePath.exists()) filePath.delete()

            filePath.createNewFile()
            document.writeTo(FileOutputStream(filePath))

            document.close()
            _toastMessage.postValue("Successfully PDF created")

        } catch (e: IOException) {
            e.printStackTrace()
            _toastMessage.postValue("Error: ${e.message}")
        }
    }

    fun shareFile(billDetails: BillDetails, activity: Activity) {
        try {
            val path = appContext.getExternalFilesDir("Bill")
            val fName = "${billDetails.patientName}_${billDetails.patientOpenID}_${billDetails.billDateString}.pdf"
            val finalPath = path.toString() + fName

            if (finalPath.isEmpty()) {
                _toastMessage.postValue(appContext.getString(R.string.download_bill))
                return
            }

            val file = File(path, fName)
            if (!file.exists()) {
                _toastMessage.postValue(appContext.getString(R.string.download_bill))
                return
            }

            val uri = FileProvider.getUriForFile(appContext, appContext.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            activity.startActivity(Intent.createChooser(intent, appContext.getString(R.string.share_the_file)))

        } catch (e: Exception) {
            e.printStackTrace()
            _toastMessage.postValue("Error: ${e.message}")
        }
    }
}