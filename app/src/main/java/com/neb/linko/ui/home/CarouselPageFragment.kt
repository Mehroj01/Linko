package com.neb.linko.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.neb.linko.databinding.FragmentCarouselPageBinding
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.web.WebActivity
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"

class CarouselPageFragment : Fragment() {

    private var param1: StoreModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as StoreModel
        }
    }

    lateinit var carouselPageBinding: FragmentCarouselPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        carouselPageBinding = FragmentCarouselPageBinding.inflate(inflater, container, false)

        try {
            Picasso.get().load(param1?.imageUrl).into(carouselPageBinding.img)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        carouselPageBinding.img.setOnClickListener {
            val intent = Intent(requireContext(), WebActivity::class.java).apply {
                this.putExtra("storeData", Gson().toJson(param1))
            }
            startActivity(intent)
        }

        return carouselPageBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: StoreModel) =
            CarouselPageFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}