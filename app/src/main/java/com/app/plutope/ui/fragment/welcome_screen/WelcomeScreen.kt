package com.app.plutope.ui.fragment.welcome_screen


import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.airbnb.lottie.LottieAnimationView
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWelcomeScreenBinding
import com.app.plutope.ui.base.BaseFragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WelcomeScreen : BaseFragment<FragmentWelcomeScreenBinding, WelcomeScreenViewModel>() {
    private lateinit var myViewPagerAdapter: ViewAdapter
    private val welcomeScreenViewModel: WelcomeScreenViewModel by viewModels()

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
        }

    override fun getViewModel(): WelcomeScreenViewModel {
        return welcomeScreenViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.welcomeScreenViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_welcome_screen
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        preferenceHelper.isFirstTime=true
        myViewPagerAdapter = ViewAdapter(requireContext())
        viewDataBinding!!.viewPager.adapter = myViewPagerAdapter
        viewDataBinding!!.viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewDataBinding!!.tabIndicator.attachTo(viewDataBinding!!.viewPager)

    }

    override fun setupObserver() {

    }

    inner class ViewAdapter(private val context: Context) : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        private var layouts = intArrayOf(
            R.layout.welcome_screen_1,
            R.layout.welcome_screen_2,
            R.layout.welcome_screen_3,

            )

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = context.getSystemService(
                LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater
            val view: View = layoutInflater!!.inflate(layouts[position], null)

            // viewDataBinding!!.btnNext2.visibility = View.GONE
            when (position) {
                0 -> {
                    // viewDataBinding!!.btnNext2.visibility = View.GONE
                    val imageView = view.findViewById<ImageView>(R.id.image_view)
                    Glide.with(imageView.context).load(R.drawable.welcome_1).into(imageView)
                }

                1 -> {
                    // viewDataBinding!!.btnNext2.visibility = View.GONE
                }

                2 -> {

                    // viewDataBinding!!.btnNext2.visibility = View.VISIBLE

                    view.findViewById<LottieAnimationView>(R.id.btn_next_2).setOnClickListener {
                        findNavController().navigate(WelcomeScreenDirections.actionWelcomeScreenToDashboard())
                    }
                }
            }
            val viewPager = container as ViewPager
            viewPager.addView(view, 0)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val viewPager = container as ViewPager
            val view = `object` as View
            viewPager.removeView(view)
        }
    }

    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }


}