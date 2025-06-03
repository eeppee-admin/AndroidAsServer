package com.example.androidasserver.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidasserver.R
import com.example.androidasserver.databinding.FragmentHomeBinding
import com.example.androidasserver.ext.viewBinding

class SettingFragment : Fragment() {
    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txt.text = "damn"
    }
}