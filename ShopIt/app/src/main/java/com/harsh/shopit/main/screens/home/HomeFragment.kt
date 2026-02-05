package com.harsh.shopit.main.screens.home

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.home.banner.BannerItemAdapter
import com.harsh.shopit.main.screens.home.banner.BannerItemDataModel
import com.harsh.shopit.main.screens.home.saleprod.SaleProductsAdapter
import com.harsh.shopit.main.screens.home.saleprod.SaleProductsDataModel
import com.harsh.shopit.main.screens.home.trendProd.TrendProductsAdapter
import com.harsh.shopit.main.screens.home.trendProd.TrendProductsDataModel

class HomeFragment : Fragment(R.layout.customer_fragment_home) {


    private lateinit var bannerList: ArrayList<BannerItemDataModel>
    private lateinit var bannerAdaper: BannerItemAdapter
    private lateinit var bannerRecycler: RecyclerView


    ///for:: sale products
    private lateinit var saleProductsRecycler: RecyclerView
    private lateinit var saleProductsList: ArrayList<SaleProductsDataModel>
    private lateinit var saleProductsAdaper: SaleProductsAdapter

    ///for:: flash sale timer
    private var countDownTimer: CountDownTimer? = null


    ///for:: trending products
    private lateinit var trendingProductsRecycler: RecyclerView
    private lateinit var trendingProductsList: ArrayList<TrendProductsDataModel>
    private lateinit var trendingProductsAdaper: TrendProductsAdapter
    private fun timerCounter(view: View) {
        val timerHr = view.findViewById<TextView>(R.id.time_hr)
        val timerMin = view.findViewById<TextView>(R.id.time_min)
        val timerSec = view.findViewById<TextView>(R.id.time_sec)

        val hour = 2
        val min = 15
        val sec = 42

        val totalTimeInMillis =
            (hour * 60 * 60 * 1000L) + // hours
                    (min * 60 * 1000L) +     // minutes
                    (sec * 1000L)            // seconds

        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val hours = (millisUntilFinished / (1000 * 60 * 60))
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                timerHr.text = String.format("%02d", hours)
                timerMin.text = String.format("%02d", minutes)
                timerSec.text = String.format("%02d", seconds)
            }

            override fun onFinish() {
                timerHr.text = "00"
                timerMin.text = "00"
                timerSec.text = "00"
            }

        }.start()
    }

    private fun bannerRecycler(view: View) {
        bannerRecycler = view.findViewById(R.id.bannerRecycler)
        bannerRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bannerRecycler.setHasFixedSize(true)
        bannerList = ArrayList()


        bannerList.add(
            BannerItemDataModel(
                R.drawable.card1bg,
                "Limited Offer",
                requireContext().getColor(R.color.secondary),
                "Master the UI: 50% Off Design Systems",
                "Curated components for elite creators."
            )
        )
        bannerList.add(
            BannerItemDataModel(
                R.drawable.card2bg,
                "New Arraival",
                requireContext().getColor(R.color.primary),
                "Elevate Your Space",
                "Smart home solutions for professionals."
            )
        )


        bannerAdaper = BannerItemAdapter(bannerList)
        bannerRecycler.adapter = bannerAdaper
    }

    private fun saleProductsRecycler(view: View) {
        saleProductsRecycler = view.findViewById(R.id.saleProductsRecycler)
        saleProductsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        saleProductsRecycler.setHasFixedSize(true)
        saleProductsList = ArrayList()


        saleProductsList.add(
            SaleProductsDataModel(
                R.drawable.prod1,
                "-40%",
                "Wireless ANC",
                "$129",
                "$210"
            )
        )
        saleProductsList.add(
            SaleProductsDataModel(
                R.drawable.prod2,
                "-25%",
                "Smart Watch X",
                "$89",
                "$120"
            )
        )
        saleProductsList.add(
            SaleProductsDataModel(
                R.drawable.prod3,
                "-15%",
                "Air Mini Drone",
                "$299",
                "$350"
            )
        )



        saleProductsAdaper = SaleProductsAdapter(saleProductsList)
        saleProductsRecycler.adapter = saleProductsAdaper
    }

    private fun trendProductsRecycler(view: View) {
        trendingProductsRecycler = view.findViewById(R.id.trendingProductsRecycler)
        trendingProductsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        trendingProductsRecycler.setHasFixedSize(true)
        trendingProductsList = ArrayList()


        trendingProductsList.add(
            TrendProductsDataModel(
                R.drawable.trend1,
                requireContext().getColor(R.color.primaryLight),
                "Velocity Pro",
                "Performance Lifestyle Shoe",
                "$185",
            )
        )
        trendingProductsList.add(
            TrendProductsDataModel(
                R.drawable.trend2,
                requireContext().getColor(R.color.secondaryLight),
                "Aura Sound G2",
                "360° Immersive Audio",
                "$240",
            )
        )



        trendingProductsAdaper = TrendProductsAdapter(trendingProductsList)
        trendingProductsRecycler.adapter = trendingProductsAdaper
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //banner
        bannerRecycler(view)
        //sale products
        saleProductsRecycler(view)
        //sale counter
        timerCounter(view)
        //trending products
        trendProductsRecycler(view)

    }
}