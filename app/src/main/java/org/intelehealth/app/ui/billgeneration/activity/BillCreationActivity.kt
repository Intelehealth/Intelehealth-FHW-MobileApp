package org.intelehealth.app.ui.billgeneration.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.databinding.ActivityBillCreationBinding
import org.intelehealth.app.ui.billgeneration.models.BillDetails
import org.intelehealth.app.ui.billgeneration.utils.PaymentStatus
import org.intelehealth.app.ui.billgeneration.utils.PrintBillUsingThermalPrinter
import org.intelehealth.app.ui.billgeneration.viewmodel.BillGenerationViewModel
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.SessionManager
import java.util.Locale

class BillCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillCreationBinding
    private val viewModel: BillGenerationViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var billDetails: BillDetails
    private var paidOrUnpaid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupUI()
        observeViewModel()

        // Fetch initial data
        intent?.let { billDetails = getBillDetailsFromIntent(it)!! }
        setupActionBar()
        showBillDetails()
        printBill()

    }

    private fun showBillDetails() {
        billDetails.billEncounterUUID.let {
            if (it.isNotEmpty()) {
                viewModel.isBillGenerated = true
                binding.contentGenerateBill.isBillGenerated = viewModel.isBillGenerated
                if (billDetails.billType.isNotEmpty())
                    viewModel.updatePaymentStatusValue(billDetails.billType)
            }
        }
        binding.contentGenerateBill.patientDetailsTV.text = viewModel.setPatientDetails(billDetails)
        viewModel.manageTestsData(binding, billDetails.selectedTestsList, billDetails)
    }

    private fun setupActionBar() {
        val toolbar = binding.toolbar
        toolbar.title = billDetails.patientName + " : " + billDetails.receiptNum
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUI() {
        setupReasonTextWatcher()
        setupPaymentButtons()
        setupBillActionButtons()
    }

    private fun setupReasonTextWatcher() {
        binding.contentGenerateBill.reasonET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                with(binding.contentGenerateBill) {
                    if (!s.isNullOrEmpty()) {
                        val isEmpty = reasonET.text.isNullOrEmpty()
                        tvReasonErrorNotPay.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        reasonET.background = ContextCompat.getDrawable(
                            this@BillCreationActivity,
                            if (isEmpty) R.drawable.input_field_error_bg_ui2 else R.drawable.bg_input_fieldnew
                        )
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPaymentButtons() {
        binding.contentGenerateBill.yesPayBill.setOnClickListener {
            toggleReasonVisibility(false)
            paidOrUnpaid = PaymentStatus.PAID.value
        }
        binding.contentGenerateBill.noPayBill.setOnClickListener {
            toggleReasonVisibility(true)
            paidOrUnpaid = PaymentStatus.UNPAID.value
        }
    }

    private fun toggleReasonVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        with(binding.contentGenerateBill) {
            reasonET.visibility = visibility
            reasonTIL.visibility = visibility
        }
    }

    private fun setupBillActionButtons() {
        with(binding.contentGenerateBill) {
            buttonConfirmBill.setOnClickListener { handleBillConfirmation() }
            buttonPrint.setOnClickListener { viewModel.printBill() }
            buttonDownload.setOnClickListener { viewModel.createPdf(billDetails, binding) }
            buttonShare.setOnClickListener {
                viewModel.shareFile(
                    billDetails,
                    activity = this@BillCreationActivity
                )
            }
        }
    }

    private fun handleBillConfirmation() {
        if (isValidBill()) {
            lifecycleScope.launch {
                val reason = binding.contentGenerateBill.reasonET.text.toString()
                billDetails.billType =
                    if (binding.contentGenerateBill.yesPayBill.isChecked) PaymentStatus.PAID.value else PaymentStatus.UNPAID.value
                viewModel.updatePaymentStatusValue(if (binding.contentGenerateBill.yesPayBill.isChecked) PaymentStatus.PAID.value else "Unpaid - $reason")

                val result = viewModel.confirmBill(billDetails)
                if (result) {
                    onBillCreated()
                    viewModel.syncOnServer()
                }
            }
        }
    }

    private fun onBillCreated() {
        Toast.makeText(this, getString(R.string.bill_generated_success), Toast.LENGTH_LONG).show()
        viewModel.isBillGenerated = true
        binding.contentGenerateBill.isBillGenerated = viewModel.isBillGenerated
    }

    private fun isValidBill(): Boolean {
        with(binding.contentGenerateBill) {
            if (!yesPayBill.isChecked && !noPayBill.isChecked) {
                DialogUtils().showCommonDialog(
                    this@BillCreationActivity, 0,
                    getString(R.string.error),
                    getString(R.string.select_payment_information),
                    true, getString(R.string.ok), getString(R.string.cancel), {}
                )
                return false
            }

            if (noPayBill.isChecked && reasonTIL.visibility == View.VISIBLE) {
                val reason = reasonET.text.toString()
                if (reason.isEmpty()) {
                    tvReasonErrorNotPay.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.enter_reason_toast)
                    }
                    return false
                }
            }
            return true
        }
    }

    private fun observeViewModel() {
        viewModel.apply {
            patientDetails.observe(this@BillCreationActivity) {
                binding.contentGenerateBill.patientDetailsTV.text = it
            }
            paymentStatusValue.observe(this@BillCreationActivity) { paymentStatus ->
                binding.contentGenerateBill.paymentStatus.apply {
                    if (!paymentStatus.isNullOrEmpty() && billDetails.billType != "NA") {
                        visibility = View.VISIBLE
                        text = when {
                            paymentStatus.contains(
                                PaymentStatus.UNPAID.value,
                                ignoreCase = true
                            ) -> {
                                paymentStatus.split("-", limit = 2)[0].trim()
                            }

                            else -> paymentStatus
                        }
                    } else {
                        visibility = View.GONE
                        text = ""
                    }
                }
            }

        }
        viewModel.toastMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                viewModel.showToast(message)
            }
        }
    }

    private fun getBillDetailsFromIntent(intent: Intent): BillDetails? {
        val bundle = intent.getBundleExtra("BUNDLE")
        billDetails = bundle?.getSerializable("billDetails") as? BillDetails
            ?: run {
                return billDetails
            }
        return billDetails
    }


    fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home_menu -> {
                val intent = Intent(
                    this@BillCreationActivity,
                    HomeScreenActivity_New::class.java
                )
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(
            this@BillCreationActivity,
            HomeScreenActivity_New::class.java
        )
        startActivity(intent)
    }
    private fun printBill() {
        val fragmentManager = supportFragmentManager
        val printBillObj= PrintBillUsingThermalPrinter(this@BillCreationActivity,binding,this@BillCreationActivity, fragmentManager)
        val layoutBinding = binding.contentGenerateBill
        printBillObj.initBluetoothDevice()
        layoutBinding.buttonPrint.setOnClickListener {
            printBillObj.textPrint()
        }
        layoutBinding.tvDeviceSelected.setOnClickListener {
            printBillObj.showBluetoothDeviceChooseDialog()
        }
        layoutBinding.btnConnect.setOnClickListener {
            printBillObj.doConnect()
        }
        layoutBinding.btnDisConnect.setOnClickListener {
            printBillObj.doDisConnect()
        }
        layoutBinding.btnDisConnect.setOnClickListener {
            printBillObj.doDisConnect()
        }

    }
}


