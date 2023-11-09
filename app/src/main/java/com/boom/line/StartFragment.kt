package com.boom.line

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.boom.line.databinding.FragmentStartBinding


class StartFragment : Fragment() {

    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartBinding.inflate(inflater,container,false)
        binding.imageView5.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.navigate(R.id.levelsFragment)
        }
        binding.textView.text = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE).getInt("balance",0).toString()
        var music = requireActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",false)
        binding.imageView7.setImageResource(if(music) R.drawable.on else R.drawable.off)
        binding.imageView7.setOnClickListener {
            music = !music
            binding.imageView7.setImageResource(if(music) R.drawable.on else R.drawable.off)
            requireActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("music",music).apply()
        }
        return binding.root
    }


}