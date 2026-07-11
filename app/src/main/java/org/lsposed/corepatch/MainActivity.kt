package org.lsposed.corepatch

import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import org.lsposed.corepatch.App.Companion.mService
import org.lsposed.corepatch.App.Companion.reloadListener
import org.lsposed.corepatch.adapter.MultiTypeListAdapter
import org.lsposed.corepatch.data.SwitchData
import org.lsposed.corepatch.ui.PreferenceLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mService == null || "system" !in mService.scope) {
            reloadListener = {
                runOnUiThread {
                    loadPrefs()
                }
            }
            setContentView(PreferenceLayout(this).apply {
                titleView.setText(R.string.xposed_service_unavailable)
                subtitleView.setText(R.string.xposed_service_unavailable_summary)
            })
        } else {
            loadPrefs()
        }
    }

    private fun loadPrefs() {
        val bypassDowngrade = SwitchData(
            getString(R.string.bypass_downgrade), getString(R.string.bypass_downgrade_summary), Config.BYPASS_DOWNGRADE
        )
        val bypassVerification = SwitchData(
            getString(R.string.bypass_verification), getString(R.string.bypass_verification_summary), Config.BYPASS_VERIFICATION
        )
        val bypassDigest = SwitchData(
            getString(R.string.bypass_digest), getString(R.string.bypass_digest_summary), Config.BYPASS_DIGEST
        )
        val bypassExactSignatureMatch = SwitchData(
            getString(R.string.bypass_exact_signature_match), getString(R.string.bypass_exact_signature_match_summary), Config.BYPASS_EXACT_SIGNATURE_MATCH
        )
        val usePreviousSignatures = SwitchData(
            getString(R.string.use_previous_signatures), getString(R.string.use_previous_signatures_summary), Config.USE_PREVIOUS_SIGNATURES,
            if (isMiui()) {
                getString(R.string.miui_usepresig_warn) + "\n\n" + getString(R.string.use_previous_signatures_warning)
            } else {
                getString(R.string.use_previous_signatures_warning)
            }
        )
        val bypassSharedUser = SwitchData(
            getString(R.string.bypass_shared_user), getString(R.string.bypass_shared_user_summary), Config.BYPASS_SHARED_USER
        )
        val disableVerificationAgent = SwitchData(
            getString(R.string.disable_verification_agent), getString(R.string.disable_verification_agent_summary), Config.DISABLE_VERIFICATION_AGENT
        )
        val bypassBlock = SwitchData(
            getString(R.string.bypass_block), getString(R.string.bypass_block_summary), Config.BYPASS_BLOCK
        )

        val dataSet = arrayListOf(
            bypassDowngrade,
            bypassVerification,
            bypassDigest,
            bypassExactSignatureMatch,
            usePreviousSignatures,
            bypassSharedUser,
            disableVerificationAgent,
            bypassBlock
        )

        val adapter = MultiTypeListAdapter(dataSet)

        val listView = ListView(this)
        listView.adapter = adapter
        listView.fitsSystemWindows = true
        setContentView(listView)
    }

    private fun isMiui(): Boolean = try {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val get = systemProperties.getMethod("get", String::class.java)
        (get.invoke(null, "ro.miui.ui.version.code") as String).isNotEmpty()
    } catch (_: ReflectiveOperationException) {
        false
    }

    override fun onStop() {
        super.onStop()
        reloadListener = {}
    }
}
