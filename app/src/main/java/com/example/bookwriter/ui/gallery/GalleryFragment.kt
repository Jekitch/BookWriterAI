package com.example.bookwriter.ui.gallery

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookwriter.AccauntInfo
import com.example.bookwriter.TempBookContent
import com.example.bookwriter.databinding.FragmentGalleryBinding
import org.jetbrains.annotations.Nullable
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    class AI_web_api() {
        //val ip = "http://192.168.0.47:5000"
        val ip = "http://192.168.56.1:5000"
        var respounse = ""

        init {
            println("Init")
        }

        fun runOnUiThread(action: Runnable) {
            action.run()
        }

        fun KillProccessByID(Key : String, PID: String) : String
        {
            return "/api/kill?key=${Key}&process_id=${PID}"
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

        fun send_request(req : String, ConectionTimeoutMS : Int) : Int {
            val address = this.ip + req;
            val url = URL(address)
            try {
                Log.println(Log.DEBUG, "conect", "Sending");
                val con = url.openConnection() as HttpURLConnection
                con.connectTimeout = ConectionTimeoutMS;
                con.setRequestMethod("GET");
                println("\nSent 'GET' request to URL : $url; Response Code : ${con.responseCode}")
                val inputStream = con.inputStream;
                inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            respounse += line;
                            Log.println(Log.DEBUG, "API Thread", respounse);
                        }
                    }
            } catch (e: Exception) { Log.println(Log.ERROR, "Conection to website", e.message.toString()); respounse+= "Not Conection"; return 1;}
            return 0;
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ///
        var DialogShow = false;
        ///

        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        var ActiveTreadCanRun = false

        val api = AI_web_api();

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val DiscrTb: EditText = binding.DiscriptionTb;
        val BtnSend: Button = binding.SendBtn;
        val ProgBar0: ProgressBar = binding.progressBar0;
        val ProgBar1: ProgressBar = binding.progressBar1;
        val ProgLable: TextView = binding.progressLable;

        val KToggleLay: CardView = binding.KillToggleLayout;
        val killbtn: Button = binding.Kill;
        val stilbtn: Button = binding.Still;

        //on thread
        var PID = "";

        //dialog btns
        if (DialogShow) {
            killbtn.setOnClickListener(View.OnClickListener {
                KToggleLay.visibility = View.INVISIBLE; //send request kill
                Log.println(Log.DEBUG, "KILL THREAD", "kill proccess ${PID}")
                Thread {
                    api.send_request(
                        api.KillProccessByID(AccauntInfo.Accaunt_API_key, PID),
                        2000
                    );
                }.start()
            })

            stilbtn.setOnClickListener(View.OnClickListener {
                KToggleLay.visibility = View.INVISIBLE;
            })
        }
        else
        {
            Log.println(Log.DEBUG, "KILL THREAD", "kill proccess ${PID}")
            Thread {
                api.send_request(
                    api.KillProccessByID(AccauntInfo.Accaunt_API_key, PID),
                    2000
                );
            }.start()
        }
        //handler
        var ConnectionTimeout = 10000; //async request timeout (проверено с использованием нескольких устройств)
        var RequestTimeout = 2000;
        var RequestWaitIterations = 90; //90itr * 2000ms = 180 sec == 3 min max Waiting (Выверено эксперементально)
        BtnSend.setOnClickListener(View.OnClickListener {

            if(ActiveTreadCanRun)
            {
                ActiveTreadCanRun = false;

                ProgBar0.visibility = View.INVISIBLE;
                ProgBar1.visibility = View.INVISIBLE;
                ProgLable.setText("");

                BtnSend.setText("Создать книгу")

                //Log.println(Log.DEBUG, "KILL THREAD", "kill proccess ${KillAll.isChecked}")
                //kill all
                if (DialogShow){ KToggleLay.visibility = View.VISIBLE; }
            }
            else
            {
                if (DiscrTb.text.length > 1) {

                    ActiveTreadCanRun = true; //сли переменная true, то поток работает

                    BtnSend.setText("Остановить")

                    ProgBar0.visibility = View.VISIBLE;
                    ProgBar1.visibility = View.VISIBLE;
                    ProgBar1.isIndeterminate = true;

                    Log.println(Log.DEBUG, "debug", "Book Run");
                    ProgLable.setText("");
                    ProgLable.setTextColor(Color.parseColor("#ADADAD"))
                    val toast = Toast.makeText(this@GalleryFragment.context, "подключение к сайту", Toast.LENGTH_SHORT).show();
                    Thread {
                        var conection_state = 0;

                        var progress = "0";
                        var Iteration = 0;

                        conection_state = api.send_request(
                            api.MkBook(
                                AccauntInfo.Accaunt_API_key,
                                DiscrTb.text.toString(),
                                2
                            ), ConnectionTimeout
                        );

                        Thread.sleep(RequestTimeout.toLong());

                        while (Iteration < RequestWaitIterations && ActiveTreadCanRun) {
                            if ("Not Conection" in api.respounse) {
                                ProgLable.setTextColor(Color.parseColor("#FF0000"))
                                ProgLable.setText("Соединение разорвано!");
                                api.respounse = "";
                                break; }
                            if ("[DIED PROCCESS]:" in api.respounse) {
                                ProgLable.setTextColor(Color.parseColor("#FF0000"))
                                ProgLable.setText("Задача отменена администратором сервера");
                                api.respounse = "";
                                break; }

                            if (PID == "") { // if proccess in not exist

                                //if access denied
                                if ("403 Invalid Api Key" in api.respounse) {

                                    DiscrTb.setText("");
                                    ProgLable.setTextColor(Color.parseColor("#FF0000"))
                                    ProgLable.setText("(Неверный Ключ АПИ) Зайдите в аккаунт");
                                    break;
                                }
                                //get PID
                                try {
                                    ProgBar1.isIndeterminate = false;
                                    ProgBar1.progress = 0;
                                    ProgLable.setText("0% выполнено");

                                    PID = api.respounse.split("pid:")[1];
                                    Log.println(Log.DEBUG, "pid", PID.toString());
                                    //send Status Request & respounse = "Empty string"

                                    api.respounse = "";
                                    conection_state = api.send_request(api.GetStatus(AccauntInfo.Accaunt_API_key, PID), ConnectionTimeout);
                                } catch (e: Exception) {
                                    Log.println(Log.ERROR, "Thread", "Execute PID"); }
                            } else {
                                if ("[COMPLETE]" in api.respounse) {
                                    ProgBar1.progress = 100;
                                    ProgLable.setText(ProgBar1.progress.toString() + "% готово");

                                    //
                                    try {
                                        Thread.sleep(RequestTimeout.toLong());
                                        DiscrTb.setText("");
                                        ProgLable.setText("");
                                    } catch (e: Exception) {
                                    }



                                    var Content = api.respounse.split("[COMPLETE]:")[1];

                                    //format : header:<>;content:<>;
                                    try {
                                        TempBookContent.Header =
                                            Content.toString().split("header:")[1].split(";")[0];
                                        TempBookContent.Content =
                                            Content.toString().split("content:")[1].split(";")[0];
                                    } catch (e: Exception) {
                                        TempBookContent.Header = "ERROR"; TempBookContent.Content =
                                            "Book Formating Error"; }

                                    break;
                                } else {
                                    try {
                                        progress = api.respounse.split("Procents:")[1].split(";")[0];
                                        Log.println(Log.DEBUG, "proggress", progress);
                                        ProgBar1.progress = progress.toInt();
                                        ProgLable.setText(ProgBar1.progress.toString() + "% выполнено");
                                    } catch (e: Exception) {
                                    };
                                    api.respounse = "";
                                    conection_state = api.send_request(api.GetStatus(AccauntInfo.Accaunt_API_key, PID), ConnectionTimeout);
                                    //ReqText.setText(progress);
                                }

                            }
                            Thread.sleep(RequestTimeout.toLong());
                            Iteration++;
                        }
                        Log.println(Log.DEBUG, "ITERATION", "${Iteration} of ${RequestWaitIterations}")
                        ProgBar0.setVisibility(View.INVISIBLE);
                        ProgBar1.setVisibility(View.INVISIBLE);

                        //kill all
                        ActiveTreadCanRun = false;
                        KToggleLay.visibility = View.INVISIBLE;
                        BtnSend.setText("Создать книгу")

                    }.start()
                } else
                {
                    ProgLable.setTextColor(Color.parseColor("#FF0000"))
                    ProgLable.setText("Напишите название книги!");
                }
            }


        })
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}