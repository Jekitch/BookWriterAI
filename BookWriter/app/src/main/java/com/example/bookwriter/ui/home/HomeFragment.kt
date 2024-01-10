package com.example.bookwriter.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookwriter.AccauntInfo
import com.example.bookwriter.databinding.FragmentHomeBinding
import com.example.bookwriter.ui.gallery.GalleryFragment
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val RespounseTb : TextView = binding.LoginResp;
        val BtnSend : Button = binding.SendBtn;

        val login_tb : TextView = binding.loginTb;
        val password_tb : TextView = binding.passwordTb;

        //var LOGIN = login_tb.text.toString();
        //var PASSWORD = password_tb.text.toString();
        //handler
        BtnSend.setOnClickListener(View.OnClickListener {
            val api = GalleryFragment.AI_web_api();
            Thread {
                api.send_request(api.LoginOn(login_tb.text.toString(), password_tb.text.toString()));
                Log.println(Log.ASSERT, "Login", "sended: " + login_tb.text.toString());
            }.start()
            while (("[LOGIN 200]:" in api.respounse) == false) {  }

            //Exsecute values
            AccauntInfo.Loign = api.respounse.split("loign:")[1].split(";")[0];
            AccauntInfo.Password = api.respounse.split("password:")[1].split(";")[0];
            AccauntInfo.Email = api.respounse.split("email:")[1].split(";")[0];
            AccauntInfo.Accaunt_API_key = api.respounse.split("api_key:")[1].split(";")[0];
            RespounseTb.setText(api.respounse.split("msg:")[1].split(";")[0]);
            //show elements
            RespounseTb.visibility = View.VISIBLE;
            RespounseTb.setTextColor(Color.parseColor("#27CC58"))



        });

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}