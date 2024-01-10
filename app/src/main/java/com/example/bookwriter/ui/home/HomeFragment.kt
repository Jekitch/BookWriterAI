package com.example.bookwriter.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
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
        val LoginProgress : ProgressBar = binding.loginproggress;  LoginProgress.visibility = View.INVISIBLE

        //var LOGIN = login_tb.text.toString();
        //var PASSWORD = password_tb.text.toString();
        //handler
        BtnSend.setOnClickListener(View.OnClickListener {
            LoginProgress.visibility = View.VISIBLE;
            RespounseTb.setText("");
            if (login_tb.text.length > 0 && password_tb.text.length > 0){
                val api = GalleryFragment.AI_web_api();

                val toast = Toast.makeText(this@HomeFragment.context, "подключение к сайту", Toast.LENGTH_SHORT).show();

                api.respounse = ""
                Thread {
                    api.send_request(api.LoginOn(login_tb.text.toString(), password_tb.text.toString()), 3000);
                    Log.println(Log.ASSERT, "Login", "sended: " + login_tb.text.toString());
                    while (api.respounse == "") { Thread.sleep(10); }
                    try {
                        LoginProgress.visibility = View.INVISIBLE;
                        Log.println(Log.ASSERT, "Login respounce", api.respounse);

                        if ("Not Conection" in api.respounse) {
                            RespounseTb.visibility = View.VISIBLE;
                            RespounseTb.setTextColor(Color.parseColor("#FF0000"));
                            RespounseTb.setText("The website is unavailable \uD83D\uDE3F");
                            LoginProgress.visibility = View.INVISIBLE
                        }
                        else {
                            if ("[LOGIN 200]:" in api.respounse) {
                                //Exsecute values
                                AccauntInfo.Loign = api.respounse.split("login:")[1].split(";")[0];
                                AccauntInfo.Password = api.respounse.split("password:")[1].split(";")[0];
                                AccauntInfo.Email = api.respounse.split("email:")[1].split(";")[0];
                                AccauntInfo.Accaunt_API_key =
                                    api.respounse.split("api_key:")[1].split(";")[0];
                                RespounseTb.setText(api.respounse.split("msg:")[1].split(";")[0]);
                                //show elements
                                RespounseTb.visibility = View.VISIBLE;
                                RespounseTb.setTextColor(Color.parseColor("#27CC58"))
                            } else {
                                RespounseTb.visibility = View.VISIBLE;
                                RespounseTb.setText(api.respounse.split("msg:")[1].split(";")[0]);
                                RespounseTb.setTextColor(Color.parseColor("#FF0000"))
                                //un login in app
                                AccauntInfo.Accaunt_API_key = "" //Set incorect key
                            }
                        }
                    } catch (e:Exception) {}
                }.start()
            } else {
                LoginProgress.visibility = View.INVISIBLE
                RespounseTb.visibility = View.VISIBLE;
                RespounseTb.setTextColor(Color.parseColor("#FFAA00"))
                RespounseTb.setText("Fill in the fields! Kudasai");
            }
        });
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}