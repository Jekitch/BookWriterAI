package com.example.bookwriter.ui.gallery

import android.R
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookwriter.AccauntInfo
import com.example.bookwriter.TempBookContent
import com.example.bookwriter.databinding.FragmentGalleryBinding
import com.example.bookwriter.ui.slideshow.SlideshowFragment
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    class AI_web_api() {
        val ip = "http://192.168.56.1:5000"
        var respounse = "r"

        init {
            println("Init")
        }

        fun runOnUiThread(action: Runnable) {
            action.run()
        }

        fun LoginOn(login: String, paqssword: String) : String
        {
            return "/auth?login=${login}&email=${"empty@email.com"}&pass=${paqssword}"
        }

        fun GetAImodules(Key : String) : String
        {
            return "/api/ai?key=${Key}&name=GetAll&discription=\"ТУТ НЕТ АРГУМЕНТОВ НО МОЖНО ЧТО НИБУДЬ НАПИСАТЬ\"";
        }

        fun MkBook(Key : String,  Name : String, Pages : Int ) : String
        {
            return "/api/ai?key=${Key}&name=BookMakerAI&discription=Title=\"${Name}\"__Pages=${Pages}";
        }

        fun GetStatus(Key : String, PID : String ) : String
        {
            return "/api/status?key=${Key}&process_id=${PID}"
        }

        fun send_request(req : String) : Int {
            val address = this.ip + req;
            val url = URL(address)
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET
                    println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            respounse += line;
                            //Log.println(Log.ASSERT, "API Thread", respounse);
                        }
                    }
                }
            } catch (e: Exception) { return 1; }
            return 0;
        }
    }

    var spinnerArray = arrayOf("Dumbell", "Punching Bag", "Yoga Ball", "Skipping Rope")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        val api = AI_web_api();

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val DiscrTb: EditText = binding.DiscriptionTb;
        val BtnSend: Button = binding.SendBtn;
        val ProgBar0: ProgressBar = binding.progressBar0;
        val ProgBar1: ProgressBar = binding.progressBar1;
        val ProgLable: TextView = binding.progressLable;

        //handler
        BtnSend.setOnClickListener(View.OnClickListener {
            ProgBar0.visibility = View.VISIBLE;
            ProgBar1.visibility = View.VISIBLE;
            ProgBar1.isIndeterminate = true;
            Log.println(Log.ASSERT, "debug", "Book Run");

            Thread {
                var conection_state = 0;

                var PID = ""; var progress = "0";
                var Iteration = 0;

                conection_state = api.send_request(api.MkBook(AccauntInfo.Accaunt_API_key, DiscrTb.text.toString(), 2));

                Thread.sleep(3000);
                if (conection_state == 1) { ProgLable.setText("Соединение прервано"); }
                while (Iteration < 25) {
                    if (PID == "") { // if proccess in not exist
                        try {
                            ProgBar1.isIndeterminate = false;
                            ProgBar1.progress = 0;
                            ProgLable.setText("0% выполнено");

                            PID = api.respounse.split("pid:")[1];
                            Log.println(Log.ASSERT, "pid", PID.toString());
                            //send Status Request & respounse = "Empty string"

                            //if access denied
                            if ("403 Invalid Api Key" in api.respounse) {
                                ProgBar0.setVisibility(View.INVISIBLE);
                                ProgBar1.setVisibility(View.INVISIBLE);
                                DiscrTb.setText("");
                                ProgLable.setText("Access Denied! Login In pls");
                                break;
                            }

                            api.respounse = "";
                            conection_state = api.send_request(api.GetStatus("admin", PID));
                        } catch (e: Exception) { Log.println(Log.ERROR, "Thread", "Execute PID"); }
                    } else {
                        if ( "[COMPLETE]" in api.respounse )
                        {
                            ProgBar1.progress = 100;
                            ProgLable.setText(ProgBar1.progress.toString() + "% готово");

                            try {
                                Thread.sleep(2000);
                                ProgBar0.setVisibility(View.INVISIBLE);
                                ProgBar1.setVisibility(View.INVISIBLE);
                                DiscrTb.setText("");
                                ProgLable.setText("");

                                val toast = Toast.makeText(this@GalleryFragment.context, "Перейдите на вкладку \"Просмотр Книги\"", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();

                            } catch (e:Exception){}

                            var Content = api.respounse.split("[COMPLETE]:")[1];

                            //format : header:<>;content:<>;
                            try{
                                TempBookContent.Header = Content.toString().split("header:")[1].split(";")[0];
                                TempBookContent.Content = Content.toString().split("content:")[1].split(";")[0];
                            } catch (e: Exception) { TempBookContent.Header = "ERROR"; TempBookContent.Content = "Book Formating Error"; }

                            break;
                        }
                        else
                        {
                            try{
                                progress = api.respounse.split("Procents:")[1].split(";")[0];
                                Log.println(Log.ASSERT, "proggress", progress);
                                ProgBar1.progress = progress.toInt();
                                ProgLable.setText(ProgBar1.progress.toString() + "% выполнено");
                            } catch (e: Exception) { };
                            api.respounse = "";
                            conection_state = api.send_request(api.GetStatus("admin", PID));
                            //ReqText.setText(progress);
                        }


                    }
                    Thread.sleep(3000);
                    Iteration++;
                }
                Log.println(Log.DEBUG, "ITERATION", "${Iteration} of 25")

            }.start()


        })
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}