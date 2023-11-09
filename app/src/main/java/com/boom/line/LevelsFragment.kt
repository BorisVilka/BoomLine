package com.boom.line

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.boom.line.databinding.FragmentLevelsBinding

class LevelsFragment : Fragment() {

    private lateinit var binding: FragmentLevelsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLevelsBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onResume() {
        val views = arrayOf(binding.textView2,binding.textView3,binding.textView4,binding.textView5,binding.textView6,binding.textView7)
        val level = requireContext().getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("level",0)
        for(i in views.indices) {
            if(i>level) {
                views[i].text = ""
                views[i].setBackgroundResource(R.drawable.bg)
                views[i].setOnClickListener(null)
            } else {
                views[i].text = "${i+1}"
                views[i].setBackgroundResource(R.drawable.circle)
                views[i].setOnClickListener {
                    requireActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putInt("tmp",i).apply()
                    val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
                    navController.navigate(R.id.gameFragment)
                }
            }
        }
        super.onResume()
    }


}