package com.app.plutope.ui.fragment.contact

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentContactListBinding
import com.app.plutope.model.ContactModel
import com.app.plutope.model.ContactType
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactListFragment : BaseFragment<FragmentContactListBinding, ContactListViewModel>() {

    private lateinit var adapter: ContactListAdapter
    private val contactViewModel: ContactListViewModel by viewModels()
    val args: ContactListFragmentArgs by navArgs()

    companion object {
        const val keyContact = "keyContact"
        const val keyContactSelect = "keyContactSelect"
    }

    override fun getViewModel(): ContactListViewModel {
        return contactViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.contactViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_list
    }

    override fun setupToolbarText(): String {
        return "Contacts"
    }

    override fun setupUI() {
        setOnClickListner()
        if (args.isForSelection) viewDataBinding?.btnAddContact?.visibility =
            GONE else viewDataBinding?.btnAddContact?.visibility = VISIBLE

        adapter = ContactListAdapter {
            if (args.isForSelection) {
                val bundle = Bundle()
                bundle.putParcelable(keyContactSelect, it)
                setFragmentResult(keyContact, bundle)
                findNavController().popBackStack()
            }
        }

        viewDataBinding?.recyclerContacts?.adapter = adapter

        contactViewModel.getContactList()

    }

    private fun setOnClickListner() {
        viewDataBinding?.btnAddContact?.setOnClickListener {
            findNavController().safeNavigate(ContactListFragmentDirections.actionContactListFragmentToAddContactFragment())
        }
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                contactViewModel.contactsResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            //detail page
                            if (!it.data.isNullOrEmpty()) {
                                viewDataBinding?.txtNoContacts?.visibility = GONE
                                viewDataBinding?.recyclerContacts?.visibility = VISIBLE
                                val contactList =
                                    getContactListWithHeaders(it.data as MutableList<ContactModel>)
                                adapter.submitList(contactList)
                                adapter.notifyDataSetChanged()
                            }
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            viewDataBinding?.layoutNoFound?.visibility = VISIBLE
                            viewDataBinding?.recyclerContacts?.visibility = GONE
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }
    }

    private fun getContactListWithHeaders(contactList: MutableList<ContactModel>): MutableList<ContactModel> {
        val sortedList = contactList.sortedBy { it.name }

        val items = mutableListOf<ContactModel>()
        var currentHeader: Char? = null

        sortedList.forEach { contactModel ->
            val headerLetter = contactModel.name.first().uppercaseChar()
            if (headerLetter != currentHeader) {
                items.add(
                    ContactModel(
                        id = contactModel.id,
                        name = headerLetter.toString(),
                        address = "",
                        type = ContactType.HEADER
                    )
                )
                currentHeader = headerLetter
            }
            items.add(contactModel)
        }

        return items
    }


}