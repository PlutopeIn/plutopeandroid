package com.app.plutope.ui.fragment.notification

import androidx.fragment.app.viewModels
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentNotificationBinding
import com.app.plutope.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Notification : BaseFragment<FragmentNotificationBinding, NotificationViewModel>() {


    private val notificationViewModel: NotificationViewModel by viewModels()
    var notificationAdapter: NotificationListAdapter? = null

    override fun getViewModel(): NotificationViewModel {
        return notificationViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.notificationViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_notification
    }

    override fun setupToolbarText(): String {
        return getString(R.string.notifications)
    }

    override fun setupUI() {

        val notificationList = arrayListOf<NotificationModel>()
      /*  repeat(10) {
            notificationList.add(
                NotificationModel(
                    "",
                    "Received : 5,000 DST",
                    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod.",
                    "9:35 AM"
                )
            )
        }*/

        notificationAdapter = NotificationListAdapter { }
        notificationAdapter?.submitList(notificationList)
        viewDataBinding!!.rvNewNotificationList.adapter = notificationAdapter

        viewDataBinding!!.rvEarlierNotificationList.adapter = notificationAdapter

    }

    override fun setupObserver() {

    }


}