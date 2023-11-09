package com.boom.line

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.boom.line.databinding.FragmentGameBinding
import kotlin.math.min


class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding

    var pause = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGameBinding.inflate(inflater,container,false)
        val activity = requireActivity()
        binding.label2.text = "${binding.game.level+1}"
        binding.textView8.text = "${binding.game.balance}"
        binding.game.setEndListener(object : GameView.Companion.EndListener {
            override fun end() {
                Log.d("TAG","${binding.game.win}")
                if(binding.game.win) {
                    var level =  activity.getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("level",0)
                    var tmp = binding.game.level
                    if(tmp==level && level<5) level++
                    activity.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putInt("tmp",tmp+1).putInt("level",level).apply()
                    activity.runOnUiThread {
                        binding.label.text = "GREAT!"
                        if(tmp>=5) binding.imageView10.visibility = View.INVISIBLE
                        binding.pause.visibility = View.VISIBLE
                    }
                } else {
                    activity.runOnUiThread {
                        val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
                        navController.popBackStack()
                    }
                }
            }

            override fun score(score: Int) {
                activity.runOnUiThread {
                    activity.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putInt("balance",binding.game.balance).apply()
                    binding.textView8.text = "${binding.game.balance}"
                }
            }

        })
        binding.pauseB.setOnClickListener {
            binding.game.togglePause()
            binding.pause.visibility = View.VISIBLE
            binding.label.text = "PAUSE"
            pause = true
        }
        binding.imageView9.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.popBackStack()
        }
        binding.imageView10.setOnClickListener {
            if(pause) {
                pause = false
                binding.pause.visibility = View.INVISIBLE
                binding.game.togglePause()
            } else {
                val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
                navController.popBackStack()
                navController.navigate(R.id.gameFragment)
            }
        }
        return binding.root
    }


}