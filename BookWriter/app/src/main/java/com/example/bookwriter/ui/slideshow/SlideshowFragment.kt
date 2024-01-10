package com.example.bookwriter.ui.slideshow

import android.content.Intent.getIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookwriter.TempBookContent
import com.example.bookwriter.databinding.FragmentSlideshowBinding


class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val Theader: TextView = binding.Header;
        val Tcontent: TextView = binding.Content;

        Theader.setText(TempBookContent.Header);
        Tcontent.setText(TempBookContent.Content);

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}